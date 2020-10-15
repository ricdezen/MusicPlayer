package com.dezen.riccardo.musicplayer.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Base class for a PlayList. Contains Songs, sorted based on some criteria. Allows navigating to
 * the previous or next element, shuffling, and some utility methods for searching and sorting.
 *
 * @author Riccardo De Zen.
 */
public class PlayList {

    private List<Song> songs = new ArrayList<>();
    private HashMap<String, Song> songById = new HashMap<>();

    public PlayList() {
    }

    public PlayList(Set<Song> content) {
        for (Song song : content) {
            songs.add(song);
            songById.put(song.getId(), song);
        }
    }

    /**
     * @param index The index in the playlist.
     * @return
     */
    public Song get(int index) {
        return songs.get(index);
    }

    /**
     * @return The size of the PlayList.
     */
    public int size() {
        return songs.size();
    }
}
