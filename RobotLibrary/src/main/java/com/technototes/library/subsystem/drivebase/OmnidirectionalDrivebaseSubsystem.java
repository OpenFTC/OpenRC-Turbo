package com.technototes.library.subsystem.drivebase;

import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.util.MathUtils;

import java.util.function.DoubleSupplier;

public abstract class OmnidirectionalDrivebaseSubsystem<T extends Motor> extends DrivebaseSubsystem<T> {
    public DoubleSupplier gyroSupplier = () -> 0;

    public OmnidirectionalDrivebaseSubsystem(T... motors) {
        super(motors);

    }

    public OmnidirectionalDrivebaseSubsystem(DoubleSupplier gyro, T... d) {
        this(d);
        gyroSupplier = gyro;
    }

    public abstract void drive(double speed, double angle, double twist);

    public void driveFieldCentric(double speed, double x, double y, double twist) {
        drive(speed, Math.toDegrees(Math.atan2(y, x)) + gyroSupplier.getAsDouble(), twist);
    }

    public void drive(double speed, double x, double y, double twist) {
        drive(speed, Math.toDegrees(Math.atan2(y, x)), twist);
    }
}
