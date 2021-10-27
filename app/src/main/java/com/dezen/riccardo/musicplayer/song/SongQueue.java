package com.dezen.riccardo.musicplayer.song;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A view on a set of songs for playback. Allows navigating forward and backwards, shuffling, sorting and searching.
 *
 * @author Riccardo De Zen.
 */
public class SongQueue implements Iterable<Song> {

    private List<Song> songList;

    public SongQueue(Iterable<Song> songs) {
        songList = new ArrayList<>();
        for (Song s : songs)
            songList.add(s);
    }

    public SongQueue() {
        songList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Iterator<Song> iterator() {
        return songList.iterator();
    }
}
