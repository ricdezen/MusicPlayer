package com.dezen.riccardo.musicplayer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Custom class. Acts as an ArrayBlockingQueue, but if the maximum size would be exceeded by an
 * insertion, it removes the oldest item. This is done before attempting the insertion with `offer`
 * and `put`. The implementation is voluntarily naive. Honestly, aint nobody got time for that.
 *
 * @param <E> Type of Objects in the queue.
 */
public class CircularBlockingDeque<E> extends ArrayBlockingQueue<E> {

    private int capacity;

    /**
     * The Queue will hold at most capacity elements.
     *
     * @param capacity The maximum size of the queue.
     */
    public CircularBlockingDeque(int capacity) {
        super(capacity);
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        if (size() == capacity)
            remove();
        return super.add(e);
    }

    @Override
    public boolean offer(E e) {
        if (size() == capacity)
            remove();
        return super.offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (size() == capacity)
            remove();
        return super.offer(e, timeout, unit);
    }

    @Override
    public void put(E e) throws InterruptedException {
        if (size() == capacity)
            remove();
        super.put(e);
    }
}
