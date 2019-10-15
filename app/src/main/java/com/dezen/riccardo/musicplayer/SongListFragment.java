package com.dezen.riccardo.musicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.io.IOException;
import java.util.List;

public class SongListFragment extends Fragment{

    private SongViewModel songList;
    private ListView songsListView;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songList = ViewModelProviders.of(this).get(SongViewModel.class);
        songList.getSongList().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                songsListView.setAdapter(new CustomAdapter());
            }
        });
        songsListView = getView().findViewById(R.id.songs_listview);
        songsListView.setAdapter(new CustomAdapter());
        mediaPlayer = new MediaPlayer();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songlist, container, false);
    }

    private void play(String path){
        if(mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(IOException e){
            Toast.makeText(getContext(), "Impossibile riprodurre il file", Toast.LENGTH_SHORT).show();
        }
    }

    private View getItemView(final int index){
        final View newView = getLayoutInflater().inflate(R.layout.song_listview_item, null);
        ((TextView)newView.findViewById(R.id.textView_song_title)).setText(songList.get(index).getTitle());
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //play(songList.get(index).getData());
            }
        });
        return newView;
    }

    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return songList.size();
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
