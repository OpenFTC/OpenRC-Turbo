package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp(name="ColorSensTest")
public class ColorSensTest extends LinearOpMode {
    ColorSensor colorSensor;


    @Override
    public void runOpMode() throws InterruptedException {
        colorSensor = hardwareMap.get(ColorSensor.class, "ColorSen");
        waitForStart();

        while(opModeIsActive()) {
            telemetry.addData("A", colorSensor.alpha());
            telemetry.addData("R", colorSensor.red());
            telemetry.addData("G", colorSensor.green());
            telemetry.addData("B", colorSensor.blue());
            telemetry.addData("ARGB", colorSensor.argb());
        }
    }
}