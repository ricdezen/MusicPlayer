package com.dezen.riccardo.musicplayer.song;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, Song> idMap;
    private HashMap<String, Integer> indexMap;
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
        indexMap = new HashMap<>();
        idMap = new HashMap<>();
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
    @NonNull
    public synchronized List<Song> getSongs() {
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
    public synchronized Song get(int index) throws IndexOutOfBoundsException {
        return songList.get(index);
    }

    /**
     * Retrieve a Song based on its id instead of its index.
     * TODO this is a naive linear search. Use an HashMap..
     *
     * @param id The String id for the Song.
     * @return The Song whose id matches the String, or null if not present.
     */
    @Nullable
    public synchronized Song get(String id) {
        for (Song song : songList)
            if (song.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals(id))
                return song;
        return null;
    }

    /**
     * Find the Song that comes after the given one.
     *
     * @param id Id for the Song.
     * @return The next Song, or null if the Song list is empty or the Song can't be found.
     */
    @Nullable
    public synchronized Song next(String id) {
        // Empty list -> null.
        if (songList.isEmpty())
            return null;

        // Song not found -> null.
        Integer index = indexMap.get(id);
        if (index == null)
            return null;

        int nextIndex = (index + 1) % songList.size();
        return songList.get(nextIndex);
    }

    /**
     * Find the Song that comes before the given one.
     *
     * @param id Id for the Song.
     * @return The previous Song, or null if the Song list is empty or the Song can't be found.
     */
    @Nullable
    public synchronized Song previous(String id) {
        // Empty list -> null.
        if (songList.isEmpty())
            return null;

        // Song not found -> null.
        Integer index = indexMap.get(id);
        if (index == null)
            return null;

        int nextIndex = (index - 1 + songList.size()) % songList.size();
        return songList.get(nextIndex);
    }

    /**
     * @return The size of the List of Songs.
     */
    public synchronized int size() {
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
    public void onLoaded(@NonNull List<Song> newList) {
        synchronized (this) {
            songList = newList;
            // Build the maps for ids and indexes.
            idMap.clear();
            for (Song song : songList)
                idMap.put(song.getId(), song);
            indexMap.clear();
            for (int i = 0; i < songList.size(); i++)
                indexMap.put(songList.get(i).getId(), i);
            setChanged();
        }
        notifyObservers(songList);
    }
}
