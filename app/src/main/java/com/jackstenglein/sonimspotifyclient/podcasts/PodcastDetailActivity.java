package com.jackstenglein.sonimspotifyclient.podcasts;

import android.util.Log;
import com.jackstenglein.sonimspotifyclient.list.AbstractEpisodeListActivity;
import java.util.HashMap;
import kaaes.spotify.webapi.android.models.Pager;

public class PodcastDetailActivity extends AbstractEpisodeListActivity<SimpleEpisode> {

    private static final String TAG = "PodcastDetailActivity";

    private static final String EMPTY_STRING = "";

    private static final String RELEASE_PRECISION_YEAR = "year";
    private static final int RELEASE_YEAR_START_INDEX = 0;
    private static final int RELEASE_YEAR_END_INDEX = 4;
    private static final int RELEASE_YEAR_SHORTHAND_INDEX = 2;

    private static final String RELEASE_PRECISION_MONTH = "month";
    private static final int RELEASE_MONTH_START_INDEX = 5;
    private static final int RELEASE_MONTH_END_INDEX = 7;

    private static final int RELEASE_DAY_START_INDEX = 8;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void handleSelection(SimpleEpisode episode) {
        if (episode != null && spotifyAppRemote != null) {
            Log.d(TAG, "playing " + episode.name + " with URI: " + episode.uri);
            spotifyAppRemote.getPlayerApi().play(episode.uri);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        return false;
    }

    @Override
    public void getNextPage(Pager<SimpleEpisode> currentPage) {
        if (currentPage == null) {
            getSpotifyEpisodes(0);
            return;
        }

        if (currentPage.next == null) return;
        int nextOffset = currentPage.offset + currentPage.items.size();
        getSpotifyEpisodes(nextOffset);
    }

    private void getSpotifyEpisodes(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("offset", offset);
        String showId = getIntent().getStringExtra(FollowedPodcastsActivity.SPOTIFY_SHOW_ID_INTENT);
        spotifyWebApi.getEpisodes(showId, queryParams, getDefaultSpotifyCallback());
    }

    @Override
    public String getPrimaryText(SimpleEpisode episode) {
        return episode.name;
    }

    @Override
    public String getSecondaryText(SimpleEpisode episode) {
        StringBuilder builder = new StringBuilder();
        String releaseDate = getReleaseDate(episode);
        if ( ! EMPTY_STRING.equals(releaseDate)) {
            builder.append(releaseDate);
            builder.append(" • ");
        }

        long hours = getHours(episode.duration_ms);
        long minutes = getMinutes(episode.duration_ms);
        builder.append(hours).append(" hr ");
        builder.append(minutes).append(" min");

        return builder.toString();
    }

    public static String getReleaseDate(SimpleEpisode episode) {
        if (episode.release_date_precision == null || episode.release_date == null) {
            return EMPTY_STRING;
        }

        if (RELEASE_PRECISION_YEAR.equals(episode.release_date_precision)) {
            return episode.release_date;
        }

        String year = episode.release_date.substring(RELEASE_YEAR_START_INDEX,
                RELEASE_YEAR_END_INDEX);
        String month = episode.release_date.substring(RELEASE_MONTH_START_INDEX,
                RELEASE_MONTH_END_INDEX);

        if (RELEASE_PRECISION_MONTH.equals(episode.release_date_precision)) {
            return month + "/" + year;
        }

        String day = episode.release_date.substring(RELEASE_DAY_START_INDEX);
        return day + "/" + month + "/" + year.substring(RELEASE_YEAR_SHORTHAND_INDEX);
    }

    private long getHours(long duration) {
        return ((duration / 1000) / 60) / 60;
    }

    private long getMinutes(long duration) {
        return ((duration / 1000) / 60) % 60;
    }
}
