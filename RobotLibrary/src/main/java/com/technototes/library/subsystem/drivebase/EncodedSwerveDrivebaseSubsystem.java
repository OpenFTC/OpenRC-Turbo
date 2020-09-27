package com.technototes.library.subsystem.drivebase;

import com.technototes.library.hardware.motor.EncodedMotor;

public class EncodedSwerveDrivebaseSubsystem extends SwerveDrivebaseSubsystem<EncodedMotor> {
    public EncodedSwerveDrivebaseSubsystem(EncodedMotor... d) {
        super(d);
    }
}
