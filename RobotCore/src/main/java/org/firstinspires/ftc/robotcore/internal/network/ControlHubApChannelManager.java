package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.SynchronousResultReceiver;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ControlHubApChannelManager implements ApChannelManager
{
    private final static String TAG = "ControlHubApChannelManager";
    private final static boolean DEBUG = false;
    private final static ApChannel FACTORY_DEFAULT_AP_CHANNEL = ApChannel.AUTO_2_4_GHZ;

    private final ChannelResultReceiver channelResultReceiver = new ChannelResultReceiver();
    private final Context context = AppUtil.getDefContext();
    private EnumSet<ApChannel> supportedChannels = null;

    @Override
    public Set<ApChannel> getSupportedChannels()
    {
        if (supportedChannels == null) {
            // We MUST do a copy instead of manipulating the original EnumSet
            EnumSet<ApChannel> result = EnumSet.copyOf(ApChannel.ALL_2_4_GHZ_CHANNELS);

            result.add(ApChannel.AUTO_2_4_GHZ);
            if (AndroidBoard.getInstance().supports5GhzAp()) {
                result.addAll(ApChannel.NON_DFS_5_GHZ_CHANNELS);
                if (AndroidBoard.getInstance().supports5GhzAutoSelection()) {
                    result.add(ApChannel.AUTO_5_GHZ);
                }
            }
            supportedChannels = result; // update atomically
        }
        return supportedChannels;
    }

    /**
     * Returns the current AP channel.
     */
    @Override
    public synchronized ApChannel getCurrentChannel()
    {
        if (AndroidBoard.getInstance().supportsGetChannelInfoIntent()) {
            if (DEBUG) RobotLog.vv(TAG, "Sending broadcast to get current channel");
            Intent getCurrentChannelInfoIntent = new Intent(Intents.ACTION_FTC_AP_GET_CURRENT_CHANNEL_INFO);
            getCurrentChannelInfoIntent.putExtra(Intents.EXTRA_RESULT_RECEIVER, AppUtil.wrapResultReceiverForIpc(channelResultReceiver));
            AppUtil.getDefContext().sendBroadcast(getCurrentChannelInfoIntent);
            try {
                return channelResultReceiver.awaitResult(45, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                RobotLog.ee(TAG, "Thread interrupted while getting current channel from AP service");
                Thread.currentThread().interrupt();
            } catch (TimeoutException e) {
                RobotLog.ee(TAG, "Timeout while getting current channel from AP service");
            }
        }
        return ApChannel.UNKNOWN;
    }

    @Override
    public void setChannel(ApChannel channel, boolean sendChangeToSystem) throws InvalidNetworkSettingException
    {
        if (!getSupportedChannels().contains(channel)) {
            throw new InvalidNetworkSettingException("This device does not support channel " + channel);
        }
        if (sendChangeToSystem) {
            if (AndroidBoard.getInstance().supportsBulkNetworkSettings()) {
                setChannelViaBulkSettingsApi(channel);
            }
            else {
                setChannelViaLegacyApi(channel.channelNum);
            }
        }
    }

    /**
     * Unlike {@link this#setChannelViaLegacyApi(int)}, this method supports sending {@link ApChannel#AUTO_5_GHZ}
     */
    private void setChannelViaBulkSettingsApi(ApChannel channel) throws InvalidNetworkSettingException
    {
        RobotLog.vv(TAG, "Setting channel via bulk Wi-Fi settings API: " + channel.getDisplayName());

        // NetworkConnectionHandler#setNetworkSettings will call setChannel, but it will do so with
        // sendChangeToSystem set to false, which will prevent entrance back into this
        // setChannelViaBulkSettingsApi method.
        NetworkConnectionHandler.getInstance().getNetworkConnection().setNetworkSettings(null, null, channel);
    }

    private void setChannelViaLegacyApi(int ch)
    {
        RobotLog.vv(TAG, "Sending ap channel change intent");
        Intent intent = new Intent(Intents.ACTION_FTC_AP_CHANNEL_CHANGE);
        intent.putExtra(Intents.EXTRA_AP_PREF, ch);
        context.sendBroadcast(intent);
    }

    @Override
    public ApChannel resetChannel(boolean sendChangeToSystem)
    {
        ApChannel defaultChannel = FACTORY_DEFAULT_AP_CHANNEL;
        try {
            setChannel(defaultChannel, sendChangeToSystem);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.ee(TAG, e, "Unable to reset channel to " + defaultChannel);
        }
        return defaultChannel;
    }

    private static class ChannelResultReceiver extends SynchronousResultReceiver<ApChannel> {

        public ChannelResultReceiver()
        {
            super(3, TAG, CallbackLooper.getDefault().getHandler());
        }

        @Override
        @NonNull
        protected ApChannel provideResult(int resultCode, Bundle resultData)
        {
            ApChannel receivedChannel = ApChannel.fromBandAndChannel(
                    resultData.getInt(Intents.BUNDLE_KEY_CURRENT_BAND),
                    resultData.getInt(Intents.BUNDLE_KEY_CURRENT_CHANNEL));
            if (DEBUG) RobotLog.vv(TAG, "Received current channel from AP service: " + receivedChannel.getDisplayName());
            return receivedChannel;
        }
    }
}
