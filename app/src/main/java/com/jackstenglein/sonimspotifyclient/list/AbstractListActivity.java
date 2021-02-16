package com.jackstenglein.sonimspotifyclient.list;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public abstract class AbstractListActivity<T> extends AppCompatActivity implements PagerAdapter.DataSource<T> {

    protected abstract String getTag();
    protected abstract void handleSelection(T item);

    protected PagerAdapter<T> adapter;
    protected SpotifyService spotifyWebApi;
    protected SpotifyAppRemote spotifyAppRemote;

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
                Log.d(getTag(), "Connected to spotify app remote");

                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
                spotifyWebApi = api.getService();
                getNextPage(null);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(getTag(), throwable.getMessage(), throwable);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Log.d(getTag(), "onKeyDown: " + keyCode, null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            adapter.updateSelection(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            handleSelection(adapter.getSelectedItem());
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(spotifyAppRemote);
        spotifyAppRemote = null;
    }
}
