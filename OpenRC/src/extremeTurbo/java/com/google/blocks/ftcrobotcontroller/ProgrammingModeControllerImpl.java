package com.google.blocks.ftcrobotcontroller;

import android.app.Activity;
import android.widget.TextView;

import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.ProgrammingModeController;

import org.firstinspires.ftc.robotserver.internal.programmingmode.ProgrammingModeManager;

public class ProgrammingModeControllerImpl implements ProgrammingModeController {
    public ProgrammingModeControllerImpl(Activity activity, TextView viewById, ProgrammingModeManager programmingModeManager) {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void startProgrammingMode(FtcEventLoopHandler ftcEventLoopHandler) {

    }

    @Override
    public void stopProgrammingMode() {

    }
}
