package com.jackstenglein.sonimspotifyclient.podcasts;

import com.jackstenglein.sonimspotifyclient.podcasts.SavedShow;

import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface SpotifyEpisodeService {

    @GET("/me/shows")
    void getMyShows(@QueryMap Map<String, Object> options, Callback<Pager<SavedShow>> callback);
}
