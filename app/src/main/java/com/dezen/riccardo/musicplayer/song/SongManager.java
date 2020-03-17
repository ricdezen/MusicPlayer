package com.dezen.riccardo.musicplayer.song;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining the shared data shared among the app.
 *
 * @author Riccardo De Zen.
 */
public class SongManager {
    /**
     * Only available instance of this class.
     */
    private static SongManager activeInstance;

    private MutableLiveData<List<Song>> songList;
    private SongLoader songLoader;

    /**
     * The private constructor.
     *
     * @param context The calling context, used to retrieve an instance of {@link SongLoader}.
     */
    private SongManager(Context context) {
        songList = new MutableLiveData<>(new ArrayList<>());
        songLoader = SongLoader.getInstance(context);
    }

    /**
     * Only way to retrieve instances of the class.
     *
     * @param context The calling {@link Context}, used to retrieve an instance of
     *                {@link SongLoader}.
     */
    public static SongManager getInstance(Context context) {
        if (activeInstance == null)
            activeInstance = new SongManager(context);
        return activeInstance;
    }

    /**
     * Method to retrieve the list of songs, and load it if its empty.
     *
     * @return A LiveData containing the song list and starts loading the songs. Whenever its
     * values are updated, a whole new list is created. But the {@link LiveData} stays the same.
     */
    public LiveData<List<Song>> getSongs() {
        if (songList.getValue() == null || songList.getValue().size() == 0)
            updateSongs();
        return songList;
    }

    /**
     * Method used to update the song list.
     */
    public void updateSongs() {
        songLoader.loadSongList(songList);
    }
}
