package org.firstinspires.ftc.teamcode.simple;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.structure.TeleOpCommandOpMode;
import com.technototes.library.subsystem.simple.SimpleServoSubsystem;
import com.technototes.library.subsystem.simple.SimpleTankDrivebaseSubsystem;

@TeleOp(name = "Simple Teleop")
public class SimpleOpMode extends TeleOpCommandOpMode{
    public SimpleTankDrivebaseSubsystem drivebaseSubsystem;
    public SimpleServoSubsystem gripperSubsystem;
    @Override
    public void beginInit() {
        drivebaseSubsystem = new SimpleTankDrivebaseSubsystem(new Motor<DcMotor>("leftMotor"), new Motor<DcMotor>("rightMotor"));;
        gripperSubsystem = new SimpleServoSubsystem(new Servo("gripper"));
        driverGamepad.a.whenActivated(() -> gripperSubsystem.setPosition(0));
        driverGamepad.b.whenActivated(() -> gripperSubsystem.setPosition(1));
        CommandScheduler.getRunInstance().schedule(() -> drivebaseSubsystem.arcadeDrive(driverGamepad.leftStick));
    }
}
