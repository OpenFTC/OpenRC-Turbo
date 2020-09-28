package org.firstinspires.ftc.teamcode.newcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.logging.Log;
import com.technototes.library.logging.Loggable;
import com.technototes.library.structure.RobotBase;
import com.technototes.library.subsystem.simple.SimpleMecanumDrivebaseSubsystem;
import com.technototes.library.subsystem.simple.SimpleMotorSubsystem;
import com.technototes.library.subsystem.simple.SimpleServoSubsystem;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.newcode.subsystems.ClawRotateSubsystem;
import org.firstinspires.ftc.teamcode.newcode.subsystems.LiftSubsystem;

public class Robot extends RobotBase implements Loggable{


    public Hardware hardware;


    //drivebase
    public SimpleMecanumDrivebaseSubsystem drivebaseSubsystem;

    //lift
    public LiftSubsystem liftSubsystem;

    //slide
    public SimpleMotorSubsystem slideSubsytem;

    //claw
    public ClawRotateSubsystem clawRotateSubsystem;
    public SimpleServoSubsystem clawSubsystem;

    //capstone
    public SimpleMotorSubsystem capstonePusherSubsystem;

    //block flipper
    public SimpleServoSubsystem blockFlipperSubsystem;

    public Robot(HardwareMap map, Telemetry tel) {
        super(map, tel);

        hardware = new Hardware(map);

        drivebaseSubsystem = new SimpleMecanumDrivebaseSubsystem(
                hardware.flMotor, hardware.frMotor, hardware.rlMotor, hardware.rrMotor);

        liftSubsystem = new LiftSubsystem(hardware.lLiftMotor, hardware.rLiftMotor).setLiftPositions(0, 500, 1000, 1500, 2000, 2500, 3000);

        slideSubsytem = new SimpleMotorSubsystem(hardware.slide);

        clawRotateSubsystem = new ClawRotateSubsystem(hardware.turn);

        clawSubsystem = new SimpleServoSubsystem(hardware.claw);

        capstonePusherSubsystem = new SimpleMotorSubsystem(hardware.cap);

        blockFlipperSubsystem = new SimpleServoSubsystem(hardware.blockFlipper);
    }


}
