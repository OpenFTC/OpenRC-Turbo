package com.technototes.library.measurement.unit;

public abstract class Unit<D extends Enum<D>> {
    public Enum<D> unitType;
    public double value;
    public Unit(double v, Enum<D> e){
        unitType = e;
        value = v;
    }
    public abstract double to(D d);
    public abstract double get();

}
