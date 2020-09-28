package org.firstinspires.ftc.teamcode.newcode.commands.claw;

import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.newcode.subsystems.ClawRotateSubsystem;

public class ClawRotateRightCommand extends Command {
    public ClawRotateSubsystem subsystem;

    public ClawRotateRightCommand(ClawRotateSubsystem s) {
        subsystem = s;
    }

    @Override
    public void init() {
        switch (subsystem.currentRotation) {
            case RIGHT:
                subsystem.setClawRotation(ClawRotateSubsystem.ClawRotation.CENTER);
                break;
            case CENTER:
                subsystem.setClawRotation(ClawRotateSubsystem.ClawRotation.LEFT);
                break;
        }
    }

    @Override
    public boolean isFinished() {
        return commandRuntime.seconds() > 0.25;
    }
}
