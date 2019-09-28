package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/*
//TODO: Fix servo delayed start
//TODO: Fix straight drive for all modes
TODO: Lift Height For lvl 2
TODO: Automatic servo for Micro
TODO: Automatic shooting for Macro
 */
@TeleOp(name="Drive", group="Drive")
public class Drive extends OpMode {
    //Constants
    private final int ticksPerMicroRev = 778;
    private final int[] rgbaUpper = new int[] {2100, 650, 480, 3500};
    private final int[] rgbaLower = new int[] {2700, 1000, 590, 3900};
    private final double microMaxDistance = 5.8;
    private final double normalSpeed = 0.6;
    private final double slowSpeed = 0.25;
    private final double fastSpeed = 0.9;
    private final double lowerLiftBound = 9;
    private final double upperLiftBound = 36.3;
    private final double liftLockHeight = 20.9;

    //Variables
    private double driveSpeed;
    private double liftHeight;
    private boolean constIntake;
    private boolean straightDrive;

    //Robot States
    private DriveState driveState;
    private MicroState microState;

    //Robot Hardware TODO: Fix motors naming scheme
    //TODO: Change motors to DcMotorEx
    private DcMotorEx rightMotor;
    private DcMotorEx forRight;
    private DcMotorEx leftMotor;
    private DcMotorEx forLeft;
    private DcMotor intakeMotor;
    private DcMotorEx microPolMotor;
    private DcMotorEx macroPolMotor;
    private DcMotorEx liftMotor;
    private Servo liftLock;
    private Servo macroTrigger;
    private Servo microGate;

    private Rev2mDistanceSensor liftDistanceSensor;
    private ColorSensor microColorSensor;
    private DistanceSensor microDistanceSensor;
    private TouchSensor macroMagLimit;

    //Telemetry
    Telemetry.Item teleSpeed;
    Telemetry.Item teleMicroState;
    Telemetry.Item teleLiftHeight;

    @Override
    public void init() {
        driveState = driveState.Normal;
        driveSpeed = normalSpeed;
        constIntake = false;

        //Initialize all motors and Servos
        rightMotor = hardwareMap.get(DcMotorEx.class, "RightMotor");
        forRight = hardwareMap.get(DcMotorEx.class, "ForRight");
        leftMotor = hardwareMap.get(DcMotorEx.class, "LeftMotor");
        forLeft = hardwareMap.get(DcMotorEx.class, "ForLeft");
        intakeMotor = hardwareMap.get(DcMotor.class, "IntakeMotor");
        microPolMotor = hardwareMap.get(DcMotorEx.class, "MicroPolMotor");
        macroPolMotor = hardwareMap.get(DcMotorEx.class, "MacroPolMotor");
        liftMotor = hardwareMap.get(DcMotorEx.class, "LiftMotor");
        liftLock = hardwareMap.get(Servo.class, "LiftLock");
        macroTrigger = hardwareMap.get(Servo.class, "MacroTrigger");
        microGate = hardwareMap.get(Servo.class, "MicroGate");
        //Initialize all sensors
        liftDistanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "LiftDistance");
        microColorSensor = hardwareMap.get(ColorSensor.class, "MicroColorSensor");
        microDistanceSensor = hardwareMap.get(DistanceSensor.class, "MicroColorSensor");
        macroMagLimit = hardwareMap.get(TouchSensor.class, "MacroMagLimit");
        //TODO: Add reverses
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        forRight.setDirection(DcMotorSimple.Direction.REVERSE);
        microPolMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        //TODO: Add brake button
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        forLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        microPolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        microPolMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        microPolMotor.setTargetPosition(0);
        microPolMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        microPolMotor.setPower(0.8);

        //Initialize Servos
        liftLock.setPosition(0);
        macroTrigger.setPosition(0);
        microGate.setPosition(0.1);
        //Initialize Telemetry
        teleSpeed = telemetry.addData("Drive Speed", driveSpeed);
        teleMicroState = telemetry.addData("Micro Pol State", microState);
        teleLiftHeight = telemetry.addData("Lift Height", liftHeight);
    }

    @Override
    public void loop() {
        if (gamepad1.right_trigger)
            driveState = DriveState.Slow;
        else if (gamepad1.left_trigger)
            driveState = DriveState.Fast;
        else driveState = DriveState.Normal;

        if (gamepad1.left_bumper)
            straightDrive = true;
        else straightDrive = false;

        //Sets Drive Speed Based on driveState
        driveSpeed = (driveState == DriveState.Slow) ? slowSpeed :
                        (driveState == DriveState.Fast) ? fastSpeed : normalSpeed;
        if (straightDrive)
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
        if ((lowerLiftBound < liftHeight && gamepad2.left_stick_y > 0) ||
                (liftHeight < upperLiftBound && gamepad2.left_stick_y < 0)) //TODO: Correct sticks and optimize.
            liftMotor.setPower(gamepad2.left_stick_y); //TODO: invert gamepad stick, correct motor direction.
        else liftMotor.setPower(0);

        if (liftHeight < liftLockHeight && gamepad2.left_trigger)
            liftLock.setPosition(1);
        if (gamepad2.x)
            liftLock.setPosition(0);

        if (checkColor(microColorSensor, rgbaUpper, rgbaLower) ||
                microDistanceSensor.getDistance(DistanceUnit.CM) <= microMaxDistance) {
            if (microState != MicroState.Shooting)
                microPolMotor.setTargetPosition(microPolMotor.getCurrentPosition() + ticksPerMicroRev);
            microState = MicroState.Shooting;
        }
        else if (microPolMotor.getCurrentPosition() - 30 < microPolMotor.getTargetPosition()) {
            microPolMotor.setTargetPosition(microPolMotor.getTargetPosition());
            microState = MicroState.Idle;
        }

        if (macroMagLimit.isPressed())
            macroTrigger.setPosition(0.7);
        if (gamepad2.left_stick_button && gamepad2.right_stick_button)
            macroTrigger.setPosition(0);

        if (gamepad2.right_bumper)
            macroPolMotor.setPower(1);
        else if (gamepad2.left_bumper)
            macroPolMotor.setPower(-1);
        else macroPolMotor.setPower(0);

        if (gamepad2.dpad_down)
            microGate.setPosition(0.1);
        else if (gamepad2.dpad_up)
            microGate.setPosition(0.26);

        /*if (gamepad2.dpad_up) microPolMotor.setPower(1);
        else if (gamepad2.dpad_up) microPolMotor.setPower(-1);
        else microPolMotor.setPower(0);*/

        if (gamepad2.a) intakeMotor.setPower(1);
        else if (gamepad2.b) intakeMotor.setPower(-1);
        else intakeMotor.setPower(0);

        teleSpeed.setValue(driveSpeed);
        teleMicroState.setValue(driveState);
        teleLiftHeight.setValue(liftHeight);
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
        Slow
    }

    public enum MicroState {
        Idle,
        Shooting,
        Feeding
    }
}

