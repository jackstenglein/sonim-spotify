package com.jackstenglein.sonimspotifyclient.list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jackstenglein.sonimspotifyclient.home.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.client.Response;

public abstract class AbstractListActivity<T, K> extends AppCompatActivity implements PagerAdapter.DataSource<T> {

    protected abstract String getTag();
    protected abstract void handleSelection(T item);
    protected abstract K getSpotifyWebApi();

    protected PagerAdapter<T> adapter;
    protected K spotifyWebApi;
    protected SpotifyAppRemote spotifyAppRemote;
    protected boolean appRemoteReconnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        RecyclerView songsList = findViewById(R.id.primarySecondaryList);
        songsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = PagerAdapter.create(this, layoutManager);
        songsList.setAdapter(adapter);
        songsList.setLayoutManager(layoutManager);

        if (shouldConnectToSpotifyAppRemote()) {
            connectToSpotifyAppRemote();
        } else if (shouldFetchImmediately()) {
            spotifyWebApi = getSpotifyWebApi();
            getNextPage(null);
        }
    }

    protected int getLayoutId() {
        return R.layout.primary_secondary_list;
    }

    protected boolean shouldConnectToSpotifyAppRemote() {
        return true;
    }

    private void connectToSpotifyAppRemote() {
        ConnectionParams connectionParams = new ConnectionParams.Builder(HomeActivity.CLIENT_ID)
                .setRedirectUri(HomeActivity.REDIRECT_URI).showAuthView(false).build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener(){

            @Override
            public void onConnected(SpotifyAppRemote remote) {
                spotifyAppRemote = remote;
                Log.d(getTag(), "Connected to spotify app remote");

                spotifyWebApi = getSpotifyWebApi();
                if (!appRemoteReconnecting && shouldFetchImmediately()) {
                    getNextPage(null);
                } else if (appRemoteReconnecting) {
                    appRemoteReconnecting = false;
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(getTag(), throwable.getMessage(), throwable);
            }
        });
    }

    protected boolean shouldFetchImmediately() {
        return true;
    }

    protected SpotifyCallback<Pager<T>> getDefaultSpotifyCallback() {
        return new SpotifyCallback<Pager<T>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e(getTag(), "failure: " + spotifyError.getErrorDetails(), spotifyError);
            }

            @Override
            public void success(Pager<T> tPager, Response response) {
                adapter.addPage(tPager);
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (shouldConnectToSpotifyAppRemote()) {
            appRemoteReconnecting = true;
            connectToSpotifyAppRemote();
        }
    }
}
