package org.firstinspires.ftc.teamcode.newcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.sensor.IMU;
import com.technototes.library.hardware.sensor.RangeSensor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logging.Log;
import com.technototes.library.logging.Loggable;
import com.technototes.library.structure.HardwareBase;

public class Hardware extends HardwareBase implements Loggable {

    public HardwareMap hardwareMap;


    //upper assembly
    @Log.Number(name = "slide")
    public Motor<CRServo> slide;
    @Log.NumberBar(name = "turn", min = 0)
    public Servo turn;
    @Log.NumberSlider(name = "claw", min = 0)
    public Servo claw;

    //capstone pusher
    public Motor<CRServo> cap;

    //rear flipper
    public Servo blockFlipper;

    //color sensor
    public ColorSensor sensorColorBottom;

    //range sensors
    public RangeSensor sensorRangeFront;
    public RangeSensor sensorRangeRear;
    public RangeSensor sensorRangeLeft;
    public RangeSensor sensorRangeRight;

    //lift
    public EncodedMotor<DcMotor> lLiftMotor;
    public EncodedMotor<DcMotor> rLiftMotor;

    //drivebase
    public Motor<DcMotor> flMotor;
    public Motor<DcMotor> frMotor;
    public Motor<DcMotor> rlMotor;
    public Motor<DcMotor> rrMotor;

    public IMU imu;

    public Hardware(HardwareMap map) {
        hardwareMap = map;

        slide = new Motor<CRServo>("slide");

        turn = new Servo("grabTurn");
        claw = new Servo("claw").setRange(0, 0.7);

        cap = new Motor<CRServo>("cap");

        blockFlipper = new Servo("blockFlipper");

        sensorColorBottom = hardwareMap.get(ColorSensor.class, "sensorColorBottom");

        sensorRangeFront = new RangeSensor("sensorRangeFront");
        sensorRangeRear = new RangeSensor("sensorRangeRear");
        sensorRangeLeft = new RangeSensor("sensorRangeLeft");
        sensorRangeRight = new RangeSensor("sensorRangeRight");

        lLiftMotor = new EncodedMotor<DcMotor>("motorLiftLeft").setInverted(false);
        rLiftMotor = new EncodedMotor<DcMotor>("motorLiftRight").setInverted(true);

        //lLiftMotor.setPIDValues(0.1, 0,0);
        //rLiftMotor.setPIDValues(0.1, 0,0);

        flMotor = new Motor<DcMotor>("motorFrontLeft");
        frMotor = new Motor<DcMotor>("motorFrontRight");
        rlMotor = new Motor<DcMotor>("motorRearLeft");
        rrMotor = new Motor<DcMotor>("motorRearRight");

        imu = new IMU("imu1");


    }


}
