package com.dezen.riccardo.musicplayer.song;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Base class for a PlayList. Contains Songs, sorted based on some criteria. Allows navigating to
 * the previous or next element, shuffling, and some utility methods for searching and sorting.
 *
 * @author Riccardo De Zen.
 */
public class PlayList {

    private final Random random = new Random();
    private final List<Song> songs = new ArrayList<>();
    private final HashMap<String, Song> songById = new HashMap<>();
    private final HashMap<String, Integer> indexById = new HashMap<>();

    /**
     * TODO add, remove, etc to make the PlayList be usable even when empty.
     * Empty PlayList, currently just a placeholder.
     */
    public PlayList() {

    }

    /**
     * @param content A Set of Songs. The playlist will contain these.
     */
    public PlayList(Set<Song> content) {
        for (Song song : content) {
            songs.add(song);
            songById.put(song.getId(), song);
        }
        Collections.sort(songs);
        for (int i = 0; i < songs.size(); i++)
            indexById.put(songs.get(i).getId(), i);
    }

    /**
     * @param index The index in the playlist.
     * @return The Song at the given index. Raises IndexOutOfBoundsException if the index is
     * invalid (less than 0 or more than size-1).
     */
    public Song get(int index) {
        return songs.get(index);
    }

    /**
     * @param id The id of the Song.
     * @return The Song with the given id, or null if it's not present.
     */
    @Nullable
    public Song get(String id) {
        return songById.get(id);
    }

    /**
     * @return The size of the PlayList.
     */
    public int size() {
        return songs.size();
    }

    /**
     * @return True if the playlist is empty, false otherwise.
     */
    public boolean isEmpty() {
        return songs.isEmpty();
    }

    /**
     * Get the next song in the playlist.
     *
     * @param id The id of the current song.
     * @return The next Song. Returns null if the given id does not match any song in the
     * Playlist or if the Playlist is empty.
     */
    @Nullable
    public Song next(@NonNull String id) {
        // Empty list -> null.
        if (songs.isEmpty())
            return null;

        // Song not found -> null.
        Integer index = indexById.get(id);
        if (index == null)
            return null;

        int nextIndex = (index + 1) % songs.size();
        return songs.get(nextIndex);
    }

    /**
     * Get the next song in the playlist.
     *
     * @param song The current song.
     * @return The next Song.
     */
    public Song next(@NonNull Song song) {
        return next(song.getId());
    }

    /**
     * Get the previous song in the playlist.
     *
     * @param id The id of the current song.
     * @return The previous Song.
     */
    public Song previous(@NonNull String id) {
        // Empty list -> null.
        if (songs.isEmpty())
            return null;

        // Song not found -> null.
        Integer index = indexById.get(id);
        if (index == null)
            return null;

        int nextIndex = (index - 1 + songs.size()) % songs.size();
        return songs.get(nextIndex);
    }

    /**
     * Get the previous song in the playlist.
     *
     * @param song The current song.
     * @return The previous Song.
     */
    public Song previous(@NonNull Song song) {
        return previous(song.getId());
    }

    /**
     * Get a random song in the playlist (for random reproduction).
     *
     * @param id The id of a Song to exclude (ideally the current one, to avoid repetitions).
     * @return A random Song, if {@code id} is not null, its song will be excluded. Returns null if
     * the playlist is empty.
     */
    public Song random(@Nullable String id) {
        // Empty playlist -> null
        if (songs.isEmpty())
            return null;

        // Id null or not in playlist -> random.
        Integer start = indexById.get(id);
        int max = (id == null || start == null) ? songs.size() + 1 : songs.size();
        int step = random.nextInt(max);

        // If start == null -> Step goes from 0, otherwise from start, to skip it.
        return songs.get((((start != null) ? start : 0) + step) % songs.size());
    }
}
