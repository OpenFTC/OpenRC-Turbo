package org.firstinspires.ftc.teamcode.strafer.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.technototes.library.command.simple.MecanumDriveCommand;
import com.technototes.library.logging.Loggable;
import com.technototes.library.structure.TeleOpCommandOpMode;

import org.firstinspires.ftc.teamcode.strafer.OI;
import org.firstinspires.ftc.teamcode.strafer.Robot;


@TeleOp(name = "Strafer TeleOp")
public class StraferTeleOp extends TeleOpCommandOpMode implements Loggable {
    public OI oi;

    public Robot robot;

    @Override
    public void beginInit() {
        robot = new Robot(hardwareMap, telemetry);
        oi = new OI(driverGamepad, codriverGamepad, robot);
    }


}
