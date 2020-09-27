package com.technototes.library.subsystem.simple;

import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.subsystem.drivebase.SwerveDrivebaseSubsystem;

public class SimpleSwerveDrivebaseSubsystem extends SwerveDrivebaseSubsystem<Motor> {
    public SimpleSwerveDrivebaseSubsystem(Motor d) {
        super(d);
    }
}
