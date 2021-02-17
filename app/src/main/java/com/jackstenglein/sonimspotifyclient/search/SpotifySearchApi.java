package com.jackstenglein.sonimspotifyclient.search;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;

public class SpotifySearchApi {
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

    private final SpotifySearchService mSpotifyService;

    private String mAccessToken;

    /**
     * Create instance of SpotifyApi with given executors.
     *
     * @param httpExecutor executor for http request. Cannot be null.
     * @param callbackExecutor executor for callbacks. If null is passed than the same
     *                         thread that created the instance is used.
     */
    public SpotifySearchApi(Executor httpExecutor, Executor callbackExecutor) {
        mSpotifyService = init(httpExecutor, callbackExecutor);
    }

    private SpotifySearchService init(Executor httpExecutor, Executor callbackExecutor) {

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setExecutors(httpExecutor, callbackExecutor)
                .setEndpoint(SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(new WebApiAuthenticator())
                .build();

        return restAdapter.create(SpotifySearchService.class);
    }

    /**
     *  New instance of SpotifyWebApi,
     *  with single thread executor both for http and callbacks.
     */
    public SpotifySearchApi() {
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
     * @return The instance of the wrapper.
     */
    public SpotifySearchApi setAccessToken(String accessToken) {
        mAccessToken = accessToken;
        return this;
    }

    /**
     * @return The SpotifyWebService instance
     */
    public SpotifySearchService getService() {
        return mSpotifyService;
    }
}
