package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.openftc.revextensions2.ExpansionHubEx;
import org.openftc.revextensions2.RevBulkData;


@TeleOp(name="yay")
public class Test extends LinearOpMode {

    RevTouchSensor magnet;
    ExpansionHubEx expansionHub1;
    RevBulkData bulkData;
    Telemetry.Item totalCur;
    Telemetry.Item total;
    int r,g,b;

    @Override
    public void runOpMode() throws InterruptedException {
        magnet = hardwareMap.get(RevTouchSensor.class, "gh");
        expansionHub1 = hardwareMap.get(ExpansionHubEx.class, "Expansion Hub 1");
        waitForStart();
        resetStartTime();
        expansionHub1.getMotorCurrentDraw(null, 1);
        r = 0;
        g = 0;
        b = 0;
        totalCur = telemetry.addData("is", magnet.isPressed());
        total = telemetry.addData("val" , magnet.getValue());
        while (!isStopRequested()) {
            expansionHub1.setLedColor(0,0,0);
            while (getRuntime() > 500) {}
            expansionHub1.setLedColor(0,0,255);
            while (getRuntime() > 500) {}
            expansionHub1.setLedColor(0,255,0);
            while (getRuntime() > 500) {}
            expansionHub1.setLedColor(255,0,0);
            while (getRuntime() > 500) {}
            expansionHub1.setLedColor(255,255,255);
            totalCur.setValue(magnet.isPressed());
            total.setValue(magnet.getValue());
            telemetry.update();
        }
    }
}
