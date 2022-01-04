package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.MovingStatistics;

@TeleOp
public class BulkWriteSpeedTest extends LinearOpMode
{

    MovingStatistics movingStatistics = new MovingStatistics(150);

    @Override
    public void runOpMode() throws InterruptedException
    {
        LynxModule module = hardwareMap.get(LynxModule.class, "Expansion Hub 2");

        telemetry.setMsTransmissionInterval(50);

        waitForStart();
        
        long tRef = System.nanoTime();

        while (opModeIsActive())
        {
            LynxModule.BulkData bulkData = module.getBulkData();

            long now = System.nanoTime();
            movingStatistics.add(now-tRef);
            tRef = now;

            telemetry.addLine(String.format("Average time %.2fms", movingStatistics.getMean()/1000f));
            telemetry.update();
        }
    }
}
