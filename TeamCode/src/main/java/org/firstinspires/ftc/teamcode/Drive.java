package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Dictionary;
import java.util.concurrent.locks.Lock;

@TeleOp(name="Drive", group="Drive")
public class Drive extends OpMode {
    //Constants
    int ticksPerMicroRev = 778;
    private final int[] rgbaUpper = new int[] {2100, 650, 480, 3500};
    private final int[] rgbaLower = new int[] {2700, 1000, 590, 3900};
    private final double microMaxDistance = 5.8;
    private final double normalSpeed = 0.6;
    private final double slowSpeed = 0.25;
    private final double fastSpeed = 0.9;
    private final double lowerLiftBound = 9;
    private final double upperLiftBound = 36.3;
    private final double liftLockPosition = 20.9;

    //Variables
    private double driveSpeed;
    private DriveState driveState;
    private double liftHeight;
    private boolean constIntake;

    //Robot Hardware TODO: Fix motors naming scheme
    //TODO: Change motors to DcMotorEx
    private DcMotor rightMotor;
    private DcMotor forRight;
    private DcMotor leftMotor;
    private DcMotor forLeft;
    private DcMotor intakeMotor;
    private DcMotor microPolMotor;
    private DcMotor macroPolMotor;
    private DcMotor hangingMotor;
    private Servo Locker;
    private Servo Trigger;
    private Servo microMover;

    private Rev2mDistanceSensor liftDistanceSensor;
    private ColorSensor microColorSensor;
    private DistanceSensor microDistanceSensor;
    private TouchSensor macroMagLimit;
    //TODO: Add Mag-limit and code.

    //Telemetry
    Telemetry.Item teleSpeed;


    @Override
    public void init() {
        driveState = driveState.Normal;
        driveSpeed = normalSpeed;
        constIntake = false;

        //TODO: Fix Motors to correct names
        rightMotor = hardwareMap.get(DcMotor.class, "RightMotor");
        forRight = hardwareMap.get(DcMotor.class, "ForRight");
        leftMotor = hardwareMap.get(DcMotor.class, "LeftMotor");
        forLeft = hardwareMap.get(DcMotor.class, "ForLeft");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        microPolMotor = hardwareMap.get(DcMotor.class, "microPolMotor");
        macroPolMotor = hardwareMap.get(DcMotor.class, "macroPolMotor");
        hangingMotor = hardwareMap.get(DcMotor.class, "hangingMotor");
        Locker = hardwareMap.get(Servo.class, "Locker");
        Trigger = hardwareMap.get(Servo.class, "Trigger");
        microMover = hardwareMap.get(Servo.class, "microMover");
        //TODO: Add reverses
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        forRight.setDirection(DcMotorSimple.Direction.REVERSE);
        microPolMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        //TODO: Add brake button.
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        forRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        forLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        microPolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        microPolMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        microPolMotor.setTargetPosition(0);
        microPolMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        microPolMotor.setPower(0.8);
        teleSpeed = telemetry.addData("Drive Speed", driveSpeed);

    }

    @Override
    public void loop() {
        if (gamepad1.left_bumper && gamepad1.right_trigger)
            driveState = DriveState.StraightSlow;
        else if (gamepad1.right_trigger)
            driveState = DriveState.Slow;
        else if (gamepad1.left_trigger)
            driveState = DriveState.Fast;
        else if (gamepad1.left_bumper)
            driveState = DriveState.StraightNormal;
        else driveState = DriveState.Normal;

        //Sets Drive Speed Based on driveState
        driveSpeed = (driveState == DriveState.Slow || driveState == DriveState.StraightSlow)
                ? slowSpeed : (driveState == DriveState.Fast) ? fastSpeed : normalSpeed;
        if (driveState == DriveState.StraightNormal || driveState == DriveState.StraightSlow)
            setMotor(-gamepad1.left_stick_y, -gamepad1.left_stick_y, driveSpeed);  //Temp code
        else
            setMotor(-gamepad1.left_stick_y, -gamepad1.right_stick_y, driveSpeed); //Temp code

        //Activate constant intake if right_stick_y is bigger than 0.8 and right_bumper is pressed
        constIntake = (0.8 < gamepad2.right_stick_y && gamepad2.right_bumper) || constIntake;
        //Disable constant intake if right_stick_y is under -0.5
        constIntake = (!(gamepad2.right_stick_y < -0.5)) && constIntake;
        //Sets motor based on constant intake or right_stick_y
        intakeMotor.setPower((constIntake) ? 1 : gamepad2.right_stick_y);

        liftHeight = liftDistanceSensor.getDistance(DistanceUnit.CM); //Get distance from liftSensor

        //Limit lift to the lift's bounds
        if (lowerLiftBound < liftHeight || liftHeight < upperLiftBound)
            hangingMotor.setPower(gamepad2.left_stick_y); //TODO: invert gamepad stick, correct motor direction.
        else hangingMotor.setPower(0);

        if (liftHeight < 20.9 && gamepad2.left_trigger)
            Locker.setPosition(1);
        if (gamepad2.x)
            Locker.setPosition(0);



        if (macroMagLimit.isPressed())
            Trigger.setPosition(0.5);
        if (gamepad2.left_stick_button && gamepad2.right_stick_button)
            Trigger.setPosition(0);

        if (gamepad2.right_bumper)
            macroPolMotor.setPower(1);
        else if (gamepad2.left_bumper)
            macroPolMotor.setPower(-1);
        else macroPolMotor.setPower(0);

        if (gamepad2.dpad_down)
            microMover.setPosition(0.1);
        else if (gamepad2.dpad_up)
            microMover.setPosition(0.26);




        /*if (gamepad2.dpad_up) microPolMotor.setPower(1);
        else if (gamepad2.dpad_up) microPolMotor.setPower(-1);
        else microPolMotor.setPower(0);*/

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

    //TODO: Make possibly make static, make the function take in a array of motors.
    //More: Passing through objects in java passes pointer to memory, as long as we don't = a object it stays the reference.
    //https://stackoverflow.com/a/40523/6122159
    public void setMotor(double leftStick, double rightStick, double multiplier) { //Temporary function!
        rightMotor.setPower(rightStick * multiplier);
        forRight.setPower(rightStick * multiplier);
        leftMotor.setPower(leftStick * multiplier);
        forLeft.setPower(leftStick * multiplier);
    }

    //Enums / States

    public enum DriveState {
        Normal,
        Fast,
        Slow,
        StraightNormal,
        StraightSlow
    }
}

