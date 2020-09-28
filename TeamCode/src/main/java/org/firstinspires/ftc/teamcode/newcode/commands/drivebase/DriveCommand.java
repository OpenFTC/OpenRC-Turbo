package org.firstinspires.ftc.teamcode.newcode.commands.drivebase;

import com.technototes.library.command.Command;
import com.technototes.library.subsystem.simple.SimpleMecanumDrivebaseSubsystem;

import java.util.function.DoubleSupplier;

public class DriveCommand extends Command {
    public SimpleMecanumDrivebaseSubsystem subsystem;
    public DoubleSupplier xAxis, yAxis, twist;

    public DriveCommand(SimpleMecanumDrivebaseSubsystem s, DoubleSupplier x, DoubleSupplier y, DoubleSupplier t) {
        addRequirements(s);
        subsystem = s;
        xAxis = x;
        yAxis = y;
        twist = t;
    }

    @Override
    public void execute() {
        subsystem.drive(1, xAxis.getAsDouble(), yAxis.getAsDouble(), twist.getAsDouble());
    }
}
