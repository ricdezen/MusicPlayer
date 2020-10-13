package com.dezen.riccardo.musicplayer;

/**
 * Interface defining the ability of an item to be filtered based on a certain criteria.
 *
 * @param <Q> The type for the filtering query/criteria to use.
 * @author Riccardo De Zen.
 */
public interface Filterable<Q> {
    /**
     * Method to check whether the Object matches the given criteria.
     *
     * @param query The criteria to check.
     * @return {@code true} if the Object matches the criteria, {@code false} otherwise.
     */
    boolean matches(Q query);
}
