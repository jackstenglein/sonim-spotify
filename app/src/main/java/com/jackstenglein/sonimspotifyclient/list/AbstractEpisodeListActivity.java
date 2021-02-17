package com.jackstenglein.sonimspotifyclient.list;

import com.jackstenglein.sonimspotifyclient.home.HomeActivity;
import com.jackstenglein.sonimspotifyclient.podcasts.SpotifyEpisodeApi;
import com.jackstenglein.sonimspotifyclient.podcasts.SpotifyEpisodeService;

public abstract class AbstractEpisodeListActivity<T>
        extends AbstractListActivity<T, SpotifyEpisodeService> {

    @Override
    protected SpotifyEpisodeService getSpotifyWebApi() {
        SpotifyEpisodeApi api = new SpotifyEpisodeApi();
        api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
        return api.getService();
    }
}
