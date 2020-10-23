package org.firstinspires.ftc.teamcode.strafer;

import com.technototes.library.command.InstantCommand;
import com.technototes.library.control.gamepad.CommandGamepad;
import com.technototes.library.structure.OIBase;
import com.technototes.library.subsystem.drivebase.DrivebaseSubsystem;
import com.technototes.subsystem.DrivebaseSubsystem.DriveSpeed;

public class OI extends OIBase {

    public Robot robot;

    public OI(CommandGamepad g1, CommandGamepad g2, Robot r) {
        super(g1, g2);
        robot = r;
        setDriverControls();
    }

    public void setDriverControls() {
//        CommandScheduler.getRunInstance().schedule(new MecanumDriveCommand(
//           robot.drivebaseSubsystem, driverGamepad.leftStick, driverGamepad.rightStick).setFieldCentric(robot.hardware.imu).addRequirements(robot.drivebaseSubsystem));
        driverGamepad.y.whenToggled(new InstantCommand(() -> robot.drivebaseSubsystem.setDriveSpeed(DriveSpeed.TURBO)))
                .whenInverseToggled(new InstantCommand(() -> robot.drivebaseSubsystem.setDriveSpeed(DriveSpeed.NORMAL)));
    }

}
