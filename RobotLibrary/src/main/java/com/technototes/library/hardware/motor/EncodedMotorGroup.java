package com.technototes.library.hardware.motor;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.technototes.library.hardware.HardwareDeviceGroup;
import com.technototes.logger.Log;

/** Class for encoded motor groups
 * @author Alex Stedman
 */
public class EncodedMotorGroup extends EncodedMotor<DcMotor> implements HardwareDeviceGroup<Motor> {
    private Motor[] followers;

    /** Create an encoded motor group
     *
     * @param leader The Lead motor
     * @param followers The following motors
     */
    public EncodedMotorGroup(EncodedMotor<DcMotor> leader, Motor... followers) {
        super(leader.getDevice());
        this.followers = followers;
        for (Motor s : followers) {
            s.follow(leader);
        }
    }

    @Override
    public Motor[] getFollowers() {
        return followers;
    }

    @Override
    public Motor[] getAllDevices() {
        Motor[] m = new Motor[followers.length + 1];
        m[0] = this;
        for (int i = 1; i < m.length; i++) {
            m[i] = followers[i - 1];
        }
        return m;
    }
    @Log
    @Override
    public double getSensorValue() {
        return super.getSensorValue();
    }
}
