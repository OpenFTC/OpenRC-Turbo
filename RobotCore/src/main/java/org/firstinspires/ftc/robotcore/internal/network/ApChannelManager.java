package org.firstinspires.ftc.robotcore.internal.network;

import java.util.Set;

/**
 * Utility for changing the operating channel for AP mode.
 */
public interface ApChannelManager
{
    /**
     * Gets the set of channels that we are able to broadcast on
     */
    Set<ApChannel> getSupportedChannels();

    /**
     * Gets the channel that we are currently broadcasting on
     */
    ApChannel getCurrentChannel();

    /**
     * setChannel
     *
     * Sets the operating channel of the AP
     */
    void setChannel(ApChannel channel, boolean sendChangeToSystem) throws InvalidNetworkSettingException;

    /**
     * resetChannel
     *
     * Reset the operating channel to the Factory default
     */
    ApChannel resetChannel(boolean sendChangeToSystem);
}
