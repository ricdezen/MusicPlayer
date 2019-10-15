package com.dezen.riccardo.musicplayer;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 0;

    private SongViewModel songList;
    private ListView songsListView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if(checkPermissions()) init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_play_group:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            init();
    }

    private boolean checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void init(){
        songList = ViewModelProviders.of(this).get(SongViewModel.class);
        songList.getSongList().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                songsListView.setAdapter(new CustomAdapter());
            }
        });
        songsListView = findViewById(R.id.songs_listview);
        songsListView.setAdapter(new CustomAdapter());
        mediaPlayer = new MediaPlayer();
    }

    private View getItemView(final int index){
        final View newView = getLayoutInflater().inflate(R.layout.song_listview_item, null);
        ((TextView)newView.findViewById(R.id.textView_song_title)).setText(songList.get(index).getTitle());
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(songList.get(index).getData());
            }
        });
        return newView;
    }

    private void play(String path){
        if(mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(IOException e){
            Toast.makeText(getApplicationContext(), "Impossibile riprodurre il file", Toast.LENGTH_SHORT).show();
        }
    }

    private class CustomAdapter extends BaseAdapter{
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
