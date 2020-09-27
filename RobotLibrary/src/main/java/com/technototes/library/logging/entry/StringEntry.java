package com.technototes.library.logging.entry;

import com.technototes.library.logging.Dimension;

import java.awt.*;
import java.util.function.Supplier;

public class StringEntry extends Entry<String>{
    public StringEntry(String n, Supplier<String> s, int x, int y) {
        super(n, s, x, y);
    }
    public StringEntry(String n, Supplier<String> s, Dimension d) {
        super(n, s, d);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
