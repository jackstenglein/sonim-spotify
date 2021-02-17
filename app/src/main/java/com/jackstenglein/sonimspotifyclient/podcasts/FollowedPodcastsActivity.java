package com.jackstenglein.sonimspotifyclient.podcasts;

import android.content.Intent;
import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.list.AbstractEpisodeListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.models.Pager;

public class FollowedPodcastsActivity extends AbstractEpisodeListActivity<SavedShow> {

    public static final String SPOTIFY_SHOW_ID_INTENT = "SpotifyShowId";

    private static final String TAG = "FollowedPodcastsActivit";
    private static final int NAVIGATION_REQUEST_CODE = 7845;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected boolean shouldConnectToSpotifyAppRemote() {
        return false;
    }

    @Override
    protected void handleSelection(SavedShow item) {
        Intent intent = new Intent(this, PodcastDetailActivity.class);
        intent.putExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA,
                getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
        intent.putExtra(SPOTIFY_SHOW_ID_INTENT, item.show.id);
        startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
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
