package com.technototes.library.subsystem.drivebase;

import com.qualcomm.robotcore.util.Range;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.util.MathUtils;

import java.util.function.DoubleSupplier;

public abstract class MecanumDrivebaseSubsystem<T extends Motor> extends OmnidirectionalDrivebaseSubsystem<T> {
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

    public void joystickDrive(double x, double y, double rotation, double gyroAngle) {
        double speed = Range.clip(Math.abs(MathUtils.pythag(x, y)), 0, 1);
        double headingRad = Math.toRadians(gyroAngle);
        double angle = -Math.atan2(y, x);

        angle += headingRad - Math.PI / 4;
        x = Math.cos(angle) * speed;
        y = Math.sin(angle) * speed;

        double powerCompY = -(x + y);
        double powerCompX = x - y;

        speed = Range.clip(speed + Math.abs(rotation), 0, 1)*driveSpeed.getSpeed();

        double flPower = powerCompY - powerCompX - rotation;
        double frPower = -powerCompY - powerCompX - rotation;
        double rlPower = powerCompY + powerCompX - rotation;
        double rrPower = -powerCompY + powerCompX - rotation;

        double scale = getSpeedScale(flPower, frPower, rlPower, rrPower);
        flMotor.setSpeed(flPower * speed * scale);
        frMotor.setSpeed(frPower * speed * scale);
        rlMotor.setSpeed(rlPower * speed * scale);
        rrMotor.setSpeed(rrPower * speed * scale);
    }

    private static double getSpeedScale(double fl, double fr, double rl, double rr) {
        // Get the magnitude of the powers for the motors
        double afl = Math.abs(fl);
        double afr = Math.abs(fr);
        double arl = Math.abs(rl);
        double arr = Math.abs(rr);
        double scale = Math.max(Math.max(afl, afr), Math.max(arl, arr));
        // Calculate the value to scale by such that the fastest motor is at either 1 or -1
        return (scale < 1e-4) ? 1.0 : (1.0 / scale);

    }

    @Override
    public void drive(double speed, double angle, double twist) {
        double robotHeadingRad = Math.toRadians(angle);
        double powerCompY = Math.sin(robotHeadingRad);
        double powerCompX = Math.cos(robotHeadingRad);

        double frontLeftSpeed = powerCompY + powerCompX + twist;
        double frontRightSpeed = -powerCompY + powerCompX + twist;
        double rearLeftSpeed = powerCompY - powerCompX + twist;
        double rearRightSpeed = -powerCompY - powerCompX + twist;

        double scale = 1.0 / MathUtils.getMax(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
        flMotor.setSpeedWithScale(frontLeftSpeed, scale);
        frMotor.setSpeedWithScale(frontRightSpeed, scale);
        rlMotor.setSpeedWithScale(rearLeftSpeed, scale);
        rrMotor.setSpeedWithScale(rearRightSpeed, scale);
    }
}
