package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;


public class HexCoreMotorEncoderTest extends LinearOpMode {

    private DcMotor CoreHex;

    boolean testing = false;

    @Override
    public void runOpMode() throws InterruptedException {
        CoreHex = hardwareMap.get(DcMotor.class, "MicroPolMotor");
        CoreHex.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        CoreHex.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        CoreHex.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        waitForStart();
        while (!isStopRequested()) {
            if (gamepad1.a) {
                testing = true;
                while (testing && !isStopRequested()) {
                    if (gamepad1.b) testing = false;
//                    Telemetry.Item/ it;
//                    it.
                    CoreHex.setTargetPosition(-2000);
                    wait(4000);
                    CoreHex.setTargetPosition(0);
                    wait(4000);
                }
            }
        }
    }
}
