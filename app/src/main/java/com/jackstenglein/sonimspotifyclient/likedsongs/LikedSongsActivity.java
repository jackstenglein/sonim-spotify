package com.jackstenglein.sonimspotifyclient.likedsongs;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.client.Response;

public class LikedSongsActivity extends AppCompatActivity {

    private static final String TAG = "LikedSongsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.primary_secondary_list);

        RecyclerView songsList = findViewById(R.id.primarySecondaryList);
        songsList.setHasFixedSize(true);
        LikedSongsAdapter adapter = new LikedSongsAdapter();
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
                adapter.setSavedTracks(savedTrackPager.items);
            }
        });
    }
}
