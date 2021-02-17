package com.jackstenglein.sonimspotifyclient.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.jackstenglein.sonimspotifyclient.HomeActivity;
import com.jackstenglein.sonimspotifyclient.R;
import com.jackstenglein.sonimspotifyclient.list.AbstractListActivity;
import com.jackstenglein.sonimspotifyclient.podcasts.FollowedPodcastsActivity;
import com.jackstenglein.sonimspotifyclient.podcasts.PodcastDetailActivity;
import com.jackstenglein.sonimspotifyclient.podcasts.SimpleEpisode;
import com.jackstenglein.sonimspotifyclient.podcasts.SimpleShow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.client.Response;

public class SearchActivity extends AbstractListActivity<Object, SpotifySearchService>
        implements TextView.OnEditorActionListener {

    private static final String TAG = "SearchActivity";
    private static final int NAVIGATION_REQUEST_CODE = 1764;
    private static final int SEARCH_LIMIT_PER_MEDIA_TYPE = 5;
    private static final String MEDIA_TYPE_ARTIST_FORMAT = "%s • %s";

    private EditText searchBar;
    private int nextOffset;
    private int maxTotal;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected SpotifySearchService getSpotifyWebApi() {
        SpotifySearchApi api = new SpotifySearchApi();
        api.setAccessToken(getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
        return api.getService();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.search_activity;
    }

    @Override
    protected boolean shouldFetchImmediately() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        Log.d(TAG, "onEditorAction keyCode: " + event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            Log.d(TAG, "onEditorAction: enter pushed");
            adapter.clear();
            search(0);
            hideKeyboard();
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        searchBar.setFocusable(false);
        searchBar.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        Log.d(TAG, "dispatchKeyEvent: " + keyEvent);

        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN &&
                searchBar.isFocused() && adapter.getItemCount() > 0) {
            hideKeyboard();
            adapter.updateSelection(keyEvent.getKeyCode());
            return true;
        }

        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    protected void handleSelection(Object item) {
        if (item instanceof Album) {
            spotifyAppRemote.getPlayerApi().play(((Album)item).uri);
        }

        if (item instanceof Track) {
            spotifyAppRemote.getPlayerApi().play(((Track)item).uri);
        }

        if (item instanceof SimpleShow) {
            Intent intent = new Intent(this, PodcastDetailActivity.class);
            intent.putExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA,
                    getIntent().getStringExtra(HomeActivity.SPOTIFY_TOKEN_EXTRA));
            intent.putExtra(FollowedPodcastsActivity.SPOTIFY_SHOW_ID_INTENT, ((SimpleShow)item).id);
            startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
        }

        if (item instanceof SimpleEpisode) {
            spotifyAppRemote.getPlayerApi().play(((SimpleEpisode)item).uri);
        }
    }

    @Override
    public boolean shouldHideSelection() {
        searchBar.setFocusable(true);
        searchBar.requestFocus();
        return true;
    }

    @Override
    public void getNextPage(Pager<Object> currentPage) {
        if (nextOffset > maxTotal) return;
        search(nextOffset);
    }

    private void search(int offset) {
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", SEARCH_LIMIT_PER_MEDIA_TYPE);
        queryParams.put("offset", offset);
        queryParams.put("q", searchBar.getText().toString());
        spotifyWebApi.search(queryParams, new SpotifyCallback<SearchResult>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e(getTag(), "failure: " + spotifyError.getErrorDetails(), spotifyError);
            }

            @Override
            public void success(SearchResult searchResult, Response response) {
                handleSearchResult(searchResult);
            }
        });
    }

    private void handleSearchResult(SearchResult searchResult) {
        int maxItems = Math.max(searchResult.albums.items.size(), searchResult.tracks.items.size());
        maxItems = Math.max(maxItems, searchResult.shows.items.size());
        maxItems = Math.max(maxItems, searchResult.episodes.items.size());
        nextOffset += maxItems;

        maxTotal = Math.max(searchResult.albums.total, searchResult.tracks.total);
        maxTotal = Math.max(maxTotal, searchResult.episodes.total);
        maxTotal = Math.max(maxTotal, searchResult.shows.total);

        ArrayList<Object> results = new ArrayList<>();
        results.addAll(searchResult.albums.items);
        results.addAll(searchResult.tracks.items);
        results.addAll(searchResult.episodes.items);
        results.addAll(searchResult.shows.items);
        Collections.shuffle(results);

        Pager<Object> pager = new Pager<>();
        pager.items = results;
        adapter.addPage(pager);
    }

    @Override
    public String getPrimaryText(Object item) {
        if (item instanceof Album) {
            return ((Album)item).name;
        }

        if (item instanceof Track) {
            return ((Track)item).name;
        }

        if (item instanceof SimpleShow) {
            return ((SimpleShow)item).name;
        }

        if (item instanceof SimpleEpisode) {
            return ((SimpleEpisode)item).name;
        }

        return null;
    }

    @Override
    public String getSecondaryText(Object item) {
        String mediaType = "Unknown";
        String artist = "Unknown";

        if (item instanceof Album) {
            mediaType = "Album";
            artist = ((Album)item).artists.get(0).name;
        }

        if (item instanceof Track) {
            mediaType = "Track";
            artist = ((Track)item).artists.get(0).name;
        }

        if (item instanceof SimpleShow) {
            mediaType = "Podcast";
            artist = ((SimpleShow)item).publisher;
        }

        if (item instanceof SimpleEpisode) {
            mediaType = "Podcast Episode";
            artist = PodcastDetailActivity.getReleaseDate((SimpleEpisode)item);
        }

        return String.format(Locale.getDefault(), MEDIA_TYPE_ARTIST_FORMAT, mediaType, artist);
    }
}
