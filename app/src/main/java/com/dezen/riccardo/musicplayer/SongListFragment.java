package com.dezen.riccardo.musicplayer;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

import java.util.Observer;

/**
 * Fragment displaying the song list for the app.
 *
 * @author Riccardo De Zen.
 */
public class SongListFragment extends Fragment {
    // TODO remove, think of something better.
    private static final int DEFAULT_BITMAP_SIZE = 128;

    private static final int NOW_PLAYING = 1;
    private static final int DEFAULT_VIEW = 0;

    private PlayerClient playerClient;
    private SongManager songManager;
    private RecyclerView songsRecycler;
    private View rootView;

    private String currentSong;

    // Runnable to update recycler.
    private Runnable updateRecycler = () -> {
        if (songsRecycler.getAdapter() != null)
            songsRecycler.getAdapter().notifyDataSetChanged();
    };

    // When songs are updated, update List.
    private Observer songObserver = (obj, newVal) -> onMainThread(updateRecycler);

    // Callback for player events.
    private MediaControllerCompat.Callback playerListener = new MediaControllerCompat.Callback() {
        /**
         * When the metadata is updated, get the id of the song and set it as the song currently
         * being played.
         *
         * @param metadata The current metadata for the session or null if none.
         */
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null)
                return;
            currentSong = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            onMainThread(updateRecycler);
        }
    };

    /**
     * TODO this may lead to a crash if the system tries to restore the fragment. Move client.
     * Create a new Fragment attached to a client for the app's Service.
     *
     * @param playerClient The client.
     */
    public SongListFragment(PlayerClient playerClient) {
        super();
        this.playerClient = playerClient;
        this.playerClient.setListener(playerListener);
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
        // First List setup.
        songsRecycler = rootView.findViewById(R.id.songs_recycler);
        songsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        songsRecycler.setAdapter(new CustomAdapter());

        // Observe changes in the Song list.
        songManager.addObserver(songObserver);
        songManager.updateSongs();
    }

    /**
     * Stop Observing songs.
     */
    @Override
    public void onStop() {
        super.onStop();
        songManager.deleteObserver(songObserver);
    }

    /**
     * Run a Runnable on the main UI thread.
     *
     * @param runnable Any Runnable.
     */
    private void onMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomHolder> {

        /**
         * A new ViewHolder is created.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public CustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == NOW_PLAYING) {
                View view = getLayoutInflater().inflate(R.layout.now_playing_item, parent, false);
                ImageView nowPlayingView = view.findViewById(R.id.imageView_now_playing);
                AnimatedVectorDrawable d = (AnimatedVectorDrawable) nowPlayingView.getDrawable();
                d.start();
            } else {

            }
            int layout = (viewType == NOW_PLAYING) ?
                    R.layout.now_playing_item :
                    R.layout.song_listview_item;
            return new CustomHolder(getLayoutInflater().inflate(layout, parent, false));
        }

        /**
         * A ViewHolder gets populated.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull CustomHolder holder, int position) {
            holder.populate(songManager.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            if (songManager.get(position).getId().equals(currentSong))
                return NOW_PLAYING;
            return DEFAULT_VIEW;
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return songManager.size();
        }
    }

    private class CustomHolder extends RecyclerView.ViewHolder {

        private TextView titleView;
        private TextView albumView;
        private TextView artistView;
        private ImageView imageView;
        private Song song;

        public CustomHolder(@NonNull View itemView) {
            super(itemView);
            this.titleView = itemView.findViewById(R.id.textView_song_title);
            this.albumView = itemView.findViewById(R.id.textView_song_album);
            this.artistView = itemView.findViewById(R.id.textView_song_artist);
            this.imageView = itemView.findViewById(R.id.imageView_song);
            this.imageView.setClipToOutline(true);
            this.itemView.setOnClickListener(v -> playerClient.play(this.song.getId()));
        }

        public void populate(@NonNull Song song) {
            this.song = song;
            titleView.setText(song.getTitle());
            albumView.setText(song.getAlbum());
            artistView.setText(song.getArtist());
            imageView.setImageBitmap(Utils.getDefaultThumbnail(getResources()));
            songManager.getThumbnail(song.getId(), DEFAULT_BITMAP_SIZE,
                    (id, thumbnail) -> onMainThread(() -> {
                        if (this.song.getId().equals(id))
                            imageView.setImageBitmap(thumbnail);
                        else
                            imageView.setImageBitmap(Utils.getDefaultThumbnail(getResources()));
                    })
            );
        }
    }
}
