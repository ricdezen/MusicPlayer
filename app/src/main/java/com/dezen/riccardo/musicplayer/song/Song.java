package com.dezen.riccardo.musicplayer.song;

import androidx.annotation.NonNull;

/**
 * Class containing the data for a single Song.
 *
 * @author Riccardo De Zen.
 */
public class Song {

    private String title;
    private String author;
    private String album;
    private String duration;
    private String dataPath;

    /**
     * @param title    The title of the song.
     * @param author   The author of the song.
     * @param album    The album of the song.
     * @param duration The duration of the song.
     * @param dataPath The path on disk for the song.
     */
    public Song(@NonNull String title,
                @NonNull String author,
                @NonNull String album,
                @NonNull String duration,
                @NonNull String dataPath) {
        this.title = title;
        this.author = author;
        this.album = album;
        this.duration = duration;
        this.dataPath = dataPath;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    @NonNull
    public String getAlbum() {
        return album;
    }

    @NonNull
    public String getDuration() {
        return duration;
    }

    @NonNull
    public String getDataPath() {
        return dataPath;
    }
}
