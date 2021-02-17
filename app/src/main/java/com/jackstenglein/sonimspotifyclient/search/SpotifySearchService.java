package com.jackstenglein.sonimspotifyclient.search;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface SpotifySearchService {

    @GET("/search?type=album,track,show,episode")
    void search(@QueryMap Map<String, Object> queryParams, Callback<SearchResult> callback);
}
