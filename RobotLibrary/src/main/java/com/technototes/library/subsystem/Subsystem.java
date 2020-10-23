package com.technototes.library.subsystem;

import com.technototes.library.command.Command;
import com.technototes.library.hardware.HardwareDevice;

public abstract class Subsystem<T extends HardwareDevice> {
    public T[] devices;
    private Command defaultCommand;


    public Subsystem(T... d) {
        devices = d;
    }

    public Subsystem setDefaultCommand(Command command){
        defaultCommand = command;
        return this;
    }

    public T[] getDevices() {
        return devices;
    }

    public Command getDefaultCommand(){
        return defaultCommand;
    }
}
