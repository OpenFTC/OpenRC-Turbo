package org.firstinspires.ftc.teamcode.newcode.subsystems;

import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.subsystem.motor.EncodedMotorSubsystem;

public class LiftSubsystem extends EncodedMotorSubsystem {
    public double[] liftPositions;
    public int liftHeightIndex = 0;
    public int lastPlacedBrickHeight = 0;

    public LiftSubsystem(EncodedMotor... motors) {
        super(motors);
    }

    public LiftSubsystem(int startingHeight, EncodedMotor... motors) {
        super(motors);
        liftHeightIndex = startingHeight;
    }

    public LiftSubsystem setLiftPositions(double... positions) {
        liftPositions = positions;
        return this;
    }

    public LiftSubsystem setHeightValue(int h) {
        liftHeightIndex = h;
        return this;
    }

    public int getHeight() {
        return liftHeightIndex;
    }

    public boolean goToHeight(int height) {
        return setPosition(liftPositions[height]);
    }
}
