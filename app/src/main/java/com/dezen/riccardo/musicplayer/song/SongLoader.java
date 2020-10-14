package com.dezen.riccardo.musicplayer.song;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.Utils;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class dedicated to loading the song list.
 *
 * @author Riccardo De Zen.
 */
public class SongLoader {

    // Parameters for Thread Pool.
    private static final int DEVICE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int INITIAL_POOL_SIZE = Math.min(DEVICE_CORES, 3);
    private static final int MAX_POOL_SIZE = DEVICE_CORES + 3;
    private static final int KEEP_ALIVE_TIME = 3000;
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.MILLISECONDS;


    /**
     * The only available instance of the class.
     */
    private static SongLoader activeInstance;

    /**
     * The {@link ContentResolver} to use when querying the data.
     */
    private ContentResolver contentResolver;

    /**
     * App Resources.
     */
    private Resources resources;

    /**
     * Thread Pool to retrieve a song's image asynchronously.
     */
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            INITIAL_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_UNIT,
            new LinkedBlockingDeque<>()
    );

    /**
     * Private constructor.
     *
     * @param context The calling {@link Context}, used to retrieve the
     *                {@link android.content.ContentResolver}, after retrieving the application
     *                context.
     */
    private SongLoader(Context context) {
        contentResolver = context.getApplicationContext().getContentResolver();
        resources = context.getResources();
    }

    /**
     * Only way to retrieve instances of the class.
     *
     * @param context The calling {@link Context}, used to retrieve the
     *                {@link android.content.ContentResolver}, after retrieving the application
     *                context.
     */
    public static SongLoader getInstance(Context context) {
        if (activeInstance == null)
            activeInstance = new SongLoader(context);
        return activeInstance;
    }

    /**
     * TODO temporary null projection. Inefficient.
     * Method to retrieve an updated cursor from the {@link android.content.ContentResolver}.
     */
    private Cursor getCursor() {
        return contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.TITLE
        );
    }

    /**
     * Method used to start a background loading operation.
     *
     * @param listener A Listener that will receive the result of the loading.
     */
    public void loadSongList(@NonNull SongListListener listener) {
        new SongLoadTask(getCursor(), listener, contentResolver).execute();
    }

    /**
     * Load the bitmap for a Song, in full size. Will be a default one if not available.
     *
     * @param song The Song for which to get the thumbnail.
     */
    public void loadThumbnail(@NonNull Song song, @NonNull ThumbnailListener listener) {
        poolExecutor.execute(() -> listener.onLoaded(song.getId(), Utils.getThumbnail(
                song.getMetadata(),
                contentResolver,
                resources
        )));
    }

    /**
     * Interface used to define callbacks for {@link SongLoader#loadSongList(SongListListener)}.
     */
    public interface SongListListener {
        /**
         * Method called when the songs have been loaded.
         *
         * @param newList The new list of songs. May or may not be equal to the previous one.
         */
        void onLoaded(@NonNull List<Song> newList);
    }

    /**
     * Interface for {@link SongLoader#loadThumbnail(Song, ThumbnailListener)} callback.
     */
    public interface ThumbnailListener {
        /**
         * Method called when a thumbnail has been loaded.
         *
         * @param thumbnail The loaded thumbnail.
         */
        void onLoaded(@NonNull String id, @NonNull Bitmap thumbnail);
    }

}
