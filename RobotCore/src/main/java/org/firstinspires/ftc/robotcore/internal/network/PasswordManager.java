package org.firstinspires.ftc.robotcore.internal.network;

import android.support.annotation.NonNull;

/**
 * Utility for managing passwords on a device.
 */
public interface PasswordManager {

    /**
     * setPassword
     *
     * Sets the password of the device to the given password.
     */
    boolean setPassword(@NonNull String password);

    /**
     * resetPassword
     *
     * Resets the password to the factory default.
     */
    boolean resetPassword();

    /**
     * isDefault
     *
     * Answers whether or not the password is the factory default.
     */
    boolean isDefault();

    /**
     * getPassword
     *
     * Return the current password of the device
     */
    String getPassword();
}
