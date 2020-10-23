package com.technototes.library.control.gamepad;

import com.technototes.control.gamepad.GamepadButton;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.control.Trigger;

import java.util.function.BooleanSupplier;

/** Class for command buttons for gamepad
 * @author Alex Stedman
 */
public class CommandButton extends GamepadButton implements GamepadTrigger<CommandButton> {
    /** Make command button
     *
     * @param supplier The supplier for the button
     */
    public CommandButton(BooleanSupplier supplier) {
        super(supplier);
    }

    protected CommandButton(){
        super();
    }

    @Override
    public CommandButton getInstance() {
        return this;
    }
}
