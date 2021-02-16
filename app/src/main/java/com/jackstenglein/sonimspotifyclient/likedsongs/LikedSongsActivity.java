package com.jackstenglein.sonimspotifyclient.likedsongs;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;

import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.client.Response;

public class LikedSongsActivity extends AppCompatActivity implements LikedSongsAdapter.LikedSongsDataSource<SavedTrack> {

    private static final String TAG = "LikedSongsActivity";
    private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.primary_secondary_list);

        RecyclerView songsList = findViewById(R.id.primarySecondaryList);
        songsList.setHasFixedSize(true);
        LikedSongsAdapter adapter = LikedSongsAdapter.create(this);
        songsList.setAdapter(adapter);
        songsList.setLayoutManager(new LinearLayoutManager(this));

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
        SpotifyService spotify = api.getService();
        
        spotify.getMySavedTracks(new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e(TAG, "failure to get saved tracks: " + spotifyError.getErrorDetails().message, spotifyError);
            }

            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                adapter.addPage(savedTrackPager);
            }
        });
    }

    @Override
    public void getNextPage(Pager<SavedTrack> pager) {

    }

    @Override
    public String getPrimaryText(SavedTrack savedTrack) {
        return savedTrack.track.name;
    }

    @Override
    public String getSecondaryText(SavedTrack savedTrack) {
        String artist = savedTrack.track.artists.get(0).name;
        long minutes = getMinutes(savedTrack.track.duration_ms);
        long seconds = getSeconds(savedTrack.track.duration_ms);
        return String.format(Locale.getDefault(), ARTIST_NAME_AND_DURATION_FORMAT, artist, minutes,
                seconds);
    }

    private long getMinutes(long duration) {
        return (duration / 1000) / 60;
    }

    private long getSeconds(long duration) {
        return (duration / 1000) % 60;
    }
}
