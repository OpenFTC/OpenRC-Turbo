package org.firstinspires.ftc.teamcode.newcode.commands.color;

import com.technototes.library.command.Command;
import com.technototes.library.subsystem.drivebase.MecanumDrivebaseSubsystem;
import com.technototes.library.subsystem.drivebase.OmnidirectionalDrivebaseSubsystem;
import org.firstinspires.ftc.teamcode.newcode.subsystems.ColorSensorSubsystem;

public class DriveToLineCommand extends Command {
    public MecanumDrivebaseSubsystem drivebaseSubsystem;
    public ColorSensorSubsystem colorSensorSubsystem;
    public DriveToLineCommand(MecanumDrivebaseSubsystem s, ColorSensorSubsystem s2){
        drivebaseSubsystem = s;
        colorSensorSubsystem = s2;
    }

    @Override
    public void execute() {
        drivebaseSubsystem.drive(0.1, 0, 0);
    }

    @Override
    public boolean isFinished() {
        return true; //over the line
    }

    @Override
    public void end() {
        drivebaseSubsystem.stop();
    }
}
