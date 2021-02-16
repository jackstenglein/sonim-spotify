package com.jackstenglein.sonimspotifyclient.likedsongs;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;
import com.jackstenglein.sonimspotifyclient.list.PagerAdapter;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.HashMap;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.client.Response;

public class LikedSongsActivity extends AppCompatActivity implements PagerAdapter.DataSource<SavedTrack> {

    private static final String TAG = "LikedSongsActivity";
    private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";

    private PagerAdapter<SavedTrack> adapter;
    private SpotifyService spotifyWebApi;
    private SpotifyAppRemote spotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.primary_secondary_list);

        RecyclerView songsList = findViewById(R.id.primarySecondaryList);
        songsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = PagerAdapter.create(this, layoutManager);
        songsList.setAdapter(adapter);
        songsList.setLayoutManager(layoutManager);

        connectToSpotifyAppRemote();
    }

    private void connectToSpotifyAppRemote() {
        ConnectionParams connectionParams = new ConnectionParams.Builder(HomeActivity.CLIENT_ID)
                .setRedirectUri(HomeActivity.REDIRECT_URI).showAuthView(false).build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener(){

            @Override
            public void onConnected(SpotifyAppRemote remote) {
                spotifyAppRemote = remote;
                Log.d(TAG, "Connected to spotify app remote");

                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
                spotifyWebApi = api.getService();
                getSpotifyTracks(0);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        });
    }

    private void getSpotifyTracks(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        spotifyWebApi.getMySavedTracks(queryParams, new SpotifyCallback<Pager<SavedTrack>>() {
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
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Log.d(TAG, "onKeyDown: " + keyCode, null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            adapter.updateSelection(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            SavedTrack savedTrack = adapter.getSelectedItem();
            if (savedTrack != null && spotifyAppRemote != null) {
                Log.d(TAG, "playing " + savedTrack.track.name + " with URI: " +
                        savedTrack.track.uri);
                spotifyAppRemote.getPlayerApi().play(savedTrack.track.uri);
            }
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public void getNextPage(Pager<SavedTrack> currentPage) {
        if (currentPage.next == null) return;

        int nextOffset = currentPage.offset + currentPage.items.size();
        getSpotifyTracks(nextOffset);
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
