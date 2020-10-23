package com.technototes.library.hardware.sensor.encoder;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.technototes.library.hardware.sensor.Sensor;
@Deprecated
public class ExternalEncoder extends Sensor<AnalogInput> implements Encoder {
    public ExternalEncoder(AnalogInput device) {
        super(device);
    }
    public ExternalEncoder(String deviceName) {
        super(deviceName);
    }


    @Override
    public void zeroEncoder() {

    }

    @Override
    public double getSensorValue() {
        return 0;
    }
}
