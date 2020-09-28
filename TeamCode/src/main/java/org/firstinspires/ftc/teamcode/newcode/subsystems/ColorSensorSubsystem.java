package org.firstinspires.ftc.teamcode.newcode.subsystems;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.subsystem.sensor.SensorSubsystem;

public class ColorSensorSubsystem extends Subsystem {
    public ColorSensor sensor;
    public ColorSensorSubsystem(ColorSensor s){
        sensor = s;
    }
    public int getRed(){
        return sensor.red();
    }
    public int getGreen(){
        return sensor.green();
    }
    public int getBlue(){
        return sensor.blue();
    }

}
