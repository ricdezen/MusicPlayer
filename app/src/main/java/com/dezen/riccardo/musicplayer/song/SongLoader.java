package com.dezen.riccardo.musicplayer.song;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * Class dedicated to loading the song list.
 *
 * @author Riccardo De Zen.
 */
public class SongLoader {
    /**
     * Fields extracted from the {@link android.content.ContentResolver}.
     */
    public static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
    };

    /**
     * The only available instance of the class.
     */
    private static SongLoader activeInstance;

    /**
     * The {@link ContentResolver} to use when querying the data.
     */
    private ContentResolver contentResolver;

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
     */
    private Cursor getCursor() {
        return contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                null,
                null,
                MediaStore.Audio.Media.TITLE
        );
    }

    /**
     * Method used to start a background loading operation.
     *
     * @param container The {@link MutableLiveData} that should contain the result of the loading.
     */
    public void loadSongList(@NonNull MutableLiveData<List<Song>> container) {
        new SongLoadTask(getCursor(), container).execute();
    }

}
