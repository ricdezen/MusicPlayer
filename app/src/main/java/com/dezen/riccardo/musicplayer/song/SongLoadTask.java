package com.dezen.riccardo.musicplayer.song;

import android.database.Cursor;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining the background task that loads the songs from the
 * {@link android.content.ContentResolver}.
 *
 * @author Riccardo De Zen.
 */
class SongLoadTask extends AsyncTask<Void, Integer, Boolean> {

    private MutableLiveData<List<Song>> container;
    private Cursor cursor;

    /**
     * Constructor.
     *
     * @param cursor    The cursor that should load the songs, must be initialized according
     *                  to {@link SongLoader#PROJECTION}.
     * @param container The {@link MutableLiveData} that should contain the result.
     */
    public SongLoadTask(Cursor cursor, MutableLiveData<List<Song>> container) {
        this.cursor = cursor;
        this.container = container;
    }

    /**
     * The method uses {@code cursor} to load the list of songs, and then puts the result
     * into {@code container}. The progress is an integer ranging from 0 to 100, and is
     * published every time its value changes (its value is computed after each song is loaded).
     *
     * @param voids No arguments are taken into consideration.
     * @return {@code true} if the process was successful, {@code false} if no song was found
     * on the cursor and {@code null} if some system error occurred.
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        final int count = cursor.getCount();
        if (count < 1) return false;

        int loadedSongs = 0;
        List<Song> songs = new ArrayList<>();

        cursor.moveToFirst();
        do {
            songs.add(new Song(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            ));
            publishProgress(++loadedSongs / count * 100);
        } while (cursor.moveToNext());

        container.postValue(songs);
        return true;
    }
}
