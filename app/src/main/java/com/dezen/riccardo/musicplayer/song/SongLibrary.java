package com.dezen.riccardo.musicplayer.song;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Interface defining the behaviour of a Song library. An object implementing this should be treated
 * as a static library of all the available Songs in a Context. A SongLibrary should implement
 * notifications for changes.
 *
 * @author Riccardo De Zen.
 */
public interface SongLibrary {

    /**
     * Method to retrieve the list of songs. If the List is empty it will also attempt to load it
     * in the background.
     *
     * @return A List of all the songs that are currently loaded.
     */
    @NonNull
    List<Song> getSongs();

    /**
     * @param index Index in the song list.
     * @return The item in the song list for the given index.
     * @throws IndexOutOfBoundsException If the index is not valid (not in 0 to size-1).
     */
    @NonNull
    Song get(int index) throws IndexOutOfBoundsException;

    /**
     * Retrieve a Song based on its id instead of its index.
     *
     * @param id The String id for the Song.
     * @return The Song whose id matches the String, or null if not present.
     */
    @Nullable
    Song get(@NonNull String id);


    /**
     * @return The size of the List of Songs.
     */
    int size();
}
