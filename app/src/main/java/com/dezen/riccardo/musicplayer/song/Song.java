package com.dezen.riccardo.musicplayer.song;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.Filterable;
import com.dezen.riccardo.musicplayer.Utils;

import java.util.Map;

/**
 * Class containing the data for a single Song.
 *
 * @author Riccardo De Zen.
 */
public class Song implements Filterable<String> {

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

    private final MediaMetadataCompat metadata;
    private final MediaBrowserCompat.MediaItem mediaItem;

    private final String id;
    private final String title;
    private final String album;
    private final String artist;

    private final Uri uri;

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
        this.id = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        this.title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        this.album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        this.artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

        this.uri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
    }

    @NonNull
    public MediaMetadataCompat getMetadata() {
        return metadata;
    }

    @NonNull
    public MediaBrowserCompat.MediaItem getMediaItem() {
        return mediaItem;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getArtist() {
        return artist;
    }

    @NonNull
    public String getAlbum() {
        return album;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    /**
     * A song matches a Query if its title contains the query itself.
     *
     * @param query any String.
     * @return True if the Song's title contains the query, false otherwise.
     */
    @Override
    public boolean matches(String query) {
        return getTitle().contains(query);
    }
}
