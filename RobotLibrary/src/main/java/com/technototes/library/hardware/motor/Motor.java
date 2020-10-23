package com.technototes.library.hardware.motor;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;
import com.technototes.library.hardware.Followable;
import com.technototes.library.hardware.HardwareDevice;
import com.technototes.library.hardware.Invertable;
import com.technototes.logger.Log;

/** Class for motors
 * @author Alex Stedman
 * @param <T> The qualcomm hardware device interface
 */
public class Motor<T extends DcMotorSimple> extends HardwareDevice<T> implements Invertable<Motor>, Followable<Motor> {
    /** Create a motor
     *
     * @param device The hardware device
     */
    public Motor(T device) {
        super(device);
    }

    /** Create a motor
     *
     * @param deviceName The device name
     */
    public Motor(String deviceName) {
        super(deviceName);
    }


    @Override
    public boolean getInverted() {
        return getDevice().getDirection() == DcMotorSimple.Direction.FORWARD;
    }

    @Override
    public Motor setInverted(boolean invert) {
        getDevice().setDirection(invert ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE);
        return this;
    }

    @Log
    public double getSpeed() {
        return getDevice().getPower();
    }

    /** Set speed of motor
     *
     * @param speed The speed of the motor
     */
    public void setSpeed(double speed) {
        getDevice().setPower(Range.clip(speed, -1, 1));
    }

    @Override
    public Motor follow(Motor device) {
        return new MotorGroup(device, this);
    }

}
