package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name="Drive", group="Drive")
public class Drive extends OpMode {
    //Variables
    double driveSpeed;

    //Robot Hardware
    private DcMotor rightMotor;
    private DcMotor forRight;
    private DcMotor leftMotor;
    private DcMotor forLeft;
    private DcMotor intakeMotor;
    private DcMotor microPolMotor;

    //Telemetry
    Telemetry.Item teleSpeed;


    @Override
    public void init() {
        driveSpeed = 0.8;
        rightMotor = hardwareMap.get(DcMotor.class, "RightMotor");
        forRight = hardwareMap.get(DcMotor.class, "ForRight");
        leftMotor = hardwareMap.get(DcMotor.class, "LeftMotor");
        forLeft = hardwareMap.get(DcMotor.class, "ForLeft");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        microPolMotor = hardwareMap.get(DcMotor.class, "microPolMotor");
        teleSpeed = telemetry.addData("Drive Speed", driveSpeed);

    }

    @Override
    public void loop() {

    }
}
