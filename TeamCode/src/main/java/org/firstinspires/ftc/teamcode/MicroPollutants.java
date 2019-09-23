package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name="MicroPollutants")
public class MicroPollutants extends LinearOpMode {

    private Servo microServo;
    private DcMotor microPolMotor;
    private ColorSensor colorSensor;

    private boolean working;
    private int x;

    @Override
    public void runOpMode() throws InterruptedException {
        microServo = hardwareMap.get(Servo.class, "microServo");
        microPolMotor = hardwareMap.get(DcMotor.class, "microPolMotor");
        colorSensor = hardwareMap.get(ColorSensor.class, "microPolColor");

        waitForStart();

        while (!isStopRequested()) {
            if (gamepad1.a) {

            }
        }
    }
}
