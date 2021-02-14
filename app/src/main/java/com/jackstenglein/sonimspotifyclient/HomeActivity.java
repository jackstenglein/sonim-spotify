package com.jackstenglein.sonimspotifyclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private enum SelectableItem {

        Search(R.id.searchContainer, SearchActivity.class),
        Playlists(R.id.playlistContainer, PlaylistsActivity.class),
        LikedSongs(R.id.likedSongsContainer, LikedSongsActivity.class),
        FollowedPodcasts(R.id.followedPodcastsContainer, FollowedPodcastsActivity.class),
        NowPlaying(R.id.nowPlayingContainer, NowPlayingActivity.class);

        private final int viewID;
        private final Class<?> activityClass;

        SelectableItem(int viewID, Class<?> activityClass) {
            this.viewID = viewID;
            this.activityClass = activityClass;
        }

        SelectableItem previousItem() {
            switch (this) {
                case Search: return NowPlaying;
                case Playlists: return Search;
                case LikedSongs: return Playlists;
                case FollowedPodcasts: return LikedSongs;
                case NowPlaying: return FollowedPodcasts;
            }
            return null;
        }

        SelectableItem nextItem() {
            switch (this) {
                case Search: return Playlists;
                case Playlists: return LikedSongs;
                case LikedSongs: return FollowedPodcasts;
                case FollowedPodcasts: return NowPlaying;
                case NowPlaying: return Search;
            }
            return null;
        }

        int getViewID() {
            return viewID;
        }

        Class<?> getActivityClass() {
            return activityClass;
        }
    }

    private static final String TAG = "HomeActivity";

    private SelectableItem currentSelection;
    private Drawable selectedBackground;
    private final HashMap<SelectableItem, View> selectableViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_item);

        currentSelection = SelectableItem.Search;
        for (SelectableItem item : SelectableItem.values()) {
            View view = findViewById(item.getViewID());
            assert(view != null);
            selectableViews.put(item, view);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Log.d(TAG, "onKeyDown: " + keyCode, null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            updateSelectedItem(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            selectCurrentItem();
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    private void updateSelectedItem(int keyCode) {
        selectableViews.get(currentSelection).setBackground(null);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            currentSelection = currentSelection.previousItem();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            currentSelection = currentSelection.nextItem();
        }

        selectableViews.get(currentSelection).setBackground(selectedBackground);
    }

    private void selectCurrentItem() {
        Intent intent = new Intent(this, currentSelection.getActivityClass());
        startActivity(intent);
    }
}
