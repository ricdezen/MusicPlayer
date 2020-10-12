package com.dezen.riccardo.musicplayer;

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

}
