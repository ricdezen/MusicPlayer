package com.dezen.riccardo.musicplayer;

import com.dezen.riccardo.musicplayer.song.Song;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class extending {@link ArrayList} and implementing {@link FilterableList}
 *
 * @author Riccardo De Zen.
 */
public class PlayList extends ArrayList<Song> implements FilterableList<String, Song> {
    /**
     * Default constructor. Needs to be declared because it's not the only one.
     */
    public PlayList() {
        super();
    }

    /**
     * Collection constructor
     *
     * @param collection A collection of {@link Song} to copy into this List.
     */
    public PlayList(Collection<Song> collection) {
        super(collection);
    }

    /**
     * Method to apply a certain query to this list. The items that do not match the query are
     * removed from the list.
     *
     * @param query The query to match.
     * @return A {@link PlayList} containing the items that did not match the query and
     * have therefore been removed.
     */
    @Override
    public PlayList removeNonMatching(String query) {
        PlayList copyOfThisList = new PlayList(this);
        PlayList removedItems = new PlayList();
        for (Song eachItem : copyOfThisList) {
            if (!eachItem.matches(query)) {
                remove(eachItem);
                removedItems.add(eachItem);
            }
        }
        return removedItems;
    }

    /**
     * Method to get the elements of the list that match the given query, without modifying the
     * list itself.
     *
     * @param query The query to match.
     * @return A {@link PlayList} containing the items that matched the query.
     */
    @Override
    public PlayList getMatching(String query) {
        PlayList matchingItems = new PlayList();
        for (Song eachItem : this)
            if (eachItem.matches(query))
                matchingItems.add(eachItem);
        return matchingItems;
    }
}
