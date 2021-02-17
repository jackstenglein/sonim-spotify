package com.jackstenglein.sonimspotifyclient.podcasts;

import java.util.Map;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface SpotifyEpisodeService {

    @GET("/me/shows")
    void getMyShows(@QueryMap Map<String, Object> options, Callback<Pager<SavedShow>> callback);

    @GET("/shows/{id}/episodes")
    void getEpisodes(@Path("id") String showId, @QueryMap Map<String, Object> options,
                     Callback<Pager<SimpleEpisode>> callback);
}
