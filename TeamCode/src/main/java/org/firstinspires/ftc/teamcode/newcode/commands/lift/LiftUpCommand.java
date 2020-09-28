package org.firstinspires.ftc.teamcode.newcode.commands.lift;

import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.newcode.subsystems.LiftSubsystem;

public class LiftUpCommand extends Command {
    public LiftSubsystem subsystem;
    public int targetHeight;

    public LiftUpCommand(LiftSubsystem s) {
        addRequirements(s);
        subsystem = s;
    }

    @Override
    public void init() {
        int temp = subsystem.getHeight() + 1;
        targetHeight = (temp >= 0 && temp < subsystem.liftPositions.length) ?
                temp : subsystem.getHeight();
    }

    @Override
    public boolean isFinished() {

        return subsystem.goToHeight(targetHeight);
    }

    @Override
    public void end() {
        subsystem.setHeightValue(targetHeight);
        subsystem.lastPlacedBrickHeight = targetHeight;
    }
}
