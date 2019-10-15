package com.dezen.riccardo.musicplayer;

import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class SongListThread extends Thread {
    private MutableLiveData<List<Song>> list;
    private Cursor cursor;
    public SongListThread(MutableLiveData<List<Song>> list, Cursor cursor){
        this.list = list;
        this.cursor = cursor;
    }
    public void run(){
        List<Song> songs = new ArrayList<>();
        if(cursor.getCount() < 1) return;
        cursor.moveToFirst();
        do{
            songs.add(new Song(cursor));
        }while(cursor.moveToNext());
        list.postValue(songs);
        cursor.close();
    }
}
