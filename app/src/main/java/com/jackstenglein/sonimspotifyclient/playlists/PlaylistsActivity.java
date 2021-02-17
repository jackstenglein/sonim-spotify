package com.jackstenglein.sonimspotifyclient.playlists;

import android.content.Intent;
import android.util.Log;

import com.jackstenglein.sonimspotifyclient.home.HomeActivity;
import com.jackstenglein.sonimspotifyclient.list.AbstractDefaultListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsActivity extends AbstractDefaultListActivity<PlaylistSimple> {

    public static final String SPOTIFY_PLAYLIST_URI_EXTRA = "SpotifyPlaylistUri";
    public static final String SPOTIFY_PLAYLIST_ID_EXTRA = "SpotifyPlaylistId";
    public static final String SPOTIFY_USER_ID_EXTRA = "SpotifyUserId";

    private static final String TAG = "PlaylistsActivity";
    private static final int NAVIGATION_REQUEST_CODE = 9134;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected boolean shouldConnectToSpotifyAppRemote() {
        return false;
    }

    @Override
    protected void handleSelection(PlaylistSimple playlist) {
        if (playlist != null) {
            Log.d(TAG, "Show details for " + playlist.name + " with URI: " + playlist.uri);
            Intent intent = new Intent(this, PlaylistDetailActivity.class);
            intent.putExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA,
                    getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
            intent.putExtra(SPOTIFY_PLAYLIST_ID_EXTRA, playlist.id);
            intent.putExtra(SPOTIFY_PLAYLIST_URI_EXTRA, playlist.uri);
            intent.putExtra(SPOTIFY_USER_ID_EXTRA, playlist.owner.id);
            startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
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
