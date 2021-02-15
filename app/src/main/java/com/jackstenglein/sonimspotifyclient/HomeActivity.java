package com.jackstenglein.sonimspotifyclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Locale;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.yashovardhan99.timeit.Stopwatch;

public class HomeActivity extends AppCompatActivity implements Stopwatch.OnTickListener {

    private enum SelectableItem {

        Search(R.id.searchContainer, SearchActivity.class),
        Playlists(R.id.playlistContainer, PlaylistsActivity.class),
        LikedSongs(R.id.likedSongsContainer, LikedSongsActivity.class),
        FollowedPodcasts(R.id.followedPodcastsContainer, FollowedPodcastsActivity.class),
        NowPlaying(R.id.nowPlayingContainer, NowPlayingActivity.class);

        private final int viewID;
        private final Class<?> activityClass;

        SelectableItem(int viewID, Class<?> activityClass) {
            this.viewID = viewID;
            this.activityClass = activityClass;
        }

        SelectableItem previousItem() {
            switch (this) {
                case Search: return NowPlaying;
                case Playlists: return Search;
                case LikedSongs: return Playlists;
                case FollowedPodcasts: return LikedSongs;
                case NowPlaying: return FollowedPodcasts;
            }
            return null;
        }

        SelectableItem nextItem() {
            switch (this) {
                case Search: return Playlists;
                case Playlists: return LikedSongs;
                case LikedSongs: return FollowedPodcasts;
                case FollowedPodcasts: return NowPlaying;
                case NowPlaying: return Search;
            }
            return null;
        }

        int getViewID() {
            return viewID;
        }

        Class<?> getActivityClass() {
            return activityClass;
        }
    }

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE = 1326;
    private static final String REDIRECT_URI = "sonimspotifyclient://callback";
    private static final String CLIENT_ID = "4a00bb1466e04ec5885388b05fb44a79";
    private static final String[] SPOTIFY_SCOPES = new String[] {"app-remote-control",
            "playlist-read-private", "user-library-read", "user-library-modify",
            "user-read-playback-position" };
    private static final String TIME_FORMAT = "%d:%02d";
    private static final int SPOTIFY_POLLING_DELAY_MS = 500;

    // Spotify connection variables
    private String spotifyAccessCode;
    private SpotifyAppRemote spotifyAppRemote;

    // Now playing UI elements and polling timer
    private Stopwatch stopwatch;
    private Track currentTrack;
    private View nowPlayingInfoContainer;
    private ImageView albumImage;
    private TextView songTitle;
    private TextView artistName;
    private TextView timeElapsed;
    private TextView totalTime;
    private ProgressBar progressBar;

    // UI elements to display selected item
    private SelectableItem currentSelection;
    private Drawable selectedBackground;
    private final HashMap<SelectableItem, View> selectableViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Get now playing UI elements
        nowPlayingInfoContainer = findViewById(R.id.nowPlayingInfoContainer);
        albumImage = findViewById(R.id.albumImage);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        timeElapsed = findViewById(R.id.timeElapsed);
        totalTime = findViewById(R.id.totalTime);
        progressBar = findViewById(R.id.progressBar);

        // Get UI elements for displaying selected item
        selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_item);
        currentSelection = SelectableItem.Search;
        for (SelectableItem item : SelectableItem.values()) {
            View view = findViewById(item.getViewID());
            assert(view != null);
            selectableViews.put(item, view);
        }

        // Connect to Spotify
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(SPOTIFY_SCOPES);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case CODE:
                    spotifyAccessCode = response.getCode();
                    Log.d(TAG, "Spotify Access Code: " + spotifyAccessCode);
                    connectToSpotifyAppRemote();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e(TAG, "Error response from Spotify AuthorizationClient",
                            new RuntimeException(response.getError()));
                    break;

                // Most likely auth flow was cancelled
                default:
            }
        }
    }

    private void connectToSpotifyAppRemote() {
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(false).build();
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
        stopwatch = new Stopwatch();
        stopwatch.setClockDelay(SPOTIFY_POLLING_DELAY_MS);
        stopwatch.setOnTickListener(this);
        stopwatch.start();
    }

    @Override
    public void onTick(Stopwatch stopwatch) {
        spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(
            new CallResult.ResultCallback<PlayerState>() {
                @Override
                public void onResult(PlayerState playerState) {
                    updateNowPlayingInformation(playerState);
                }
        });
    }

    private void updateNowPlayingInformation(PlayerState playerState) {
        Track track = playerState.track;
        if (track == null) {
            Log.d(TAG, "No track playing.");
            currentTrack = null;
            nowPlayingInfoContainer.setVisibility(View.INVISIBLE);
            return;
        }

        updateElapsedTime(playerState);

        if (track.equals(currentTrack)) return;

        Log.d(TAG, "Playing track: " + track.name);
        currentTrack = track;

        songTitle.setText(track.name);
        artistName.setText(track.artist.name);
        progressBar.setMax((int)track.duration);
        totalTime.setText(String.format(Locale.getDefault(),TIME_FORMAT, getMinutes(track.duration),
                getSeconds(track.duration)));

        spotifyAppRemote.getImagesApi().getImage(track.imageUri, Image.Dimension.SMALL)
                .setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                    @Override
                    public void onResult(Bitmap bitmap) {
                        albumImage.setImageBitmap(bitmap);
                    }
                });

        nowPlayingInfoContainer.setVisibility(View.VISIBLE);
    }

    private void updateElapsedTime(PlayerState playerState) {
        timeElapsed.setText(String.format(Locale.getDefault(),TIME_FORMAT,
            getMinutes(playerState.playbackPosition), getSeconds(playerState.playbackPosition)));
        progressBar.setProgress((int)playerState.playbackPosition, true);
    }

    private long getMinutes(long duration) {
        return (duration / 1000) / 60;
    }

    private long getSeconds(long duration) {
        return (duration / 1000) % 60;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(spotifyAppRemote);
        stopwatch.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Log.d(TAG, "onKeyDown: " + keyCode, null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
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

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            currentSelection = currentSelection.previousItem();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            currentSelection = currentSelection.nextItem();
        }

        selectableViews.get(currentSelection).setBackground(selectedBackground);
    }

    private void selectCurrentItem() {
        Intent intent = new Intent(this, currentSelection.getActivityClass());
        startActivity(intent);
    }
}
