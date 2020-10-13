package com.dezen.riccardo.musicplayer;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * Return the bitwise XOR for an array of long arguments.
     *
     * @param args The arguments.
     * @return The bitwise XOR for the arguments.
     */
    public static long bitOR(long... args) {
        long or = 0;
        for (long n : args)
            or = or | n;
        return or;
    }

    /**
     * Given two arrays of the same size, returns a map of each element of the first array to the
     * corresponding element of the second. If values is longer than keys, the trailing elements are
     * ignored, if values is shorter an Exception is thrown.
     *
     * @param keys   An array of keys.
     * @param values An array of values.
     * @return A Map of the values in the first array to the values in the second.
     * @throws IndexOutOfBoundsException If values is shorter than keys.
     */
    @NonNull
    public static <K, V> Map<K, V> toMap(K[] keys, V[] values) throws IndexOutOfBoundsException {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++)
            map.put(keys[i], values[i]);
        return map;
    }

}
