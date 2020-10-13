package com.dezen.riccardo.musicplayer;

import java.util.List;

/**
 * Interface defining the behaviour of a {@link List} designed to specifically contain
 * {@link Filterable} items.
 *
 * @param <Q> The type of query the items can be matched on.
 * @param <F> The type of {@link Filterable} Object contained.
 */
public interface FilterableList<Q, F extends Filterable<Q>> extends List<F> {
    /**
     * Method to apply a certain query to this list. The items that do not match the query are
     * removed from the list.
     *
     * @param query The query to match.
     * @return A {@link FilterableList} containing the items that did not match the query and
     * have therefore been removed.
     */
    FilterableList<Q, F> removeNonMatching(Q query);

    /**
     * Method to get the elements of the list that match the given query, without modifying the
     * list itself.
     *
     * @param query The query to match.
     * @return A {@link FilterableList} containing the items that matched the query.
     */
    FilterableList<Q, F> getMatching(Q query);
}
