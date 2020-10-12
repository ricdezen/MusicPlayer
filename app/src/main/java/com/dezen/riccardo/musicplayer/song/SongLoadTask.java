package com.dezen.riccardo.musicplayer.song;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining the background task that loads the songs from the
 * {@link android.content.ContentResolver}.
 *
 * @author Riccardo De Zen.
 */
class SongLoadTask extends AsyncTask<Void, Integer, Boolean> {

    private List<Song> songs = new ArrayList<>();
    private SongLoader.Listener listener;
    private Cursor cursor;

    /**
     * Constructor.
     *
     * @param cursor   The cursor that should load the songs.
     * @param listener The listener that will receive the result.
     */
    public SongLoadTask(Cursor cursor, SongLoader.Listener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    /**
     * The method uses {@code cursor} to load the list of songs, and then puts the result
     * into {@code container}. The progress is an integer ranging from 0 to 100, and is
     * published every time its value changes (its value is computed after each song is loaded).
     *
     * @param voids No arguments are taken into consideration.
     * @return {@code true} if the process was successful, {@code false} if no song was found.
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        final int count = cursor.getCount();
        if (count < 1) return false;

        songs.clear();
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        cursor.moveToFirst();
        do {
            String id = cursor.getString(0);
            Uri uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(id)
            );

            builder
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, String.valueOf(uri))
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, cursor.getString(1))
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cursor.getString(2))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cursor.getString(3))
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, cursor.getLong(4));

            songs.add(new Song(builder.build()));
            publishProgress(songs.size() / count * 100);
        } while (cursor.moveToNext());

        listener.onLoaded(songs);
        return true;
    }
}
