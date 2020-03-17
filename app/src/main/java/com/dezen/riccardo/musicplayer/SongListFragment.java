package com.dezen.riccardo.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

    private MusicController musicController;
    private SongManager songManager;
    private ListView songsListView;
    private View rootView;

    private int currentSong;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * @param className The class name of the service.
         * @param service The returned Binder.
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            musicController = (MusicController) service;
            musicController.play(currentSong);
        }

        /**
         * @param arg0 The component that disconnected.
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            musicController = null;
        }
    };

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
        songManager.getSongs().observe(this,
                songs -> songsListView.setAdapter(new CustomAdapter()));
        songsListView = rootView.findViewById(R.id.songs_listview);
        songsListView.setAdapter(new CustomAdapter());

        if (musicController != null)
            musicController.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (musicController != null)
            musicController.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null)
            getActivity().unbindService(serviceConnection);
    }

    /**
     * This method attempts to bind to the {@link PlayerService} class to play a song.
     * The method attempts the binding only if the IBinder is {@code null}.
     *
     * @param position The song to play on the Service.
     */
    private void play(int position) {
        if (getActivity() == null)
            return;

        currentSong = position;

        if (musicController != null)
            musicController.play(position);
        else {
            // Making sure the service is started.
            getActivity().startService(new Intent(getContext(), PlayerService.class));
            // Binding to the service.
            getActivity().bindService(
                    new Intent(getContext(), PlayerService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    private View getItemView(final int index) {
        View newView = getLayoutInflater().inflate(R.layout.song_listview_item, null);
        ((TextView) newView.findViewById(R.id.textView_song_title)).setText(songManager.getSongs().getValue().get(index).getTitle());
        newView.setOnClickListener(v ->
                play(index)
        );
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
