package com.technototes.library.subsystem.motor;

import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.subsystem.Subsystem;

public abstract class MotorSubsystem<T extends Motor> extends Subsystem<T> {
    public MotorSubsystem(T... d) {
        super(d);
    }

    public void setSpeed(double val) {
        for (T m : devices) {
            m.setSpeed(val);
        }
    }

    public void stop() {
        for (T m : devices) {
            m.setSpeed(0);
        }
    }
}
