package com.dezen.riccardo.musicplayer.song;

import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

/**
 * Class containing the data for a single Song.
 *
 * @author Riccardo De Zen.
 */
public class Song {

    private MediaMetadataCompat metadata;
    private MediaBrowserCompat.MediaItem mediaItem;

    /**
     * @param metadata The {@link MediaMetadataCompat} for this Song. Must contain at least: id,
     *                 uri, title, artist, album, duration.
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
    public String getDuration() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    @NonNull
    public Uri getUri() {
        return Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
    }
}
