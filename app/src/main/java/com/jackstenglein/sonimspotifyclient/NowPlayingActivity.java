package com.jackstenglein.sonimspotifyclient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.PlayerState;
import com.yashovardhan99.timeit.Stopwatch;

public class NowPlayingActivity extends AppCompatActivity implements Stopwatch.OnTickListener,
        View.OnClickListener {

    private static final String TAG = "NowPlayingActivity";

    private SpotifyAppRemote spotifyAppRemote;

    // Now playing UI elements and polling timer
    private Stopwatch stopwatch;
    private NowPlayingUI nowPlayingUI;
    private PlayerState currentPlayerState;
    private LibraryState currentLibraryState;

    // UI elements for player controls
    private ImageView shuffleButton;
    private ImageView previousButton;
    private ImageView playButton;
    private ImageView nextButton;
    private ImageView likeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing_activity);

        shuffleButton = findViewById(R.id.shuffleButton);
        previousButton = findViewById(R.id.previousButton);
        playButton = findViewById(R.id.playButton);
        nextButton =  findViewById(R.id.nextButton);
        likeButton = findViewById(R.id.likeButton);
        shuffleButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);

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
    public void onClick(View view) {
        if (view.equals(shuffleButton)) {
            handleShuffleClick();
        } else if (view.equals(previousButton)) {
            handlePreviousClick();
        } else if (view.equals(playButton)) {
            handlePlayClick();
        } else if (view.equals(nextButton)) {
            handleNextClick();
        } else if (view.equals(likeButton)) {
            handleLikeClick();
        }
    }

    private void handleShuffleClick() {
        Log.d(TAG, "handleShuffleClick");
        spotifyAppRemote.getPlayerApi().toggleShuffle();
    }

    private void handlePreviousClick() {
        Log.d(TAG, "handlePreviousClick");
        spotifyAppRemote.getPlayerApi().skipPrevious();
    }

    private void handlePlayClick() {
        Log.d(TAG, "handlePlayClick");
        if (currentPlayerState.isPaused) {
            spotifyAppRemote.getPlayerApi().resume();
        } else {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    private void handleNextClick() {
        Log.d(TAG, "handleNextClick");
        spotifyAppRemote.getPlayerApi().skipNext();
    }

    private void handleLikeClick() {
        Log.d(TAG, "handleLikeClick");
        if (currentLibraryState == null) return;

        if (currentLibraryState.isAdded) {
            spotifyAppRemote.getUserApi().removeFromLibrary(currentLibraryState.uri);
        } else {
            spotifyAppRemote.getUserApi().addToLibrary(currentLibraryState.uri);
        }
    }
}
