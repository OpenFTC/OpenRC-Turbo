package com.technototes.library.structure;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public abstract class RobotBase {
    public HardwareMap hardwareMap;
    public Telemetry telemetry;

    public RobotBase(HardwareMap map, Telemetry tel) {
        hardwareMap = map;
        telemetry = tel;
    }
}
