package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name="Drive", group="Drive")
public class Drive extends OpMode {
    //Constants
    int ticksPerMicroRev = 778;
    private final int[] rgbaUpper = new int[] {2100, 650, 480, 3500};
    private final int[] rgbaLower = new int[] {2700, 1000, 590, 3900};
    private final double maxDistance = 5.8;

    //Variables
    double driveSpeed;

    //Robot Hardware
    private DcMotor rightMotor;
    private DcMotor forRight;
    private DcMotor leftMotor;
    private DcMotor forLeft;
    private DcMotor intakeMotor;
    private DcMotor microPolMotor;

    private ColorSensor microColorSensor;
    private DistanceSensor microDistanceSensor;

    //Telemetry
    Telemetry.Item teleSpeed;


    @Override
    public void init() {
        driveSpeed = 0.8;

        rightMotor = hardwareMap.get(DcMotor.class, "RightMotor");
        forRight = hardwareMap.get(DcMotor.class, "ForRight");
        leftMotor = hardwareMap.get(DcMotor.class, "LeftMotor");
        forLeft = hardwareMap.get(DcMotor.class, "ForLeft");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        microPolMotor = hardwareMap.get(DcMotor.class, "microPolMotor");

        leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        forLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        microPolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        teleSpeed = telemetry.addData("Drive Speed", driveSpeed);

    }

    @Override
    public void loop() {
        rightMotor.setPower(-gamepad1.right_stick_y * driveSpeed);
        forRight.setPower(-gamepad1.right_stick_y * driveSpeed);
        leftMotor.setPower(-gamepad1.left_stick_y * driveSpeed);
        forLeft.setPower(-gamepad1.left_stick_y * driveSpeed);

        if (gamepad1.a) driveSpeed = 0.8;
        else if (gamepad1.b) driveSpeed = 1;

        if (gamepad2.dpad_up) microPolMotor.setPower(1);
        else if (gamepad2.dpad_up) microPolMotor.setPower(-1);
        else microPolMotor.setPower(0);

        if (gamepad2.a) intakeMotor.setPower(1);
        else if (gamepad2.b) intakeMotor.setPower(-1);
        else intakeMotor.setPower(0);

        teleSpeed.setValue(driveSpeed);
        telemetry.update();
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
