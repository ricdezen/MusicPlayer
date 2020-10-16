package com.dezen.riccardo.musicplayer.song;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Size;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * Class defining the shared data shared among the app. Any Object can observe the Manager for
 * changes in the library, or in the current view for the associated Players.
 * SongManager instances must be freed after use, ideally when the Service is destroyed, along with
 * its session.
 *
 * @author Riccardo De Zen.
 */
public class SongManager extends Observable implements SongLoader.SongListListener, SongLibrary {
    /**
     * Instances of this class by media session token.
     */
    private static final Map<MediaSessionCompat.Token, SongManager> instancePool = new HashMap<>();

    // Observers for the PlayList.
    private final Set<PlayListObserver> playListObservers = new HashSet<>();

    // Keep null until something is set. If null return library when asking for PL.
    private PlayList currentPlayList;
    private PlayList songLibrary;

    private final Resources resources;
    private final SongLoader songLoader;
    private final MediaSessionCompat.Token token;

    /**
     * The private constructor.
     *
     * @param token   The token of the session for which to retrieve the SongManager.
     * @param context The calling {@link Context}, used to retrieve an instance of
     *                {@link SongLoader}.
     */
    private SongManager(@NonNull MediaSessionCompat.Token token,
                        @NonNull Context context) {
        this.token = token;
        resources = context.getResources();
        songLoader = SongLoader.getInstance(context);

        // The list of songs starts as empty.
        songLibrary = new PlayList();
    }

    /**
     * Only way to retrieve instances of the class.
     *
     * @param token   The token of the session for which to retrieve the SongManager.
     * @param context The calling {@link Context}, used to retrieve an instance of
     *                {@link SongLoader}.
     */
    public static SongManager of(@NonNull MediaSessionCompat.Token token,
                                 @NonNull Context context) {
        SongManager instance = instancePool.get(token);
        if (instance != null)
            return instance;
        SongManager newInstance = new SongManager(token, context);
        instancePool.put(token, newInstance);
        return newInstance;
    }

    /**
     * Remove this instance from the referenced ones. Ideally, after this is called, no more calls
     * to the instance can be made. The contents are still present, but there is no warranty of
     * their correctness.
     */
    public void free() {
        instancePool.remove(token);
    }

    /**
     * Method to retrieve the list of songs. If the List is empty it will also attempt to load it
     * in the background.
     *
     * @return A List of all the songs that are currently loaded.
     */
    @NonNull
    public synchronized PlayList getLibrary() {
        if (songLibrary.isEmpty())
            updateSongs();
        return songLibrary;
    }

    /**
     * @return The current PlayList.
     */
    @NonNull
    public synchronized PlayList getPlayList() {
        return (currentPlayList == null) ? songLibrary : currentPlayList;
    }

    /**
     * Sets a new PlayList. Must be a Subset of the full library. If it isn't, no checks are
     * performed on its integrity, have fun. The MediaPlayers listening to changes will be notified
     * of this change.
     *
     * @param newPlayList The new PlayList.
     */
    public synchronized void setPlayList(@NonNull PlayList newPlayList) {
        currentPlayList = newPlayList;
        notifyPlayListObservers();
    }

    /**
     * Resets the PlayList to the whole library.
     */
    public synchronized void resetPlayList() {
        currentPlayList = null;
        notifyPlayListObservers();
    }

    /**
     * Returns a Bitmap for a Song. The operation is performed asynchronously. If the song is
     * unknown, the operation is performed synchronously immediately.
     *
     * @param id       The id of the Song.
     * @param listener The callback for when the loading is done.
     */
    public void getThumbnail(@NonNull String id, @NonNull SongLoader.ThumbnailListener listener) {
        Song song = songLibrary.get(id);
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
    public void getThumbnail(@NonNull String id, Size size,
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
            // By construction of the Song database we know that the list is also a Set.
            songLibrary = new PlayList(new HashSet<>(newList));
            setChanged();
        }
        notifyObservers(songLibrary);
        // If the currentPlayList is null, observers believe the full library is the playlist.
        if (currentPlayList == null)
            notifyPlayListObservers();
    }

    /**
     * @param newObserver The new Object observing changes in the PlayList.
     */
    public void observePlayList(@NonNull PlayListObserver newObserver) {
        playListObservers.add(newObserver);
    }

    /**
     * @param observer The Observer to remove.
     */
    public void removeObserver(@NonNull PlayListObserver observer) {
        playListObservers.remove(observer);
    }

    /**
     * Notify the PlayListObservers there has been a change in the PlayList.
     */
    protected void notifyPlayListObservers() {
        for (PlayListObserver o : playListObservers)
            o.onChanged(getPlayList());
    }

    public interface PlayListObserver {
        /**
         * Called when the a new PlayList is set.
         *
         * @param newPlayList The new PlayList.
         */
        void onChanged(@NonNull PlayList newPlayList);
    }
}
