package com.technototes.library.subsystem.drivebase;

import com.technototes.library.hardware.motor.Motor;
import com.technototes.subsystem.HolomonicDrivebaseSubsystem;

import java.util.function.DoubleSupplier;

public class MecanumDrivebaseSubsystem<T extends Motor> extends DrivebaseSubsystem<T> implements HolomonicDrivebaseSubsystem {
    public T flMotor, frMotor, rlMotor, rrMotor;

    public MecanumDrivebaseSubsystem(T... d) {
        super(d);
        flMotor = d[0];
        frMotor = d[1];
        rlMotor = d[2];
        rrMotor = d[3];
    }

    public MecanumDrivebaseSubsystem(DoubleSupplier gyro, T... d) {
        super(gyro, d);

    }

    @Override
    public void drive(double flSpeed, double frSpeed, double rlSpeed, double rrSpeed) {
        flMotor.setSpeed(flSpeed*getSpeed());
        frMotor.setSpeed(frSpeed*getSpeed());
        rlMotor.setSpeed(rlSpeed*getSpeed());
        rrMotor.setSpeed(rrSpeed*getSpeed());
    }
}
