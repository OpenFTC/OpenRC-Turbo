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
        rightMotor = hardwareMap.get(DcMotor.class, "ForRight");
        rightMotor = hardwareMap.get(DcMotor.class, "LeftMotor");
        rightMotor = hardwareMap.get(DcMotor.class, "ForLeft");
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        forRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        forLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);
        forRight.setDirection(DcMotor.Direction.REVERSE);
        setMotorsMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorsMode(DcMotor.RunMode.RUN_TO_POSITION);
        waitForStart();
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
