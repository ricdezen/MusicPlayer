package com.dezen.riccardo.musicplayer;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaActionSound;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SongViewModel extends AndroidViewModel {
    private SongListLiveData songList;
    private Thread currentThread;

    private static final String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
    };

    public SongViewModel(@NonNull Application application) {
        super(application);
    }
    public Song get(int i){return songList.get(i);}
    public int size(){return songList.size();}
    public LiveData<List<Song>> getSongList(){
        if(songList == null){
            songList = new SongListLiveData();
            songList.setValue(new ArrayList<Song>());
            loadSongList();
        }
        return songList;
    }
    public void loadSongList(){
        Cursor cursor = getApplication().getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE
        );
        currentThread = new SongListThread(songList, cursor);
        currentThread.start();
    }
}
