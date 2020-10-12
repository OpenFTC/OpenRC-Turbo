package com.technototes.logger.entry;

import java.util.function.Supplier;

/** The root class for logging entries
 * @author Alex Stedman
 *
 * @param <T> The type of value being stored by the entry
 */
public abstract class Entry<T> implements Supplier<T> {

    protected int x;
    protected Supplier<T> supplier;

    protected String name;

    public Entry(String n, Supplier<T> s, int index){
        x = index;
        supplier = s;
        name = n;
    }

    @Override
    public T get() {
        return supplier.get();
    }

    @Override
    public String toString() {
        return name+": ["+get()+"]";
    }

    public String getName() {
        return name;
    }
    public int getIndex() {
        return x;
    }

}
