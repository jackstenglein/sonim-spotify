package com.jackstenglein.sonimspotifyclient.list;

import com.jackstenglein.sonimspotifyclient.home.HomeActivity;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public abstract class AbstractDefaultListActivity<T>
        extends AbstractListActivity<T, SpotifyService> {

    protected SpotifyService getSpotifyWebApi() {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
        return api.getService();
    }
}
