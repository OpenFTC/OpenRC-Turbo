package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="MicroPollutants")
public class MicroPollutants extends LinearOpMode {

    private Servo microServo;
    private DcMotor microPolMotor;
    private ColorSensor colorSensor;
    private DistanceSensor distanceSensor;
    private final int[] rgbaUpper = new int[] {2100, 650, 480, 3500};
    private final int[] rgbaLower = new int[] {2700, 1000, 590, 3900};
    private final double maxDistance = 5.8;
    private final double maxSpeed = 1;
    private final int ticksPerRev = 778;
    private boolean shooting = false;

    @Override
    public void runOpMode() throws InterruptedException {
        colorSensor = hardwareMap.get(ColorSensor.class, "ColorSen");
        distanceSensor = hardwareMap.get(DistanceSensor.class, "ColorSen");
        microPolMotor = hardwareMap.get(DcMotor.class, "MicroPolMotor");
        microPolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        microPolMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        microPolMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        microPolMotor.setTargetPosition(0);
        microPolMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        microPolMotor.setPower(maxSpeed);
        shooting = false;
        waitForStart();
        while (opModeIsActive()) {
            if (checkColor(colorSensor, rgbaUpper, rgbaLower) ||
                distanceSensor.getDistance(DistanceUnit.CM) <= maxDistance) {
//                waitForStart();
                if (!shooting)  { microPolMotor.setTargetPosition(microPolMotor.getCurrentPosition() + ticksPerRev); shooting = true;}
            }
            else if (microPolMotor.getCurrentPosition() - 30 < microPolMotor.getTargetPosition()) {
                microPolMotor.setTargetPosition(microPolMotor.getTargetPosition());
                shooting = false;
            }
            telemetry.addData("Target", microPolMotor.getTargetPosition());
            telemetry.update();
        }
    }

    public static boolean checkColor(ColorSensor colorSensor, int[] rgbaUpper, int[] rgbaLower) {
        int[] rgba = new int[] {colorSensor.red(), colorSensor.green(), colorSensor.blue(), colorSensor.alpha()};
        boolean compareResults = true;
        for (int i = 0; i <= 3; i++) {
            compareResults = (rgbaLower[i] <= rgba[i] && rgba[i] <= rgbaUpper[i]) && compareResults;
        }
        return compareResults;
    }
}
