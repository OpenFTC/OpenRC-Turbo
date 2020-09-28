package org.firstinspires.ftc.teamcode.newcode.commands.lift;

import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.newcode.subsystems.LiftSubsystem;

public class LiftToBottomCommand extends Command {
    public LiftSubsystem subsystem;
    public int targetHeight;

    public LiftToBottomCommand(LiftSubsystem s) {
        addRequirements(s);
        subsystem = s;
    }

    @Override
    public void init() {
        targetHeight = 0;
    }

    @Override
    public boolean isFinished() {
        return subsystem.goToHeight(targetHeight);
    }

    @Override
    public void end() {
        subsystem.setHeightValue(targetHeight);
    }
}