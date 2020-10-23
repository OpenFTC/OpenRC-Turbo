package com.technototes.library.control.gamepad;

import com.technototes.control.gamepad.GamepadButton;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.control.Trigger;

import java.util.function.BooleanSupplier;

/** Class for gamepad-command integration
 * @author Alex Stedman
 * @param <T> The type of Gamepad Button
 */
public interface GamepadTrigger<T extends GamepadButton> extends Trigger<T> {

    @Override
    default T whenActivated(Command command){
        return schedule(getInstance()::isJustActivated, command);
    }

    @Override
    default T whenDeactivated(Command command){
        return schedule(getInstance()::isDeactivated, command);
    }

    @Override
    default T whileActivated(Command command){
        return schedule(getInstance()::isActivated, command);
    }

    @Override
    default T whileDeactivated(Command command){
        return schedule(getInstance()::isDeactivated, command);
    }

    @Override
    default T whenToggled(Command command){
        return schedule(getInstance()::isJustToggled, command);
    }

    @Override
    default T whenInverseToggled(Command command){
        return schedule(getInstance()::isJustInverseToggled, command);
    }

    @Override
    default T whileToggled(Command command){
        return schedule(getInstance()::isToggled, command);
    }

    @Override
    default T whileInverseToggled(Command command){
        return schedule(getInstance()::isInverseToggled, command);
    }

    /** Return instance of class parameter
     *
     * @return The instance
     */
    T getInstance();

    /** Schedule the commands
     *
     * @param condition The condition
     * @param command The command to schedule
     * @return The instance
     */
    default T schedule(BooleanSupplier condition, Command command){
        CommandScheduler.getRunInstance().schedule(condition, command);
        return getInstance();
    }

}
