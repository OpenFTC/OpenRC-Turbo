package com.technototes.library.subsystem.motor;

import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.subsystem.PID;

public class EncodedMotorSubsystem extends MotorSubsystem<EncodedMotor>{
    public double maxSpeed = 0.5;

    public EncodedMotorSubsystem(EncodedMotor... m) {
        super(m);
    }

    public EncodedMotorSubsystem setMaxSpeed(double s) {
        maxSpeed = s;
        return this;
    }

    public boolean setPosition(double ticks) {
        return setPosition(ticks, maxSpeed);
    }

    public boolean setPosition(double ticks, double speed) {
        boolean b = true;
        for (EncodedMotor s : devices) {
            if (!s.setPosition(ticks, speed))
                b = false;
        }
        return b;
    }

}
