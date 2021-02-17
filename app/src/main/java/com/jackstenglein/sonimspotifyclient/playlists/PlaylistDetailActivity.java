package com.jackstenglein.sonimspotifyclient.playlists;

import android.util.Log;
import com.jackstenglein.sonimspotifyclient.list.AbstractDefaultListActivity;
import java.util.HashMap;
import java.util.Locale;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;

public class PlaylistDetailActivity extends AbstractDefaultListActivity<PlaylistTrack> {

    private static final String TAG = "PlaylistDetailActivity";
    private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(PlaylistTrack item) {
        if (item != null && spotifyAppRemote != null) {
            Log.d(TAG, "handleSelection: playing track with URI: " + item.track.uri);
            String playlistUri = getIntent()
                    .getStringExtra(PlaylistsActivity.SPOTIFY_PLAYLIST_URI_EXTRA);
            int index = adapter.getSelectedIndex();
            spotifyAppRemote.getPlayerApi().skipToIndex(playlistUri, index);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        return false;
    }

    @Override
    public void getNextPage(Pager<PlaylistTrack> currentPage) {
        if (currentPage == null) {
            getPlaylistTracks(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getPlaylistTracks(nextOffset);
    }

    private void getPlaylistTracks(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        String userId = getIntent().getStringExtra(PlaylistsActivity.SPOTIFY_USER_ID_EXTRA);
        String playlistId = getIntent().getStringExtra(PlaylistsActivity.SPOTIFY_PLAYLIST_ID_EXTRA);
        spotifyWebApi.getPlaylistTracks(userId, playlistId, queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(PlaylistTrack item) {
        return item.track.name;
    }

    @Override
    public String getSecondaryText(PlaylistTrack item) {
        String artist = item.track.artists.get(0).name;
        long minutes = getMinutes(item.track.duration_ms);
        long seconds = getSeconds(item.track.duration_ms);
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
