package com.dezen.riccardo.musicplayer;

import android.database.Cursor;

public class Song {
    private String title;
    private String author;
    private String album;
    private String duration;
    private String data;

    public Song(Cursor cursor){
        this.title = cursor.getString(1);
        this.author = cursor.getString(2);
        this.album = cursor.getString(3);
        this.duration = cursor.getString(4);
        this.data = cursor.getString(5);
    }

    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public String getAlbum() {return album;}
    public String getDuration() {return duration;}
    public String getData() {return data;}
}
