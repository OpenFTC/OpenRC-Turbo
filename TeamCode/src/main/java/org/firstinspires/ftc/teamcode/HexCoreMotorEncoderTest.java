package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.State;
import java.lang.annotation.Target;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name= "HexCoreMotorEncoder")
public class HexCoreMotorEncoderTest extends LinearOpMode {

    private DcMotor CoreHex;
    double time;

    @Override
    public void runOpMode() throws InterruptedException {
        CoreHex = hardwareMap.get(DcMotor.class, "MicroPolMotor");
        CoreHex.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        CoreHex.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        CoreHex.setTargetPosition(0);
        CoreHex.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        CoreHex.setPower(0.5);
        waitForStart();
        while (!isStopRequested()) {
            if (gamepad1.a) {
                CoreHex.setTargetPosition(-777);
            }
            else if (gamepad1.b) {
                CoreHex.setTargetPosition(0);
            }
        }
    }
}