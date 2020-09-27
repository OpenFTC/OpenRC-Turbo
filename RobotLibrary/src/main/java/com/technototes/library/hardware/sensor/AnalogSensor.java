package com.technototes.library.hardware.sensor;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.technototes.library.logging.Log;

public class AnalogSensor extends Sensor<AnalogInput> {

    public AnalogSensor(AnalogInput d) {
        super(d);
    }

    public AnalogSensor(String s) {
        super(s);
    }


    @Override
    @Log
    public double getSensorValue() {
        return device.getMaxVoltage();
    }

}
