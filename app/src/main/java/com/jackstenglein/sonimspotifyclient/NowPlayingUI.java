package com.jackstenglein.sonimspotifyclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import java.util.Locale;

public class NowPlayingUI {

    public static final String TIME_FORMAT = "%d:%02d";

    private static final String TAG = "NowPlayingUI";

    private Track currentTrack;
    private final View container;
    private final ImageView albumImage;
    private final TextView songTitle;
    private final TextView artistName;
    private final TextView timeElapsed;
    private final TextView totalTime;
    private final ProgressBar progressBar;
    private final SpotifyAppRemote spotifyAppRemote;

    public NowPlayingUI(Activity activity, SpotifyAppRemote spotifyAppRemote) {
        container = activity.findViewById(R.id.nowPlayingInfoContainer);
        albumImage = activity.findViewById(R.id.albumImage);
        songTitle = activity.findViewById(R.id.songTitle);
        artistName = activity.findViewById(R.id.artistName);
        timeElapsed = activity.findViewById(R.id.timeElapsed);
        totalTime = activity.findViewById(R.id.totalTime);
        progressBar = activity.findViewById(R.id.progressBar);
        this.spotifyAppRemote = spotifyAppRemote;
    }
    
    public void update(PlayerState playerState) {
        Track track = playerState.track;
        if (track == null) {
            Log.d(TAG, "No track playing.");
            currentTrack = null;
            if (container != null) {
                container.setVisibility(View.INVISIBLE);
            }
            return;
        }

        updateElapsedTime(playerState);

        if (track.equals(currentTrack)) return;

        Log.d(TAG, "Playing new track: " + track.name);
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

        if (container != null) {
            container.setVisibility(View.VISIBLE);
        }
    }

    private void updateElapsedTime(PlayerState playerState) {
        timeElapsed.setText(String.format(Locale.getDefault(), TIME_FORMAT,
                getMinutes(playerState.playbackPosition), getSeconds(playerState.playbackPosition)));
        progressBar.setProgress((int)playerState.playbackPosition, true);
    }

    private long getMinutes(long duration) {
        return (duration / 1000) / 60;
    }

    private long getSeconds(long duration) {
        return (duration / 1000) % 60;
    }
}
