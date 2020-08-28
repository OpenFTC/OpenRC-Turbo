package org.firstinspires.ftc.robotcore.internal.network;

import androidx.annotation.NonNull;

/**
 * Utility for managing passwords on a device.
 */
public interface PasswordManager {

    /**
     * setPassword
     *
     * Sets the password of the device to the given password.
     */
    void setPassword(@NonNull String password, boolean sendChangeToSystem) throws InvalidNetworkSettingException;

    /**
     * resetPassword
     *
     * Resets the password to the factory default.
     */
    String resetPassword(boolean sendChangeToSystem);

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
