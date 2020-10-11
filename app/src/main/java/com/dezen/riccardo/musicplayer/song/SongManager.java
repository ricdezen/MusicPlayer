package com.dezen.riccardo.musicplayer.song;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Class defining the shared data shared among the app.
 * TODO Make Observable to wrap songList's observation. This way getSongs can just return a List.
 *
 * @author Riccardo De Zen.
 */
public class SongManager extends Observable implements SongLoader.Listener {
    /**
     * Only available instance of this class.
     */
    private static SongManager activeInstance;

    private List<Song> songList;
    private SongLoader songLoader;

    /**
     * The private constructor.
     *
     * @param context The calling context, used to retrieve an instance of {@link SongLoader}.
     */
    private SongManager(Context context) {
        songLoader = SongLoader.getInstance(context);

        // The list of songs starts as empty.
        songList = new ArrayList<>();
    }

    /**
     * Only way to retrieve instances of the class.
     *
     * @param context The calling {@link Context}, used to retrieve an instance of
     *                {@link SongLoader}.
     */
    public static SongManager getInstance(Context context) {
        if (activeInstance == null)
            activeInstance = new SongManager(context.getApplicationContext());
        return activeInstance;
    }

    /**
     * Method to retrieve the list of songs. If the List is empty it will also attempt to load it
     * in the background.
     *
     * @return A List of all the songs that are currently loaded.
     */
    public List<Song> getSongs() {
        if (songList.isEmpty())
            updateSongs();
        return songList;
    }

    /**
     * @param index Index in the song list.
     * @return The item in the song list for the given index.
     * @throws IndexOutOfBoundsException If the index is not valid (not in 0 to size-1).
     */
    @NonNull
    public Song get(int index) throws IndexOutOfBoundsException {
        return songList.get(index);
    }

    /**
     * @return The size of the List of Songs.
     */
    public int size() {
        return songList.size();
    }

    /**
     * Method used to update the song list. A new List will be created and used when replacing
     * the current one. Observe this Object to be notified of these updates.
     */
    public void updateSongs() {
        songLoader.loadSongList(this);
    }

    /**
     * Method called when the songs get loaded. Replaces the list of songs with the new one and
     * notifies the observers.
     *
     * @param newList The new list of songs. May or may not be equal to the previous one.
     */
    @Override
    public void onLoaded(List<Song> newList) {
        songList = newList;
        setChanged();
        notifyObservers(songList);
    }
}
