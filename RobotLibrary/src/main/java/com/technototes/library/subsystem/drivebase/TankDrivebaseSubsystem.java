package com.technototes.library.subsystem.drivebase;

import com.technototes.control.gamepad.GamepadStick;
import com.technototes.library.control.gamepad.old.OldCommandGamepad;
import com.technototes.library.hardware.motor.Motor;

public abstract class TankDrivebaseSubsystem<T extends Motor> extends DrivebaseSubsystem<T> {
    private Motor leftSide, rightSide;

    public TankDrivebaseSubsystem(T l, T r) {
        super(l, r);
        leftSide = l;
        rightSide = r;
    }

    public void arcadeDrive(double s, double t){
        double lp = s+t;
        double rp = -s+t;
        double max = Math.max(lp, rp)*s;
        leftSide.setSpeed(lp);
        rightSide.setSpeed(rp);
    }


    public void arcadeDrive(GamepadStick s) {
        arcadeDrive(s.getXAxis(), s.getYAxis());
    }

    public void tankDrive(double l, double r) {
        leftSide.setSpeedWithScale(l, getScale());
        rightSide.setSpeedWithScale(-r, getScale());
    }
}
