package com.technototes.logger.entry;

import java.util.function.Supplier;

public class BooleanEntry extends Entry<Boolean> {
    protected String whenTrue, whenFalse;
    public BooleanEntry(String n, Supplier<Boolean> s, int index, String wt, String wf) {
        super(n, s, index);
        whenTrue = wt;
        whenFalse = wf;
    }

    @Override
    public String toString() {
        return get() ? whenTrue : whenFalse;
    }
}
