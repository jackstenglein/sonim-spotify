package com.jackstenglein.sonimspotifyclient.podcasts;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;

public class SpotifyEpisodeApi {
    /**
     * Main Spotify Web API endpoint
     */
    public static final String SPOTIFY_WEB_API_ENDPOINT = "https://api.spotify.com/v1";

    /**
     * The request interceptor that will add the header with OAuth
     * token to every request made with the wrapper.
     */
    private class WebApiAuthenticator implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            if (mAccessToken != null) {
                request.addHeader("Authorization", "Bearer " + mAccessToken);
            }
        }
    }

    private final SpotifyEpisodeService mSpotifyService;

    private String mAccessToken;

    private SpotifyEpisodeService init(Executor httpExecutor, Executor callbackExecutor) {

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setExecutors(httpExecutor, callbackExecutor)
                .setEndpoint(SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(new WebApiAuthenticator())
                .build();

        return restAdapter.create(SpotifyEpisodeService.class);
    }

    /**
     *  New instance of SpotifyWebApi,
     *  with single thread executor both for http and callbacks.
     */
    public SpotifyEpisodeApi() {
        Executor httpExecutor = Executors.newSingleThreadExecutor();
        MainThreadExecutor callbackExecutor = new MainThreadExecutor();
        mSpotifyService = init(httpExecutor, callbackExecutor);
    }

    /**
     * Sets access token on the wrapper.
     * Use to set or update token with the new value.
     * If you want to remove token set it to null.
     *
     * @param accessToken The token to set on the wrapper.
     */
    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    /**
     * @return The SpotifyWebService instance
     */
    public SpotifyEpisodeService getService() {
        return mSpotifyService;
    }
}
