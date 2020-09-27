package com.technototes.library.structure;

import com.technototes.library.control.gamepad.CommandGamepad;

public abstract class OIBase {
    public CommandGamepad driverGamepad, codriverGamepad;

    public OIBase(CommandGamepad g1, CommandGamepad g2) {
        driverGamepad = g1;
        codriverGamepad = g2;
    }

    public abstract void setDriverControls();

    public abstract void setCodriverControls();
}
