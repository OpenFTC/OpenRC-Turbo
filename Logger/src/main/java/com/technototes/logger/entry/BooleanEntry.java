package com.technototes.logger.entry;

import com.technototes.logger.Color;

import java.util.function.Supplier;

public class BooleanEntry extends Entry<Boolean> {
    private String whenTrue, whenFalse;
    private String tFormatted, fFormatted;
    private String trueFormat, falseFormat;
    private Color trueColor, falseColor;
    public BooleanEntry(String n, Supplier<Boolean> s, int index, String wt, String wf, Color c,
                        String tf, String ff, Color tc, Color fc) {
        super(n, s, index, c);
        trueFormat = tf;
        falseFormat = ff;
        trueColor = tc;
        falseColor = fc;
        whenTrue = wt;
        whenFalse = wf;
        tFormatted = trueColor.format(trueFormat, whenTrue);
        fFormatted = falseColor.format(falseFormat, whenFalse);

    }

    @Override
    public String toString() {
        return (get() ? tFormatted : fFormatted);
    }
}
