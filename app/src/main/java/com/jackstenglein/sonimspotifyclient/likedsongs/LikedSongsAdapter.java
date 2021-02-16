package com.jackstenglein.sonimspotifyclient.likedsongs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jackstenglein.sonimspotifyclient.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

public class LikedSongsAdapter extends RecyclerView.Adapter<LikedSongsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final String ARTIST_NAME_AND_DURATION_FORMAT = "%s • %d:%02d";


        private final TextView songName;
        private final TextView artistNameAndDuration;

        private ViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.primaryText);
            artistNameAndDuration = itemView.findViewById(R.id.secondaryText);
        }

        private void bind(String songTitle, String artistName, long duration) {
            songName.setText(songTitle);
            artistNameAndDuration.setText(String.format(Locale.getDefault(),
                    ARTIST_NAME_AND_DURATION_FORMAT, artistName, getMinutes(duration),
                    getSeconds(duration)));
        }

        private long getMinutes(long duration) {
            return (duration / 1000) / 60;
        }

        private long getSeconds(long duration) {
            return (duration / 1000) % 60;
        }
    }

    private List<SavedTrack> savedTracks = new ArrayList<>();

    public void setSavedTracks(List<SavedTrack> savedTracks) {
        this.savedTracks = new ArrayList<>(savedTracks);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView  = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.primary_secondary_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Track track = savedTracks.get(position).track;
        holder.bind(track.name, track.artists.get(0).name, track.duration_ms);
    }

    @Override
    public int getItemCount() {
        return savedTracks.size();
    }
}
