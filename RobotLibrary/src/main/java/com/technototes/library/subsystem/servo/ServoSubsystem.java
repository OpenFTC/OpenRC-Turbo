package com.technototes.library.subsystem.servo;

import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.subsystem.PID;
import com.technototes.library.subsystem.Subsystem;

public abstract class ServoSubsystem<T extends Servo> extends Subsystem<T> implements PID {
    public ServoSubsystem(T... d) {
        super(d);
    }

    public void setPosition(double val) {
        for (T m : devices) {
            m.setPosition(val);
        }
    }

    @Override
    public void setPIDValues(double p, double i, double d) {
        for (Servo s : devices) {
            s.setPIDValues(p, i, d);
        }
    }

    @Override
    public boolean setPositionPID(double ticks) {
        boolean b = true;
        for (Servo s : devices) {
            s.setPositionPID(ticks);
            if (!s.isAtPosition(ticks))
                b = false;
        }
        return b;
    }

    @Override
    public boolean setPositionPID(double p, double i, double d, double ticks) {
        setPIDValues(p, i, d);
        return setPositionPID(ticks);
    }
}
