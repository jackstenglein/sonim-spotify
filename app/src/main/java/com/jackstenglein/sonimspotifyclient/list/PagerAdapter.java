package com.jackstenglein.sonimspotifyclient.list;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jackstenglein.sonimspotifyclient.R;
import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.models.Pager;

public class PagerAdapter<T> extends RecyclerView.Adapter<PagerAdapter.ViewHolder<T>> {

    public interface DataSource<T> {
        void getNextPage(Pager<T> pager);
        String getPrimaryText(T track);
        String getSecondaryText(T track);
    }

    static class ViewHolder<T> extends RecyclerView.ViewHolder {

        private final DataSource<T> dataSource;
        private final View itemContainer;
        private final TextView primaryText;
        private final TextView secondaryText;

        private ViewHolder(View itemView, DataSource<T> dataSource) {
            super(itemView);
            this.dataSource = dataSource;
            itemContainer = itemView.findViewById(R.id.primarySecondaryListItemContainer);
            primaryText = itemView.findViewById(R.id.primaryText);
            secondaryText = itemView.findViewById(R.id.secondaryText);
        }

        private void bind(T item, Drawable background) {
            primaryText.setText(dataSource.getPrimaryText(item));
            secondaryText.setText(dataSource.getSecondaryText(item));
            itemContainer.setBackground(background);
        }
    }

    private static final int PAGINATION_QUERY_BUFFER = 10;
    private static final int SCROLL_UP_INDEX = 5;
    private static final int SCROLL_DOWN_INDEX = 4;

    private final DataSource<T> dataSource;
    private final LinearLayoutManager layoutManager;
    private Drawable selectedBackground;
    private int selectedItem;
    private final List<T> items = new ArrayList<>();
    private boolean requestPending;
    private Pager<T> lastPage;

    private PagerAdapter(DataSource<T> dataSource, LinearLayoutManager layoutManager) {
        this.dataSource = dataSource;
        this.layoutManager = layoutManager;
    }

    public static <T> PagerAdapter<T> create(DataSource<T> dataSource,
                                             LinearLayoutManager layoutManager) {
        return new PagerAdapter<>(dataSource, layoutManager);
    }

    public void addPage(Pager<T> page) {
        items.addAll(page.items);
        lastPage = page;
        requestPending = false;
        notifyDataSetChanged();
    }

    public void updateSelection(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (selectedItem > 0) {
                selectedItem--;
            }
            scrollPageDownIfNecessary();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (selectedItem < items.size() - 1) {
                selectedItem++;
            }
            scrollPageUpIfNecessary();
            requestNextPageIfNecessary();
        }
        notifyDataSetChanged();
    }

    private void scrollPageDownIfNecessary() {
        if (selectedItem % SCROLL_DOWN_INDEX == 0) {
            layoutManager.scrollToPositionWithOffset(selectedItem - SCROLL_DOWN_INDEX, 0);
        }
    }

    private void scrollPageUpIfNecessary() {
        if (selectedItem % SCROLL_UP_INDEX == 0) {
            layoutManager.scrollToPositionWithOffset(selectedItem, 0);
        }
    }

    private void requestNextPageIfNecessary() {
        if (lastPage.next == null) return;
        if (requestPending) return;
        if (selectedItem < items.size() - PAGINATION_QUERY_BUFFER) return;

        requestPending = true;
        dataSource.getNextPage(lastPage);
    }

    public T getSelectedItem() {
        if (items.size() > 0)
            return items.get(selectedItem);

        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView  = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.primary_secondary_list_item, parent, false);
        selectedBackground = ContextCompat.getDrawable(parent.getContext(),
                R.drawable.selected_item);
        return new ViewHolder(itemView, dataSource);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Drawable background = (position == selectedItem) ? selectedBackground : null;
        holder.bind(items.get(position), background);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
