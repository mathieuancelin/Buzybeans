package com.buzybeans.core.util;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author mathieu
 */
public class Pool<T> {

    private final static int POOL_SIZE = 100;
    
    private final BlockingQueue<T> objects;

    public Pool(Collection<? extends T> objects) {
        this.objects = new ArrayBlockingQueue<T>(POOL_SIZE, false);
    }

    public void add(T object) {
        this.objects.add(object);
    }

    public void remove(T object) {
        this.objects.remove(object);
    }

    public T borrow() throws InterruptedException {
        return this.objects.take();
    }

    public void giveBack(T object) throws InterruptedException {
        this.objects.put(object);
    }
}
