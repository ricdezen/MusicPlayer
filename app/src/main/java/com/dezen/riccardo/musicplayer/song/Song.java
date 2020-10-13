package com.dezen.riccardo.musicplayer.song;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.Utils;

import java.util.Map;

/**
 * Class containing the data for a single Song.
 *
 * @author Riccardo De Zen.
 */
public class Song {

    public static final String[] META_COLUMNS = {
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            MediaMetadataCompat.METADATA_KEY_TITLE,
            MediaMetadataCompat.METADATA_KEY_ALBUM,
            MediaMetadataCompat.METADATA_KEY_ARTIST
    };
    public static final String[] MEDIA_COLUMNS = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST
    };
    public static Map<String, String> MEDIA_TO_META = Utils.toMap(MEDIA_COLUMNS, META_COLUMNS);
    public static Map<String, String> META_TO_MEDIA = Utils.toMap(META_COLUMNS, MEDIA_COLUMNS);

    private MediaMetadataCompat metadata;
    private MediaBrowserCompat.MediaItem mediaItem;

    /**
     * @param metadata The {@link MediaMetadataCompat} for this Song. Must contain at least: id,
     *                 uri, title, artist, album.
     */
    public Song(@NonNull MediaMetadataCompat metadata) {
        mediaItem = new MediaBrowserCompat.MediaItem(
                metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        );

        this.metadata = metadata;
    }

    @NonNull
    public String getId() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
    }

    @NonNull
    public MediaMetadataCompat getMetadata() {
        return metadata;
    }

    @NonNull
    public String getTitle() {
        return String.valueOf(metadata.getDescription().getTitle());
    }

    @NonNull
    public String getAuthor() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_AUTHOR);
    }

    @NonNull
    public String getAlbum() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
    }

    @NonNull
    public Uri getUri() {
        return Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
    }
}
