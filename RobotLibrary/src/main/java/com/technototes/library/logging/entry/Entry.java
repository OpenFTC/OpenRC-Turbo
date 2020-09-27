package com.technototes.library.logging.entry;

import com.technototes.library.logging.Dimension;

import java.util.function.Supplier;

public abstract class Entry<T> implements Supplier<T> {

    public Dimension dimension;
    public Supplier<T> supplier;
    public String name;

    public Entry(String n, Supplier<T> s, Dimension d){
        dimension = d;
        supplier = s;
        name = n;
    }

    public Entry(String n, Supplier<T> s, int x, int y){
        this(n, s, new Dimension(x,y));
    }

    @Override
    public T get() {
        return supplier.get();
    }

    @Override
    public String toString() {
        return name+": ["+get()+"]";
    }
}
