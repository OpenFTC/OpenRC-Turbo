package org.firstinspires.ftc.teamcode.newcode.commands.claw;

import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.newcode.subsystems.ClawRotateSubsystem;

public class ClawRotateLeftCommand extends Command {
    public ClawRotateSubsystem subsystem;

    public ClawRotateLeftCommand(ClawRotateSubsystem s) {
        subsystem = s;
    }

    @Override
    public void init() {
        switch (subsystem.currentRotation) {
            case LEFT:
                subsystem.setClawRotation(ClawRotateSubsystem.ClawRotation.CENTER);
                break;
            case CENTER:
                subsystem.setClawRotation(ClawRotateSubsystem.ClawRotation.RIGHT);
                break;
        }
    }

    @Override
    public boolean isFinished() {
        return commandRuntime.seconds() > 0.25;
    }
}
