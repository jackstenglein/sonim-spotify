package com.jackstenglein.sonimspotifyclient.search;

import com.jackstenglein.sonimspotifyclient.list.AbstractDefaultListActivity;
import java.util.HashMap;
import java.util.Locale;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;

public class AlbumDetailActivity extends AbstractDefaultListActivity<Track> {

    private static final String TAG = "AlbumDetailActivity";
    private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(Track item) {
        if (item != null && spotifyAppRemote != null) {
            String albumUri = getIntent().getStringExtra(SearchActivity.SPOTIFY_ALBUM_URI_EXTRA);
            int index = adapter.getSelectedIndex();
            spotifyAppRemote.getPlayerApi().skipToIndex(albumUri, index);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        return false;
    }

    @Override
    public void getNextPage(Pager<Track> currentPage) {
        if (currentPage == null) {
            getAlbumTracks(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getAlbumTracks(nextOffset);
    }

    private void getAlbumTracks(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        String albumId = getIntent().getStringExtra(SearchActivity.SPOTIFY_ALBUM_ID_EXTRA);
        spotifyWebApi.getAlbumTracks(albumId, queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(Track item) {
        return item.name;
    }

    @Override
    public String getSecondaryText(Track item) {
        String artist = item.artists.get(0).name;
        long minutes = getMinutes(item.duration_ms);
        long seconds = getSeconds(item.duration_ms);
        return String.format(Locale.getDefault(), ARTIST_NAME_AND_DURATION_FORMAT, artist, minutes,
                seconds);
    }

    private long getMinutes(long duration) {
        return (duration / 1000) / 60;
    }

    private long getSeconds(long duration) {
        return (duration / 1000) % 60;
    }
}
