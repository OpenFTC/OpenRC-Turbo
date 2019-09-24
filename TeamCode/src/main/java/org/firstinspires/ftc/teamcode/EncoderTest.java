package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class EncoderTest extends LinearOpMode {

    private DcMotor rightMotor;
    private DcMotor forRight;
    private DcMotor leftMotor;
    private DcMotor forLeft;

    boolean testing = false;

    @Override
    public void runOpMode() throws InterruptedException {
        rightMotor = hardwareMap.get(DcMotor.class, "RightMotor");
        forRight = hardwareMap.get(DcMotor.class, "ForRight");
        leftMotor = hardwareMap.get(DcMotor.class, "LeftMotor");
        forLeft = hardwareMap.get(DcMotor.class, "ForLeft");
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);
        forRight.setDirection(DcMotor.Direction.REVERSE);
        setMotorsMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorsMode(DcMotor.RunMode.RUN_TO_POSITION);
        waitForStart();
        rightMotor.setPower(0.6);
        forRight.setPower(0.6);
        leftMotor.setPower(0.6);
        forLeft.setPower(0.6);
        while (!isStopRequested()) {
            if (gamepad1.a) {
                testing = true;
                while (testing && !isStopRequested()) {
                    if (gamepad1.b) testing = false;
                    setPosition(2000);
                    wait(4000);
                    setPosition(0);
                    wait(4000);
                }
            }
        }
    }

    public void setMotorsMode(DcMotor.RunMode runMode) {
        rightMotor.setMode(runMode);
        forRight.setMode(runMode);
        leftMotor.setMode(runMode);
        forLeft.setMode(runMode);
    }

    public void setPosition(int position) {
        rightMotor.setTargetPosition(position);
        forRight.setTargetPosition(position);
        leftMotor.setTargetPosition(position);
        forLeft.setTargetPosition(position);
    }
}
