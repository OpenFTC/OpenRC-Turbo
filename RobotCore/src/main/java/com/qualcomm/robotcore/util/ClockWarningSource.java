package com.qualcomm.robotcore.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.BuildConfig;
import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.robocol.Heartbeat;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * This class is only used on the Robot Controller
 */
public class ClockWarningSource implements GlobalWarningSource, PeerStatusCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final AppUtil appUtil = AppUtil.getInstance();
    private static final Application context = AppUtil.getDefContext();
    private static final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    private static final String CLOCK_WARNING_ENABLED_PREF = context.getString(R.string.pref_warn_about_incorrect_clocks);
    private static final ClockWarningSource instance = new ClockWarningSource();

    public static ClockWarningSource getInstance() {
        return instance;
    }

    private final boolean examineRcClock = !Device.isRevControlHub(); // The CH always has the time of the connected DS
    private final ZonedDateTime rcBuildTime = ZonedDateTime.parse(BuildConfig.SDK_BUILD_TIME, appUtil.getIso8601DateTimeFormatter());
    private final ElapsedTime timeSinceLastDsClockCheck = new ElapsedTime(0);
    private volatile boolean rcClockIsOlderThanCurrentRelease = false;
    private volatile boolean dsClockIsOlderThanCurrentRelease = false;
    private volatile boolean rcAndDsClocksDifferSignificantly = false;
    private volatile boolean needToCheckDsClock = true;
    @Nullable private volatile String warning;

    // Private constructor (singleton)
    private ClockWarningSource() {
        RobotLog.registerGlobalWarningSource(this);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        onPossibleRcClockUpdate();
    }

    public void onPossibleRcClockUpdate() {
        if (examineRcClock) {
            rcClockIsOlderThanCurrentRelease = ZonedDateTime.now().isBefore(rcBuildTime);
            needToCheckDsClock = true; // Re-check if the clocks are more than an hour apart
            refreshWarning();
        }
    }

    public void onDsHeartbeatReceived(Heartbeat dsHeartbeat) {
        if (needToCheckDsClock && timeSinceLastDsClockCheck.seconds() > 5) {
            timeSinceLastDsClockCheck.reset();

            Instant currentDsTime = Instant.ofEpochMilli(dsHeartbeat.t0);
            dsClockIsOlderThanCurrentRelease = currentDsTime.isBefore(Instant.from(rcBuildTime));
            if (examineRcClock) {
                rcAndDsClocksDifferSignificantly = Math.abs(ChronoUnit.MINUTES.between(currentDsTime, Instant.now())) > 60;
            }

            if (!dsClockIsOlderThanCurrentRelease && !rcAndDsClocksDifferSignificantly) {
                needToCheckDsClock = false;
            }
            refreshWarning();
        }
    }

    @Nullable @Override public String getGlobalWarning() {
        return warning;
    }

    @Override public boolean shouldTriggerWarningSound() {
        return false;
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (CLOCK_WARNING_ENABLED_PREF.equals(key)) {
            refreshWarning();
        }
    }

    @Override public void onPeerDisconnected() {
        needToCheckDsClock = true;
        dsClockIsOlderThanCurrentRelease = false;
        rcAndDsClocksDifferSignificantly = false;
        refreshWarning();
    }

    private void refreshWarning() {
        if (!preferences.getBoolean(CLOCK_WARNING_ENABLED_PREF, true)) {
            warning = null;
            return;
        }

        if (rcClockIsOlderThanCurrentRelease && dsClockIsOlderThanCurrentRelease) {
            warning = context.getString(R.string.warningBothClocksBehind);
        } else if (rcClockIsOlderThanCurrentRelease) {
            warning = context.getString(R.string.warningRcClockBehind);
        } else if (dsClockIsOlderThanCurrentRelease) {
            warning = context.getString(R.string.warningDsClockBehind);
        } else if (rcAndDsClocksDifferSignificantly) {
            warning = context.getString(R.string.warningClocksDiffer);
        } else {
            warning = null;
        }
    }

    @Override public void suppressGlobalWarning(boolean suppress) { }

    @Override public void setGlobalWarning(String warning) { }

    @Override public void clearGlobalWarning() { }

    @Override public void onPeerConnected() { }
}
