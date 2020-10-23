package com.dezen.riccardo.musicplayer.song;

import androidx.annotation.NonNull;

/**
 * Interface defining the behaviour of a Song library. An object implementing this should be treated
 * as a static library of all the available Songs in a Context.
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
    PlayList getLibrary();

    /**
     * Method to retrieve the currently selected Playlist.
     *
     * @return The currently selected PlayList.
     */
    @NonNull
    PlayList getPlayList();
}
