package com.jackstenglein.sonimspotifyclient.nowplaying;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.jackstenglein.sonimspotifyclient.R;
import com.jackstenglein.sonimspotifyclient.home.HomeActivity;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.PlayerState;
import com.yashovardhan99.timeit.Stopwatch;
import java.util.HashMap;

public class NowPlayingActivity extends AppCompatActivity implements Stopwatch.OnTickListener {

    private enum SelectableItem {
        Shuffle(R.id.shuffleButton),
        Previous(R.id.previousButton),
        Play(R.id.playButton),
        Next(R.id.nextButton),
        Like(R.id.likeButton);

        private final int viewID;

        SelectableItem(int viewID) {
            this.viewID = viewID;
        }

        SelectableItem previousItem() {
            switch (this) {
                case Shuffle: return Like;
                case Previous: return Shuffle;
                case Play: return Previous;
                case Next: return Play;
                case Like: return Next;
            }
            return null;
        }

        SelectableItem nextItem() {
            switch (this) {
                case Shuffle: return Previous;
                case Previous: return Play;
                case Play: return Next;
                case Next: return Like;
                case Like: return Shuffle;
            }
            return null;
        }

        int getViewID() {
            return viewID;
        }
    }

    private static final String TAG = "NowPlayingActivity";

    private SpotifyAppRemote spotifyAppRemote;

    // Now playing UI elements and polling timer
    private Stopwatch stopwatch;
    private NowPlayingUI nowPlayingUI;
    private PlayerState currentPlayerState;
    private LibraryState currentLibraryState;

    // UI elements for player controls
    private SelectableItem currentSelection;
    private Drawable selectedBackground;
    private final HashMap<SelectableItem, View> selectableViews = new HashMap<>();
    private ImageView playButton;
    private ImageView likeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing_activity);

        selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_item);
        currentSelection = SelectableItem.Play;
        for (SelectableItem item : SelectableItem.values()) {
            View view = findViewById(item.getViewID());
            assert(view != null);
            selectableViews.put(item, view);
        }
        playButton = findViewById(R.id.playButton);
        likeButton = findViewById(R.id.likeButton);

        // Connect to Spotify
        ConnectionParams connectionParams = new ConnectionParams.Builder(HomeActivity.CLIENT_ID)
                .setRedirectUri(HomeActivity.REDIRECT_URI).showAuthView(false).build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener(){

            @Override
            public void onConnected(SpotifyAppRemote remote) {
                spotifyAppRemote = remote;
                Log.d(TAG, "Connected to spotify app remote");
                subscribeToPlayerState();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: disconnectiong from app remote and stopping stopwatch");
        SpotifyAppRemote.disconnect(spotifyAppRemote);
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }

    private void subscribeToPlayerState() {
        nowPlayingUI = new NowPlayingUI(this, spotifyAppRemote);
        stopwatch = new Stopwatch();
        stopwatch.setClockDelay(HomeActivity.SPOTIFY_POLLING_DELAY_MS);
        stopwatch.setOnTickListener(this);
        stopwatch.start();
    }

    @Override
    public void onTick(Stopwatch stopwatch) {
        spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(
            new CallResult.ResultCallback<PlayerState>() {
                @Override
                public void onResult(PlayerState playerState) {
                    currentPlayerState = playerState;
                    nowPlayingUI.update(playerState);
                    getLibraryState();
                }
        });
    }

    private void getLibraryState() {
        if (currentPlayerState.track != null) {
            spotifyAppRemote.getUserApi().getLibraryState(currentPlayerState.track.uri)
                .setResultCallback(new CallResult.ResultCallback<LibraryState>() {
                    @Override
                    public void onResult(LibraryState libraryState) {
                        currentLibraryState = libraryState;
                        updateControlsUI();
                    }
            });
        }
    }

    private void updateControlsUI() {
        if (currentPlayerState.isPaused) {
            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        } else {
            playButton.setImageResource(R.drawable.ic_baseline_pause_24);
        }

        if (currentLibraryState.isAdded) {
            likeButton.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            likeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            updateSelectedItem(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            selectCurrentItem();
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    private void updateSelectedItem(int keyCode) {
        selectableViews.get(currentSelection).setBackground(null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            currentSelection = currentSelection.previousItem();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            currentSelection = currentSelection.nextItem();
        }

        selectableViews.get(currentSelection).setBackground(selectedBackground);
    }

    private void selectCurrentItem() {
        switch (currentSelection) {
            case Shuffle:
                handleShuffleClick();
                break;
            case Previous:
                handlePreviousClick();
                break;
            case Play:
                handlePlayClick();
                break;
            case Next:
                handleNextClick();
                break;
            case Like:
                handleLikeClick();
                break;
        }
    }

    private void handleShuffleClick() {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().toggleShuffle();
        }
    }

    private void handlePreviousClick() {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    private void handlePlayClick() {
        if (spotifyAppRemote == null || currentPlayerState == null) return;

        if (currentPlayerState.isPaused) {
            spotifyAppRemote.getPlayerApi().resume();
        } else {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    private void handleNextClick() {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    private void handleLikeClick() {
        if (spotifyAppRemote == null || currentLibraryState == null) return;

        if (currentLibraryState.isAdded) {
            spotifyAppRemote.getUserApi().removeFromLibrary(currentLibraryState.uri);
        } else {
            spotifyAppRemote.getUserApi().addToLibrary(currentLibraryState.uri);
        }
    }
}
