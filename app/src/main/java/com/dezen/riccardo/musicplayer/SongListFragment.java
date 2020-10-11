package com.dezen.riccardo.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dezen.riccardo.musicplayer.song.SongManager;

/**
 * Fragment displaying the song list for the app.
 *
 * @author Riccardo De Zen.
 */
public class SongListFragment extends Fragment {

    private PlayerClient playerClient;
    private SongManager songManager;
    private ListView songsListView;
    private View rootView;

    private int lastSong = 0;

    /**
     * Create a new Fragment attached to a client for the app's Service.
     *
     * @param playerClient The client.
     */
    public SongListFragment(PlayerClient playerClient) {
        super();
        this.playerClient = playerClient;
    }

    /**
     * Initializes the {@link SongManager}.
     *
     * @param context The Context that hosts the Fragment.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        songManager = SongManager.getInstance(context);
    }

    /**
     * Inflates the View for this Fragment.
     *
     * @param inflater           The inflater to use.
     * @param container          The parent container.
     * @param savedInstanceState The saved instance state if any.
     * @return An inflated {@link R.layout#fragment_songlist} view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_songlist, container, false);
        return rootView;
    }

    /**
     * Sets the list's adapter to listen to the song list.
     */
    @Override
    public void onStart() {
        super.onStart();
        songManager.getSongs().observe(
                this,
                songs -> songsListView.setAdapter(new CustomAdapter())
        );
        songsListView = rootView.findViewById(R.id.songs_listview);
        songsListView.setAdapter(new CustomAdapter());
    }

    /**
     * Play the selected Song.
     * TODO position.
     *
     * @param position The song to play on the Service.
     */
    private void play(int position) {
        lastSong = position;
        playerClient.play(
                // TODO see? Not pretty ffs.
                //songManager.getSongs().getValue().get(position).getMetadata().getString
                // (MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                String.valueOf(position)
        );
    }

    private View getItemView(final int index) {
        View newView = getLayoutInflater().inflate(R.layout.song_listview_item, null);
        ((TextView) newView.findViewById(R.id.textView_song_title)).setText(
                songManager.getSongs().getValue().get(index).getTitle()
        );
        newView.setOnClickListener(v -> play(index));
        return newView;
    }

    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return songManager.getSongs().getValue().size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItemView(position);
        }
    }
}
