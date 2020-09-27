package com.technototes.library.subsystem.drivebase;

import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.subsystem.servo.Scaleable;

public abstract class DrivebaseSubsystem<T extends Motor> extends Subsystem<T> implements Scaleable {
    public double scale = 1;//DEFAULT
    public Speed driveSpeed = Speed.NORMAL;
    public enum Speed{
        SNAIL(0.2), NORMAL(0.5), TURBO(1);
        public double spe;
        Speed(double s){
            spe = s;
        }
        public double getSpeed(){
            return spe;
        }
    }

    public DrivebaseSubsystem(T... d) {
        super(d);
    }

    public DrivebaseSubsystem(double s, T... d) {
        super(d);
        scale = s;
    }

    public DrivebaseSubsystem setDriveSpeed(Speed s){
        driveSpeed = s;
        return this;
    }

    public void stop() {
        for (Motor m : devices) {
            m.setSpeed(0);
        }
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public void setScale(double s) {
        scale = s;
    }
}
