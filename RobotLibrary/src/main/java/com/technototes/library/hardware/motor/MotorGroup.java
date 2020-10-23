package com.technototes.library.hardware.motor;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.technototes.library.hardware.HardwareDeviceGroup;

/** Class for a group of motors
 *
 * @param <T> The type of motors to group
 */
public class MotorGroup<T extends Motor> extends Motor<DcMotor> implements HardwareDeviceGroup<Motor> {
    private Motor[] followers;

    /** Make a motor group
     *
     * @param leader The leader motor
     * @param followers The follower motors
     */
    public MotorGroup(Motor<DcMotor> leader, Motor... followers) {
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


}
