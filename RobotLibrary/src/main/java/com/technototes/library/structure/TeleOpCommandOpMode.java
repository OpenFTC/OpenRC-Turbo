package com.technototes.library.structure;

import com.technototes.library.control.gamepad.CommandGamepad;

public abstract class TeleOpCommandOpMode extends CommandOpMode {
    public CommandGamepad driverGamepad = new CommandGamepad(gamepad1);
    public CommandGamepad codriverGamepad = new CommandGamepad(gamepad2);
}
