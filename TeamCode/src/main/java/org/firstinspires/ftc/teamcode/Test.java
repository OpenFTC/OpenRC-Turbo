package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.openftc.revextensions2.ExpansionHubEx;
import org.openftc.revextensions2.RevBulkData;


@TeleOp(name="yay")
public class Test extends LinearOpMode {

    ExpansionHubEx expansionHub1;
    RevBulkData bulkData;
    Telemetry.Item totalCur;
    int r,g,b;

    @Override
    public void runOpMode() throws InterruptedException {
        expansionHub1 = hardwareMap.get(ExpansionHubEx.class, "Expansion Hub 1");
        waitForStart();
        resetStartTime();
        expansionHub1.getMotorCurrentDraw(null, 1);
        bulkData = expansionHub1.getBulkInputData();
        r = 0;
        g = 0;
        b = 0;
        totalCur = telemetry.addData("Total Cur", expansionHub1.getTotalModuleCurrentDraw(ExpansionHubEx.CurrentDrawUnits.MILLIAMPS));
        while (!isStopRequested()) {
            for (r = 0; r <= 255; r++) {
                for (g = 0; g <= 255; g++) {
                    for (b = 0; b <= 255; b++) {
                        expansionHub1.setLedColor(r, g, b);
                        totalCur.setValue(expansionHub1.getTotalModuleCurrentDraw(ExpansionHubEx.CurrentDrawUnits.MILLIAMPS);
                        telemetry.update();
                        if (isStopRequested())
                            return;
                    }
                }
            }
        }
    }
}
