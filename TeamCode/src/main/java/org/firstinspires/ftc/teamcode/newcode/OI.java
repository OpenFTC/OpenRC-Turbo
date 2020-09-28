package org.firstinspires.ftc.teamcode.newcode;

import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.InstantCommand;
import com.technototes.library.command.InstantConditionalCommand;
import com.technototes.library.control.gamepad.CommandGamepad;
import com.technototes.library.structure.OIBase;
import com.technototes.library.subsystem.drivebase.DrivebaseSubsystem;
import org.firstinspires.ftc.teamcode.newcode.commands.claw.ClawRotateLeftCommand;
import org.firstinspires.ftc.teamcode.newcode.commands.claw.ClawRotateRightCommand;
import org.firstinspires.ftc.teamcode.newcode.commands.lift.LiftDownCommand;
import org.firstinspires.ftc.teamcode.newcode.commands.lift.LiftToBottomCommand;
import org.firstinspires.ftc.teamcode.newcode.commands.lift.LiftToLastBrickHeightCommand;
import org.firstinspires.ftc.teamcode.newcode.commands.lift.LiftUpCommand;

public class OI extends OIBase {
    public Robot robot;

    public OI(CommandGamepad g1, CommandGamepad g2, Robot r) {
        super(g1, g2);
        robot = r;
        setDriverControls();
        setCodriverControls();
    }

    @Override
    public void setDriverControls() {
        driverGamepad.dpad.down.toggleWhenActivated(new InstantCommand(() -> robot.drivebaseSubsystem.setDriveSpeed((DrivebaseSubsystem.Speed.TURBO))))
                .toggleWhenDeactivated(new InstantCommand(() -> robot.drivebaseSubsystem.setDriveSpeed(DrivebaseSubsystem.Speed.NORMAL)));
        driverGamepad.dpad.up.whenActivated(new InstantCommand(() -> robot.blockFlipperSubsystem.setPosition(0.15)))
                .whenDeactivated(new InstantCommand(() -> robot.blockFlipperSubsystem.setPosition(0.75)));

        driverGamepad.dpad.left.whenActivated(new InstantCommand(() -> robot.capstonePusherSubsystem.setSpeed(-1)))
                .whenDeactivated(new InstantCommand(() -> robot.capstonePusherSubsystem.setSpeed(0)));
        driverGamepad.dpad.right.whenActivated(new InstantCommand(() -> robot.capstonePusherSubsystem.setSpeed(1)))
                .whenDeactivated(new InstantCommand(() -> robot.capstonePusherSubsystem.setSpeed(0)));
    }

    @Override
    public void setCodriverControls() {
        driverGamepad.rtrigger.whenActivated(new InstantCommand(() -> robot.clawSubsystem.setPosition(1)));
        driverGamepad.ltrigger.whenActivated(new InstantCommand(() -> robot.clawSubsystem.setPosition(0)));

        driverGamepad.lbump.whenActivated(new ClawRotateLeftCommand(robot.clawRotateSubsystem));
        driverGamepad.rbump.whenActivated(new ClawRotateRightCommand(robot.clawRotateSubsystem));

//        driverGamepad.back.whenActivated(new InstantCommand(() -> robot.slideSubsytem.setSpeed(0.5)))
//                .whenDeactivated(new InstantCommand(() -> robot.slideSubsytem.setSpeed(0)));
//        driverGamepad.start.whenActivated(new InstantCommand(() -> robot.slideSubsytem.setSpeed(-0.5)))
//                .whenDeactivated(new InstantCommand(() -> robot.slideSubsytem.setSpeed(0)));

        driverGamepad.y.whenActivated(new LiftUpCommand(robot.liftSubsystem));
        driverGamepad.x.whenActivated(new LiftDownCommand(robot.liftSubsystem));
        driverGamepad.a.whenActivated(new LiftToLastBrickHeightCommand(robot.liftSubsystem));
        driverGamepad.b.whenActivated(new LiftToBottomCommand(robot.liftSubsystem));
    }

}
