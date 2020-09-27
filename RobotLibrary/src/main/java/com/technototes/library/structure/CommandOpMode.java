package com.technototes.library.structure;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.HardwareDevice;
import com.technototes.library.logging.Logger;

public abstract class CommandOpMode extends LinearOpMode {
    public static double commandTimeAtEnd = 5;
    public ElapsedTime timer = new ElapsedTime();
    public OpModeState opModeState = OpModeState.INIT;
    public Logger logger;

    public OpModeState getOpModeState() {
        return opModeState;
    }

    @Override
    public final void runOpMode() throws InterruptedException {
        HardwareDevice.hardwareMap = hardwareMap;
        beginInit();
        while (!isStarted()) {
            beginLoop();
            CommandScheduler.getInitInstance().run();
            logger.update();
            telemetry.update();
        }
        CommandScheduler.getInitInstance().runLastTime();
        opModeState = OpModeState.RUN;
        runInit();
        while (opModeIsActive()) {
            runLoop();
            CommandScheduler.getRunInstance().run();
            logger.update();
            telemetry.update();
        }
        CommandScheduler.getRunInstance().runLastTime();
        opModeState = OpModeState.FINISHED;
        end();
        timer.reset();
        CommandScheduler.getEndInstance().run();
        CommandScheduler.getEndInstance().runLastTime();
    }

    //for registering commands to run when robot is in init
    public void beginInit() {

    }

    //for other things to do in init
    public void beginLoop() {

    }

    //to schedule commands to be run
    public void runInit() {

    }

    public void runLoop() {

    }

    public void end() {

    }

    public enum OpModeState {
        INIT, RUN, FINISHED
    }
}
