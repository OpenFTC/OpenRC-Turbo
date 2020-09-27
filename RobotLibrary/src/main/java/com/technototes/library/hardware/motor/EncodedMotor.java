package com.technototes.library.hardware.motor;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.control.PIDFController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.technototes.library.hardware.HardwareDevice;
import com.technototes.library.hardware.PID;
import com.technototes.library.hardware.Sensored;
import com.technototes.library.hardware.sensor.encoder.Encoder;
import com.technototes.library.hardware.sensor.encoder.MotorEncoderSensor;
import com.technototes.library.logging.Log;
import com.technototes.library.util.PIDUtils;

public class EncodedMotor<T extends DcMotor> extends Motor<T> implements Sensored, PID {

    public PIDFController controller;
    public PIDCoefficients coefficients;
    public double threshold = 50;
    private Encoder encoder;

    public EncodedMotor(T d) {
        super(d);
        coefficients = new PIDCoefficients(0, 0, 0);
        PIDFController controller = new PIDFController(coefficients, 0);
        encoder = new MotorEncoderSensor(d);
    }

    public EncodedMotor(HardwareDevice<T> m) {
        super(m.getDevice());
        coefficients = new PIDCoefficients(0, 0, 0);
        controller = new PIDFController(coefficients);
        encoder = new MotorEncoderSensor(device);
    }

    public EncodedMotor(String s) {
        super(s);
        coefficients = new PIDCoefficients(0, 0, 0);
        //controller = new PIDFController(coefficients);
        encoder = new MotorEncoderSensor(device);
    }

    @Override
    public EncodedMotor setInverted(boolean val) {
        device.setDirection(val ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE);
        return this;
    }

    @Override
    public double getSensorValue() {
        return encoder.getSensorValue();
    }

    @Override
    public void setPIDValues(double p, double i, double d) {
        coefficients = new PIDCoefficients(p, i, d);
    }

    @Override
    public boolean setPositionPID(double val) {
        System.out.println("1");
        controller.setTargetPosition(val);
        System.out.println("2");
        setSpeed(controller.update(encoder.getSensorValue()));
        System.out.println("3");
        return isAtPosition(val);
    }

    public boolean setPosition(double ticks) {
        return setPosition(ticks, 0.5);
    }

    public boolean setPosition(double ticks, double speed) {
        if (!isAtPosition(ticks)) {
            setSpeed(getSensorValue() < ticks ? speed : -speed);
        } else {
            setSpeed(0);
            return true;
        }
        return false;
    }

    public boolean isAtPosition(double ticks) {
        return Math.abs(ticks - getSensorValue()) < threshold;
    }

    public void resetEncoder() {
        encoder.zeroEncoder();
    }

    @Override
    @Log
    public double getSpeed() {
        return super.getSpeed();
    }
}
