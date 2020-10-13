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
            for (String key : cursor.getColumnNames()) {
                String metaKey = Song.MEDIA_TO_META.get(key);
                if (metaKey == null)
                    continue;
                builder.putString(metaKey, cursor.getString(cursor.getColumnIndex(key)));
            }
            // Uri is not in both tables.
            Uri uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
            );
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, String.valueOf(uri));

            songs.add(new Song(builder.build()));
            publishProgress(songs.size() / count * 100);
        } while (cursor.moveToNext());

        listener.onLoaded(songs);
        return true;
    }
}
