package org.firstinspires.ftc.teamcode.newcode.commands.claw;

import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.newcode.subsystems.ClawRotateSubsystem;

public class ClawCenterCommand extends Command {
    public ClawRotateSubsystem subsystem;

    public ClawCenterCommand(ClawRotateSubsystem s) {
        subsystem = s;
    }

    @Override
    public void execute() {
        subsystem.setClawRotation(ClawRotateSubsystem.ClawRotation.CENTER);
    }
}
