package com.jackstenglein.sonimspotifyclient.podcasts;

import com.jackstenglein.sonimspotifyclient.list.AbstractEpisodeListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.models.Pager;

public class FollowedPodcastsActivity extends AbstractEpisodeListActivity<SavedShow> {

    private static final String TAG = "FollowedPodcastsActivit";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(SavedShow item) {

    }

    @Override
    public void getNextPage(Pager<SavedShow> currentPage) {
        if (currentPage == null) {
            getSpotifyShows(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getSpotifyShows(nextOffset);
    }

    private void getSpotifyShows(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        spotifyWebApi.getMyShows(queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(SavedShow item) {
        return item.show.name;
    }

    @Override
    public String getSecondaryText(SavedShow item) {
        return item.show.publisher;
    }
}
