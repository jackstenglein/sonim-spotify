package com.jackstenglein.sonimspotifyclient.playlists;

import android.util.Log;
import com.jackstenglein.sonimspotifyclient.list.AbstractListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.client.Response;

public class PlaylistsActivity extends AbstractListActivity<PlaylistSimple> {

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
        spotifyWebApi.getMyPlaylists(queryParams, new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e(TAG, "failure to get playlists: " + spotifyError.getErrorDetails(),
                        spotifyError);
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                adapter.addPage(playlistSimplePager);
            }
        });
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
