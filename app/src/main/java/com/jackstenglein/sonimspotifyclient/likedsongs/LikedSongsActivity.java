package com.jackstenglein.sonimspotifyclient.likedsongs;

import android.util.Log;
import com.jackstenglein.sonimspotifyclient.list.AbstractDefaultListActivity;
import java.util.HashMap;
import java.util.Locale;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;

public class LikedSongsActivity extends AbstractDefaultListActivity<SavedTrack> {

    private static final String TAG = "LikedSongsActivity";
    private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(SavedTrack savedTrack) {
        if (savedTrack != null && spotifyAppRemote != null) {
            Log.d(TAG, "playing " + savedTrack.track.name + " with URI: " +
                    savedTrack.track.uri);
            spotifyAppRemote.getPlayerApi().play(savedTrack.track.uri);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        return false;
    }

    @Override
    public void getNextPage(Pager<SavedTrack> currentPage) {
        if (currentPage == null) {
            getSpotifyTracks(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getSpotifyTracks(nextOffset);
    }

    private void getSpotifyTracks(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        spotifyWebApi.getMySavedTracks(queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(SavedTrack savedTrack) {
        return savedTrack.track.name;
    }

    @Override
    public String getSecondaryText(SavedTrack savedTrack) {
        String artist = savedTrack.track.artists.get(0).name;
        long minutes = getMinutes(savedTrack.track.duration_ms);
        long seconds = getSeconds(savedTrack.track.duration_ms);
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
