package com.dezen.riccardo.musicplayer.song;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

/**
 * Class defining the shared data shared among the app.
 *
 * @author Riccardo De Zen.
 */
public class SongManager extends Observable implements SongLoader.SongListListener, SongLibrary {
    /**
     * Only available instance of this class.
     */
    private static SongManager activeInstance;

    private ContentResolver contentResolver;
    private Resources resources;

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
        resources = context.getResources();
        songLoader = SongLoader.getInstance(context);
        contentResolver = context.getContentResolver();

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
     *
     * @param id The String id for the Song.
     * @return The Song whose id matches the String, or null if not present.
     */
    @Nullable
    public synchronized Song get(@NonNull String id) {
        return idMap.get(id);
    }

    /**
     * Find the Song that comes after the given one.
     * TODO move to the playlist.
     *
     * @param id Id for the Song.
     * @return The next Song, or null if the Song list is empty or the Song can't be found.
     */
    @Nullable
    public synchronized Song next(@NonNull String id) {
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
     * TODO remove and move to the Playlist.
     *
     * @param id Id for the Song.
     * @return The previous Song, or null if the Song list is empty or the Song can't be found.
     */
    @Nullable
    public synchronized Song previous(@NonNull String id) {
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
     * Returns a Bitmap for a Song. The operation is performed asynchronously. If the song is
     * unknown, the operation is performed synchronously immediately.
     *
     * @param id       The id of the Song.
     * @param listener The callback for when the loading is done.
     */
    public void getThumbnail(@NonNull String id, @NonNull SongLoader.ThumbnailListener listener) {
        Song song = get(id);
        if (song == null) {
            listener.onLoaded(id, Utils.getDefaultThumbnail(resources));
            return;
        }

        songLoader.loadThumbnail(song, listener);
    }

    /**
     * Returns a Bitmap for a Song. The operation is performed asynchronously. If the song is
     * unknown, the operation is performed synchronously immediately.
     *
     * @param id       The id of the Song.
     * @param size     The target size.
     * @param listener The callback for when the loading is done.
     */
    public void getThumbnail(@NonNull String id, int size,
                             @NonNull SongLoader.ThumbnailListener listener) {
        // Wrap the listener to resize the result.
        getThumbnail(id, (resultId, thumbnail) ->
                listener.onLoaded(resultId, Utils.resizeThumbnail(thumbnail, size))
        );
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
