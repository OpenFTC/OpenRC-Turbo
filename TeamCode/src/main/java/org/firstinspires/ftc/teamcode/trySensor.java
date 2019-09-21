package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "trySensor (Blocks to Java)", group = "")
public class trySensor extends LinearOpMode {

  private DcMotor RightMotor;
  private DcMotor ForRight;
  private DcMotor LeftMotor;
  private DcMotor ForLeft;
  private DcMotor HangingMotor;
  private DistanceSensor DistanceSensor;
  private DcMotor IntakeMotor;
  private Servo Locker;
  private DcMotor MicroPolMotor;
  private Servo Trigger;

  /**
   * This function is executed when this Op Mode is selected from the Driver Station.
   */
  @Override
  public void runOpMode() {
    double Distance;
    double DriveSpeed;
    boolean isStraight;

    RightMotor = hardwareMap.dcMotor.get("RightMotor");
    ForRight = hardwareMap.dcMotor.get("ForRight");
    LeftMotor = hardwareMap.dcMotor.get("LeftMotor");
    ForLeft = hardwareMap.dcMotor.get("ForLeft");
    HangingMotor = hardwareMap.dcMotor.get("HangingMotor");
    DistanceSensor = hardwareMap.get(DistanceSensor.class, "Distance");
    IntakeMotor = hardwareMap.dcMotor.get("IntakeMotor");
    Locker = hardwareMap.servo.get("Locker");
    MicroPolMotor = hardwareMap.dcMotor.get("MicroPolMotor");
    Trigger = hardwareMap.servo.get("Trigger");

    // Put initialization blocks here.
    // You will have to determine which motor to reverse for your robot.
    // In this example, the right motor was reversed so that positive
    // applied power makes it move the robot in the forward direction.
    RightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
    // You will have to determine which motor to reverse for your robot.
    // In this example, the right motor was reversed so that positive
    // applied power makes it move the robot in the forward direction.
    ForRight.setDirection(DcMotorSimple.Direction.REVERSE);
    RightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    LeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    ForRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    ForLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    DriveSpeed = 0.8;
    HangingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    waitForStart();
    if (opModeIsActive()) {
      // Put run blocks here.
      while (opModeIsActive()) {
        if (gamepad1.dpad_right) {
          RightMotor.setPower(1);
          ForRight.setPower(1);
          LeftMotor.setPower(-1);
          ForLeft.setPower(-1);
          sleep(424);
        }
        if (gamepad1.right_trigger) {
          DriveSpeed = 0.4;
        } else {
          DriveSpeed = 0.8;
        }
        if (gamepad1.left_bumper) {
          isStraight = true;
        } else {
          isStraight = false;
        }
        // Put loop blocks here.
        if (isStraight) {
          // The Y axis of a joystick ranges from -1 in its topmost position
          // to +1 in its bottommost position. We negate this value so that
          // the topmost position corresponds to maximum forward power.
          RightMotor.setPower(-(gamepad1.left_stick_y * DriveSpeed));
          ForRight.setPower(-(gamepad1.left_stick_y * DriveSpeed));
          // The Y axis of a joystick ranges from -1 in its topmost position
          // to +1 in its bottommost position. We negate this value so that
          // the topmost position corresponds to maximum forward power.
          LeftMotor.setPower(-(gamepad1.left_stick_y * DriveSpeed));
          ForLeft.setPower(-(gamepad1.left_stick_y * DriveSpeed));
        } else {
          // The Y axis of a joystick ranges from -1 in its topmost position
          // to +1 in its bottommost position. We negate this value so that
          // the topmost position corresponds to maximum forward power.
          RightMotor.setPower(-(gamepad1.right_stick_y * DriveSpeed));
          ForRight.setPower(-(gamepad1.right_stick_y * DriveSpeed));
          // The Y axis of a joystick ranges from -1 in its topmost position
          // to +1 in its bottommost position. We negate this value so that
          // the topmost position corresponds to maximum forward power.
          LeftMotor.setPower(-(gamepad1.left_stick_y * DriveSpeed));
          ForLeft.setPower(-(gamepad1.left_stick_y * DriveSpeed));
        }
        Distance = DistanceSensor.getDistance(DistanceUnit.CM);
        IntakeMotor.setPower(gamepad2.right_stick_y);
        if (Distance > 9 && gamepad2.left_stick_y > 0) {
          HangingMotor.setPower(gamepad2.left_stick_y);
        } else if (Distance < 36.3 && gamepad2.left_stick_y < 0) {
          HangingMotor.setPower(gamepad2.left_stick_y);
        } else {
          HangingMotor.setPower(0);
        }
        if (Distance < 19.7 && gamepad2.left_bumper) {
          Locker.setPosition(1);
        }
        if (gamepad2.x) {
          Locker.setPosition(-1);
        }
        if (gamepad2.left_trigger) {
          MicroPolMotor.setPower(-1);
        } else {
          MicroPolMotor.setPower(0);
        }
        if (gamepad2.b) {
          Trigger.setPosition(-1);
        }
        // Put loop blocks here.
        telemetry.addData("Distance:", Distance);
        telemetry.addData("Left Pow", LeftMotor.getPower());
        telemetry.addData("Right Pow", RightMotor.getPower());
        telemetry.update();
        telemetry.update();
      }
    }
  }
}
