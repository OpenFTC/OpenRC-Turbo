package com.technototes.library.control.gamepad;

import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.control.Trigger;

import java.util.function.BooleanSupplier;

public class ButtonGamepadComponent extends Trigger implements BooleanSupplier {
    protected BooleanSupplier booleanSupplier;
    protected boolean pastStatePress = false, pastStateRelease = false, pastStatetogglePress = false, pastStateToggleRelease = false, togglePress = true, toggleRelease = false;

    public ButtonGamepadComponent(BooleanSupplier b) {
        booleanSupplier = b;
    }

    private boolean uponPressPress() {
        boolean b = false;
        if (getAsBoolean() && (pastStatePress != getAsBoolean())) {
            b = true;
            //System.out.println("press");
        }
        pastStatePress = getAsBoolean();
        return b;
    }

    private boolean uponPressToggleRelease() {
        boolean b = false;
        if (getAsBoolean() && (pastStateToggleRelease != getAsBoolean())) {
            b = true;

        }
        pastStateToggleRelease = getAsBoolean();
        return b;
    }

    private boolean uponPressTogglePress() {
        boolean b = false;
        if (getAsBoolean() && (pastStatetogglePress != getAsBoolean())) {
            b = true;

        }
        pastStatetogglePress = getAsBoolean();
        return b;
    }


    private boolean uponRelease() {
        boolean b = false;
        if (!getAsBoolean() && (pastStateRelease != getAsBoolean())) {
            b = true;
            // System.out.println("release");
        }
        pastStateRelease = getAsBoolean();
        return b;
    }

    @Override
    public ButtonGamepadComponent whenActivated(Command c) {
        CommandScheduler.getRunInstance().schedule(this::uponPressPress, c);
        return this;
    }

    @Override
    public ButtonGamepadComponent whenDeactivated(Command c) {
        CommandScheduler.getRunInstance().schedule(this::uponRelease, c);
        return this;
    }

    @Override
    public ButtonGamepadComponent whileActivated(Command c) {
        CommandScheduler.getRunInstance().schedule(() -> getAsBoolean(), c);
        return this;
    }

    @Override
    public ButtonGamepadComponent whileDeactivated(Command c) {
        CommandScheduler.getRunInstance().schedule(() -> !getAsBoolean(), c);
        return this;
    }

    @Override
    public ButtonGamepadComponent toggleWhenActivated(Command c) {
        CommandScheduler.getRunInstance().schedule(() -> getToggle(), c);
        return this;
    }


    @Override
    public ButtonGamepadComponent toggleWhenDeactivated(Command c) {
        CommandScheduler.getRunInstance().schedule(() -> getInverseToggle(), c);
        return this;
    }

    @Override
    public boolean getAsBoolean() {
        return booleanSupplier.getAsBoolean();
    }

    public boolean getToggle(){
        boolean r = false;
        if(uponPressTogglePress()   ){
            r = togglePress;
            togglePress = !togglePress;
        }
        return r;
    }
    public boolean getInverseToggle(){
        boolean r = false;
        if(uponPressToggleRelease()){
            r = toggleRelease;
            toggleRelease = !toggleRelease;
        }
        return r;
    }

}
