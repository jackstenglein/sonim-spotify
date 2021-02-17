package com.jackstenglein.sonimspotifyclient.search;

import com.jackstenglein.sonimspotifyclient.podcasts.SimpleEpisode;
import com.jackstenglein.sonimspotifyclient.podcasts.SimpleShow;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;

public class SearchResult {
    public Pager<Album> albums;
    public Pager<Track> tracks;
    public Pager<SimpleShow> shows;
    public Pager<SimpleEpisode> episodes;
}
