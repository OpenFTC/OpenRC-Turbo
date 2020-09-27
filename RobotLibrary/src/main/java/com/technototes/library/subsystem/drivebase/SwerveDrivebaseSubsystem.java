package com.technototes.library.subsystem.drivebase;

import com.technototes.library.hardware.motor.Motor;

import java.util.function.DoubleSupplier;

public abstract class SwerveDrivebaseSubsystem<T extends Motor> extends OmnidirectionalDrivebaseSubsystem<T> {
    public SwerveDrivebaseSubsystem(T... d) {
        super(d);
    }

    public SwerveDrivebaseSubsystem(DoubleSupplier gyro, T... d) {
        this(d);
        gyroSupplier = gyro;
    }

    @Override
    public void drive(double speed, double angle, double twist) {
        //TODO
    }
}
