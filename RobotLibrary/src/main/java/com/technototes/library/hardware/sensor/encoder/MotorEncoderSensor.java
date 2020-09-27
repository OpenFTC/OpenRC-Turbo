package com.technototes.library.hardware.sensor.encoder;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.technototes.library.hardware.sensor.Sensor;
import com.technototes.library.logging.Log;

import java.util.function.IntSupplier;

public class MotorEncoderSensor extends Sensor<DcMotor> implements Encoder {

    private int zero = 0;
    private IntSupplier supplier;

    public MotorEncoderSensor(DcMotor d) {
        super(d);
        supplier = () -> d.getCurrentPosition();
        zeroEncoder();
    }

    @Override
    public void zeroEncoder() {
        zero = (int) getSensorValue();
    }
    @Log
    @Override
    public double getSensorValue() {
        return supplier.getAsInt();
    }
}
