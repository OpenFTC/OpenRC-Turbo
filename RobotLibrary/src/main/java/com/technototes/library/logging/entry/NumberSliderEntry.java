package com.technototes.library.logging.entry;

import com.technototes.library.logging.Dimension;

import java.util.function.Supplier;

public class NumberSliderEntry extends NumberEntry{
    public Number min, max, scale;
    public NumberSliderEntry(String n, Supplier<Number> s, Dimension d, Number mi, Number ma, Number sc) {
        super(n, s, d);
        min = mi;
        max = ma;
        scale = sc;
    }
    public NumberSliderEntry(String n, Supplier<Number> s, int x, int y, Number mi, Number ma, Number sc) {
        super(n, s, x, y);
        min = mi;
        max = ma;
        scale = sc;
    }

    @Override
    public String toString() {
        String r = name+": [";
        for(double i = (double)min; i <= (double)max; i+=(double)scale){
            if(Math.abs((double)get()-i)*2 < (double)scale){
                r+="#";
            }else{
                r+="~";
            }
        }
        return r+"]";
    }
}
