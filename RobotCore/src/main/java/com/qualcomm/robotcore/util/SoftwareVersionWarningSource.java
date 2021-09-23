package com.qualcomm.robotcore.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.BuildConfig;
import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.PeerDiscovery;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;

import java.util.Arrays;
import java.util.List;

/**
 * This class is only used on the Robot Controller
 */
public class SoftwareVersionWarningSource implements GlobalWarningSource, PeerStatusCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final AppUtil appUtil = AppUtil.getInstance();
    private static final Application context = AppUtil.getDefContext();
    private static final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppUtil.getDefContext());
    private static final SoftwareVersionWarningSource instance = new SoftwareVersionWarningSource();

    private static final String OBSOLETE_SOFTWARE_PREF = context.getString(R.string.pref_warn_about_obsolete_software);
    private static final String MISMATCHED_APPS_PREF = context.getString(R.string.pref_warn_about_mismatched_app_versions);

    private static final List<String> relevantWarningPreferences = Arrays.asList(OBSOLETE_SOFTWARE_PREF, MISMATCHED_APPS_PREF);

    public static SoftwareVersionWarningSource getInstance() {
        return instance;
    }

    private final boolean chOsIsObsolete;
    private volatile boolean rcIsObsolete;
    private volatile boolean needToAnalyzeDsPeerDiscovery = true;
    private volatile boolean dsIsObsolete = false;
    private volatile boolean dhOsIsObsolete = false;
    @Nullable private volatile MismatchedAppsDetail mismatchedAppsDetail = null;
    @Nullable private volatile String warning;

    // Private constructor (singleton)
    private SoftwareVersionWarningSource() {
        RobotLog.registerGlobalWarningSource(this);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        rcIsObsolete = appUtil.localAppIsObsolete();
        chOsIsObsolete = Device.isRevControlHub() && LynxConstants.controlHubOsVersionIsObsolete();

        refreshWarning();
    }

    public void onReceivedPeerDiscoveryFromCurrentPeer(PeerDiscovery peerDiscoveryData) {
        if (!needToAnalyzeDsPeerDiscovery || waitingForTimeFromDs()) { return; }
        needToAnalyzeDsPeerDiscovery = false;

        int dsMajorVersion = peerDiscoveryData.getSdkMajorVersion();
        int dsMinorVersion = peerDiscoveryData.getSdkMinorVersion();
        int rcMajorVersion = BuildConfig.SDK_MAJOR_VERSION;
        int rcMinorVersion = BuildConfig.SDK_MINOR_VERSION;

        YearMonth dsBuildMonth = peerDiscoveryData.getSdkBuildMonth();
        if (appUtil.appIsObsolete(dsBuildMonth)) {
            // Our wall clock indicates that the DS must be obsolete.
            dsIsObsolete = true;
        } else {
            // Another thing that would indicate that either the DS or the RC must be obsolete is if
            // they are from two different FTC seasons.
            Year dsFtcSeason = appUtil.getFtcSeasonYear(dsBuildMonth);
            Year rcFtcSeason = appUtil.getFtcSeasonYear(appUtil.getLocalSdkBuildMonth());
            if (!dsFtcSeason.equals(rcFtcSeason)) {
                if (rcFtcSeason.isBefore(dsFtcSeason)) {
                    rcIsObsolete = true;
                    dsIsObsolete = false;
                } else {
                    dsIsObsolete = true;
                    // Once we know the RC is obsolete, there's nothing that could happen that could
                    // indicate that to be false. For example, if we ever see a DS that is from the
                    // RC's future, then we know that the RC is obsolete, even if the DS goes away.
                    // As a result, we never change rcIsObsolete to be false. Note that we do not
                    // persist this knowledge across app restarts.
                }
            } else {
                dsIsObsolete = false;
            }
        }

        boolean appsAreMismatched = dsMajorVersion != rcMajorVersion || dsMinorVersion != rcMinorVersion;
        if (appsAreMismatched) {
            String oldAppName;
            if (rcMajorVersion == dsMajorVersion) {
                oldAppName = (rcMinorVersion < dsMinorVersion) ? "Robot Controller" : "Driver Station";
            } else {
                oldAppName = (rcMajorVersion < dsMajorVersion) ? "Robot Controller" : "Driver Station";
            }
            mismatchedAppsDetail = new MismatchedAppsDetail(oldAppName);
        } else {
            mismatchedAppsDetail = null;
        }

        refreshWarning();
    }

    public void onReceivedDriverHubOsVersionCode(int dhOsVersionCode) {
        dhOsIsObsolete = dhOsVersionCode < LynxConstants.MINIMUM_LEGAL_DH_OS_VERSION_CODE;
        refreshWarning();
    }

    @Override public String getGlobalWarning() {
        return warning;
    }

    @Override public boolean shouldTriggerWarningSound() {
        return false;
    }

    @Override public void onPeerDisconnected() {
        needToAnalyzeDsPeerDiscovery = true;
        dsIsObsolete = false;
        dhOsIsObsolete = false;
        mismatchedAppsDetail = null;
        refreshWarning();
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (relevantWarningPreferences.contains(key)) {
            // The user has enabled or disabled a warning that we're responsible for, so we need to
            // re-evaluate what our warning message should be.
            refreshWarning();
        }
    }

    private boolean waitingForTimeFromDs() {
        return Device.isRevControlHub() && !appUtil.isSaneWallClockTime(System.currentTimeMillis());
    }

    private void refreshWarning() {
        boolean obsoleteSoftwareWarningEnabled = preferences.getBoolean(OBSOLETE_SOFTWARE_PREF, true);
        boolean versionMismatchWarningEnabled = preferences.getBoolean(MISMATCHED_APPS_PREF, true);
        MismatchedAppsDetail currentMismatchedAppsDetail = this.mismatchedAppsDetail; // Cached so that it can't become null under our nose

        // Only display either an obsolete app(s) warning, or a version mismatch warning, never both.
        String appsWarning = null;
        if ((rcIsObsolete || dsIsObsolete) && obsoleteSoftwareWarningEnabled) {
            if (rcIsObsolete && dsIsObsolete) {
                appsWarning = context.getString(R.string.warningBothAppsObsolete);
            } else if (rcIsObsolete) {
                appsWarning = context.getString(R.string.warningRcAppObsolete);
            } else {
                appsWarning = context.getString(R.string.warningDsAppObsolete);
            }
        } else if (currentMismatchedAppsDetail != null && versionMismatchWarningEnabled) {
            appsWarning = context.getString(R.string.warningMismatchedAppVersions, currentMismatchedAppsDetail.oldApp);
        }

        String chOsWarning = null;
        if (chOsIsObsolete && obsoleteSoftwareWarningEnabled) {
            chOsWarning = context.getString(R.string.warningChOsObsolete, LynxConstants.MINIMUM_LEGAL_CH_OS_VERSION_STRING);
        }

        String dhOsWarning = null;
        if (dhOsIsObsolete && obsoleteSoftwareWarningEnabled) {
            dhOsWarning = context.getString(R.string.warningDhOsObsolete, LynxConstants.MINIMUM_LEGAL_DH_OS_VERSION_STRING);
        }

        this.warning = RobotLog.combineGlobalWarnings(Arrays.asList(appsWarning, chOsWarning, dhOsWarning));
    }

    @Override public void suppressGlobalWarning(boolean suppress) { }

    @Override public void setGlobalWarning(String warning) { }

    @Override public void clearGlobalWarning() { }

    @Override public void onPeerConnected() { }

    private static class MismatchedAppsDetail {
        final String oldApp;

        private MismatchedAppsDetail(String oldApp) {
            this.oldApp = oldApp;
        }
    }
}
