package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.util.RobotLog;

import androidx.annotation.NonNull;

/**
 * {@link I2cDeviceSynchDeviceWithParameters} adds to {@link I2cDeviceSynchDevice} support for
 * sensors that can be publicly initialized with parameter block of a particular type.
 */
public abstract class I2cDeviceSynchDeviceWithParameters<DEVICE_CLIENT extends I2cDeviceSynchSimple, PARAMETERS>
        extends I2cDeviceSynchDevice<DEVICE_CLIENT>
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    protected @NonNull PARAMETERS parameters;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    protected I2cDeviceSynchDeviceWithParameters(DEVICE_CLIENT deviceClient, boolean isOwned, @NonNull PARAMETERS parameters)
        {
        super(deviceClient, isOwned);
        this.parameters = parameters;
        }

    //----------------------------------------------------------------------------------------------
    // Parameters
    //----------------------------------------------------------------------------------------------

    /**
     * Returns the parameter block currently in use for this sensor
     * @return the parameter block currently in use for this sensor
     */
    @NonNull public PARAMETERS getParameters()
        {
        return this.parameters;
        }

    @Override protected synchronized boolean doInitialize()
        {
        return internalInitialize(this.parameters);
        }

    /**
     * Allows for external initialization with non-default parameters
     * @param parameters the parameters with which the sensor should be initialized
     * @return whether the initialization was successful or not.
     */
    public boolean initialize(@NonNull PARAMETERS parameters)
        {
        this.isInitialized = internalInitialize(parameters);
        if (this.deviceClientIsOwned)
            {
            if (this.isInitialized)
                {
                I2cWarningManager.removeProblemI2cDevice(deviceClient);
                }
            else
                {
                RobotLog.e("Marking I2C device %s %s as unhealthy because initialization failed", getClass().getSimpleName(), getConnectionInfo());
                I2cWarningManager.notifyProblemI2cDevice(deviceClient);
                }
            }
        return this.isInitialized;
        }

    /**
     * Actually attempts to carry out initialization with the indicated parameter block.
     * If successful, said parameter block should be stored in the {@link #parameters}
     * member variable.
     * @param parameters    the parameter block with which to initialize
     * @return              whether initialization was successful or not
     */
    protected abstract boolean internalInitialize(@NonNull PARAMETERS parameters);
    }
