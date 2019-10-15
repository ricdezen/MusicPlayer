package com.dezen.riccardo.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 0;

    private SongViewModel songList;
    private ListView songsListView;
    private MediaPlayer[] mediaPlayers;
    private LinearLayout selectedContainer;
    private Map<Integer, View> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if(checkPermissions()) init();
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
                playAll();
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
        selectedContainer = findViewById(R.id.selectedContainer);
        selectedItems = new HashMap<>();
        mediaPlayers = new MediaPlayer[5];
        mediaPlayers[0] = new MediaPlayer();
        mediaPlayers[1] = new MediaPlayer();
        mediaPlayers[2] = new MediaPlayer();
        mediaPlayers[3] = new MediaPlayer();
        mediaPlayers[4] = new MediaPlayer();
    }

    private View getItemView(final int index, final boolean append){
        final View newView = getLayoutInflater().inflate(R.layout.song_listview_multi_item, null);
        ((TextView)newView.findViewById(R.id.textView_song_title)).setText(songList.get(index).getTitle());
        ((ImageView)newView.findViewById(R.id.clip_button)).setImageDrawable(getDrawable((append)?R.drawable.paperclip_black:R.drawable.paperclip_grey));
        newView.findViewById(R.id.song_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(songList.get(index).getData());
            }
        });
        if(append){
            //append == true, i need to tell the button to remove if clicked
            newView.findViewById(R.id.button_frame).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(newView);
                }
            });
        }
        else{
            //append == false, i need to tell the button to append if clicked
            newView.findViewById(R.id.button_frame).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedContainer.getChildCount() >= 5) return;
                    append(getItemView(index, true));
                }
            });
        }
        return newView;
    }

    private void append(View v){
        selectedContainer.addView(v);
    }

    private void remove(View v){
        selectedContainer.removeView(v);
    }

    private void play(String path){
        int i = 0;
        while(i < 5 && mediaPlayers[i].isPlaying()) i++;
        play(path, i%5);
    }

    private void play(String path, int where){
        MediaPlayer mediaPlayer = mediaPlayers[where];
        if(mediaPlayer.isPlaying()) mediaPlayer.stop();
        try{
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(IOException e){
            Toast.makeText(getApplicationContext(), "Impossibile riprodurre il file", Toast.LENGTH_SHORT);
        }
    }

    private void playAll(){
        stopAll();
    }

    private void stopAll(){
        for(MediaPlayer m : mediaPlayers) m.stop();
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
            return getItemView(position, false);
        }
    }
}
