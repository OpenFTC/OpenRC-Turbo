package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

import java.util.EnumSet;
import java.util.Set;

public class WifiDirectChannelManager implements ApChannelManager
{
    private static final String TAG = "WifiDirectChannelManager";
    private EnumSet<ApChannel> supportedChannels = null;

    @Override
    public Set<ApChannel> getSupportedChannels()
    {
        if (supportedChannels == null) {
            // We MUST do a copy instead of manipulating the original EnumSet
            EnumSet<ApChannel> result = EnumSet.copyOf(ApChannel.ALL_2_4_GHZ_CHANNELS);

            result.add(ApChannel.AUTO_2_4_GHZ);
            if (WifiUtil.is5GHzAvailable()) {
            result.add(ApChannel.AUTO_5_GHZ);
            result.addAll(ApChannel.NON_DFS_5_GHZ_CHANNELS);
            }
            supportedChannels = result; // update atomically
        }
        return supportedChannels;
    }

    @Override
    public ApChannel getCurrentChannel()
    {
        // TODO(Noah): Rewrite once we store the ApChannel name to shared prefs.
        int currentChannelNum = new PreferencesHelper(TAG).readInt(AppUtil.getDefContext().getString(R.string.pref_wifip2p_channel), -1);
        if (currentChannelNum == 0) return ApChannel.AUTO_2_4_GHZ;
        int currentBand = (currentChannelNum > 14) ? ApChannel.AP_BAND_5GHZ : ApChannel.AP_BAND_2GHZ;
        return ApChannel.fromBandAndChannel(currentBand, currentChannelNum);
    }

    @Override
    public void setChannel(ApChannel channel, boolean sendChangeToSystem) throws InvalidNetworkSettingException {
        if (!getSupportedChannels().contains(channel)) {
            throw new InvalidNetworkSettingException("This device does not support channel " + channel);
        }
        if (sendChangeToSystem) {
            new WifiDirectChannelChanger().changeToChannel(channel.channelNum);
        }
    }

    @Override
    public ApChannel resetChannel(boolean sendChangeToSystem)
    {
        ApChannel defaultChannel = ApChannel.AUTO_2_4_GHZ;
        try {
            setChannel(defaultChannel, sendChangeToSystem);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.ee(TAG, e, "Unable to reset channel to default");
        }
        return defaultChannel;
    }
}
