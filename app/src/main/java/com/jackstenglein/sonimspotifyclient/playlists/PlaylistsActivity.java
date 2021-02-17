package com.jackstenglein.sonimspotifyclient.playlists;

import android.util.Log;
import com.jackstenglein.sonimspotifyclient.list.AbstractDefaultListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsActivity extends AbstractDefaultListActivity<PlaylistSimple> {

    private static final String TAG = "PlaylistsActivity";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(PlaylistSimple playlist) {
        if (playlist != null && spotifyAppRemote != null) {
            Log.d(TAG, "Playing " + playlist.name + " with URI: " + playlist.uri);
            spotifyAppRemote.getPlayerApi().play(playlist.uri);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        return false;
    }

    @Override
    public void getNextPage(Pager<PlaylistSimple> currentPage) {
        if (currentPage == null) {
            getSpotifyPlaylists(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getSpotifyPlaylists(nextOffset);
    }

    private void getSpotifyPlaylists(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        spotifyWebApi.getMyPlaylists(queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(PlaylistSimple playlist) {
        return playlist.name;
    }

    @Override
    public String getSecondaryText(PlaylistSimple playlist) {
        return playlist.tracks.total + " songs";
    }
}
