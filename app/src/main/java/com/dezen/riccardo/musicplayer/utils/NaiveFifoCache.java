package com.dezen.riccardo.musicplayer.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Very basic and naive class, contains a map of key-value pairs. Has a maximum size, when items
 * are added and the size is exceeded, the oldest items are deleted. Used to cache some song
 * thumbnails instead of always loading them. Implementation is quite naive cuz ain't nobody got
 * time for that.
 *
 * @author Riccardo De Zen.
 */
public class NaiveFifoCache<K, V> {

    private final Map<K, V> elements = new HashMap<>();
    private final Queue<K> fifoQueue = new ArrayDeque<>();
    private final int maxSize;

    /**
     * @param maxSize The maximum size for this Cache.
     */
    public NaiveFifoCache(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Add a key-value pair. If the key is already in the map, it is ignored. If the item is not in
     * the map and the maximum number of items has already been reached, then the oldest item of
     * the map is removed to make room.
     *
     * @param key   The key for the new item.
     * @param value The item to add.
     */
    public void add(@NonNull K key, @NonNull V value) {
        if (elements.containsKey(key))
            return;
        if (elements.size() == maxSize)
            elements.remove(fifoQueue.remove());

        elements.put(key, value);
        fifoQueue.add(key);
    }

    /**
     * Retrieve an item from the cache.
     *
     * @param key The key for the item.
     * @return The item, if found, or null if it was not in the cache.
     */
    @Nullable
    public V get(@NonNull K key) {
        return elements.get(key);
    }

}
