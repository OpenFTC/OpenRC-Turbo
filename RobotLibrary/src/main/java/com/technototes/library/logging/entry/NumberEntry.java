package com.technototes.library.logging.entry;

import com.technototes.library.logging.Dimension;

import java.util.function.Supplier;

public class NumberEntry extends Entry<Number>{
    public NumberEntry(String n, Supplier<Number> s, int x) {
        super(n, s, x);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
