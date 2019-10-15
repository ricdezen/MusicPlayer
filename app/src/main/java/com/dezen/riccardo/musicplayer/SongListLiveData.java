package com.dezen.riccardo.musicplayer;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class SongListLiveData extends MutableLiveData<List<Song>> {
    public Song get(int i){
        return this.getValue().get(i);
    }
    public int size(){return this.getValue().size();}
}
