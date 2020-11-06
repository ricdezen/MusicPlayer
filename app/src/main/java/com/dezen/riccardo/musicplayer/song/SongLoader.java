package com.dezen.riccardo.musicplayer.song;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Class dedicated to loading the song list.
 *
 * @author Riccardo De Zen.
 */
public class SongLoader {

    /**
     * The only available instance of the class.
     */
    private static SongLoader activeInstance;

    /**
     * The {@link ContentResolver} to use when querying the data.
     */
    private final ContentResolver contentResolver;

    /**
     * Private constructor.
     *
     * @param context The calling {@link Context}, used to retrieve the
     *                {@link android.content.ContentResolver}, after retrieving the application
     *                context.
     */
    private SongLoader(Context context) {
        contentResolver = context.getApplicationContext().getContentResolver();
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
     * Method to retrieve an updated cursor from the {@link android.content.ContentResolver}.
     * All columns are included by default.
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

}
