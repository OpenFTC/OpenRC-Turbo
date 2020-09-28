package org.firstinspires.ftc.teamcode.newcode.subsystems;

import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.subsystem.servo.ServoSubsystem;

public class ClawRotateSubsystem extends ServoSubsystem {
    public ClawRotation currentRotation;
    public Servo servo;

    public ClawRotateSubsystem(Servo s) {
        super(s);
        servo = s;
        currentRotation = ClawRotation.CENTER;
    }

    public enum ClawRotation {
        LEFT(0), CENTER(0.5), RIGHT(1);
        public double pos;

        ClawRotation(double servoPos) {
            pos = servoPos;
        }
    }

    public void setClawRotation(ClawRotation c) {
        currentRotation = c;
        servo.setPosition(c.pos);
    }
}
