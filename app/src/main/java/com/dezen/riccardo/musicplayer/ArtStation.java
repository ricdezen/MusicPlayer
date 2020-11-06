package com.dezen.riccardo.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Size;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.utils.CircularBlockingDeque;
import com.dezen.riccardo.musicplayer.utils.NaiveFifoCache;
import com.dezen.riccardo.musicplayer.utils.Utils;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ArtStation {

    // Parameters for thread pool.
    private static final int DEVICE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int INITIAL_POOL_SIZE = Math.min(DEVICE_CORES, 3);
    private static final int MAX_POOL_SIZE = DEVICE_CORES + 3;
    private static final int KEEP_ALIVE_TIME = 3000;
    private static final int MAX_QUEUE_SIZE = 25;
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.MILLISECONDS;

    private static ArtStation instance;

    // Cache for thumbnails.
    private final NaiveFifoCache<String, Bitmap> thumbnailCache = new NaiveFifoCache<>(50);

    /**
     * Thread Pool to retrieve a song's image asynchronously.
     */
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            INITIAL_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_UNIT,
            new CircularBlockingDeque<>(MAX_QUEUE_SIZE)
    );

    private final ContentResolver contentResolver;
    private final Resources resources;

    /**
     * Private constructor.
     *
     * @param context App context, used to get the content resolver.
     */
    private ArtStation(@NonNull Context context) {
        this.contentResolver = context.getContentResolver();
        this.resources = context.getResources();
    }

    /**
     * @return Singleton instance of the class.
     */
    public static ArtStation getInstance(@NonNull Context context) {
        if (instance == null)
            instance = new ArtStation(context);
        return instance;
    }

    /**
     * Returns a Bitmap for a Song. The operation is performed asynchronously. If the song's bitmap
     * is in the cache, the listener is triggered immediately.
     *
     * @param song     The target Song.
     * @param listener The callback for when the loading is done.
     */
    public void getThumbnail(@NonNull Song song, @NonNull ArtStation.ThumbnailListener listener) {
        Bitmap cached = thumbnailCache.get(song.getId());
        if (cached != null)
            listener.onLoaded(song.getId(), cached);
        else {
            loadThumbnail(song, (resultId, thumbnail) -> {
                thumbnailCache.add(resultId, thumbnail);
                listener.onLoaded(resultId, thumbnail);
            });
        }
    }

    /**
     * Returns a Bitmap for a Song. The operation is performed asynchronously. If the song is
     * unknown, the operation is performed synchronously immediately.
     *
     * @param song     The id of the Song.
     * @param size     The target size.
     * @param listener The callback for when the loading is done.
     */
    public void getThumbnail(@NonNull Song song, Size size,
                             @NonNull ArtStation.ThumbnailListener listener) {
        // Wrap the listener to resize the result.
        getThumbnail(song, (resultId, thumbnail) ->
                listener.onLoaded(resultId, Utils.resizeThumbnail(thumbnail, size))
        );
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
     * Interface for {@link ArtStation#loadThumbnail(Song, ThumbnailListener)} callback.
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
