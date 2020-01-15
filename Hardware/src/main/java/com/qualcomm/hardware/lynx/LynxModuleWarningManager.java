/*
Copyright (c) 2019 Noah Andrews
All rights reserved.
Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:
Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.
Neither the name of Noah Andrews nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.
NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.qualcomm.hardware.lynx;

import android.support.annotation.Nullable;

import com.qualcomm.hardware.R;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Composes system telemetry warnings related to the status of the lynx modules, and logs
 * persistent conditions with configurable throttling.
 */
public class LynxModuleWarningManager {
    private static final LynxModuleWarningManager instance = new LynxModuleWarningManager();

    public static LynxModuleWarningManager getInstance() {
        return instance;
    }

    // Constant fields
    private final static int LOW_BATTERY_STATUS_TIMEOUT_SECONDS = 2;
    private final static int LOW_BATTERY_LOG_FREQUENCY_SECONDS = 2;
    private final static int UNRESPONSIVE_LOG_FREQUENCY_SECONDS = 2;
    private final OpModeManagerNotifier.Notifications opModeNotificationListener = new WarningManagerOpModeListener();
    private final GlobalWarningSource warningSource = new LynxModuleWarningSource();
    private final Object warningMessageLock = new Object();

    // State
    private volatile boolean userOpModeRunning = false;
    private final ConcurrentMap<Integer, UnresponsiveStatus> modulesReportedUnresponsive = new ConcurrentHashMap<>();
    private final Set<Integer> modulesReportedReset = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    private final ConcurrentMap<Integer, LowBatteryStatus> modulesReportedLowBattery = new ConcurrentHashMap<>();
    private String cachedWarningMessage = null; // Protected by warningMessageLock

    public void init(OpModeManagerImpl opModeManager) {
        opModeManager.registerListener(opModeNotificationListener);
        warningSource.clearGlobalWarning();
        RobotLog.registerGlobalWarningSource(warningSource);
    }

    /**
     * Unlike {@link LynxModule#noteNotResponding()}, this method should be called repeatedly while
     * the module is unresponsive. This is so that we can keep track of if the module was ever
     * unresponsive while a user Op Mode was running.
     */
    public void reportModuleUnresponsive(LynxModule module) {
        if (!module.isUserModule() || !module.isOpen) return;
        int moduleNumber = module.getModuleAddress();
        UnresponsiveStatus unresponsiveStatus = modulesReportedUnresponsive.get(moduleNumber);
        if (unresponsiveStatus == null) {
            unresponsiveStatus = new UnresponsiveStatus(module);
            modulesReportedUnresponsive.put(moduleNumber, unresponsiveStatus);
        }
        unresponsiveStatus.reportConditionAndLogWithThrottle(userOpModeRunning);
    }

    public void reportModuleReset(LynxModule module) {
        if (!module.isUserModule()) return;
        int moduleNumber = module.getModuleAddress();
        String logMessage = "REV Hub #%d regained power after a complete power loss.";

        if (userOpModeRunning) { // Module resets while the user is not running an Op Mode are normal
            logMessage += " A user Op Mode was running, so unexpected behavior may occur.";
            boolean newlyReported = modulesReportedReset.add(moduleNumber);
            if (newlyReported) {
                synchronized (warningMessageLock) {
                    cachedWarningMessage = null;
                }
            }
        } else {
            logMessage += " No user Op Mode was running.";
        }
        RobotLog.ww("HubPowerCycle", logMessage, moduleNumber);
    }

    public void reportModuleLowBattery(LynxModule module) {
        if (!module.isUserModule()) return;
        int moduleNumber = module.getModuleAddress();
        LowBatteryStatus lowBatteryStatus = modulesReportedLowBattery.get(moduleNumber);
        if (lowBatteryStatus == null) {
            lowBatteryStatus = new LowBatteryStatus(module);
            modulesReportedLowBattery.put(moduleNumber, lowBatteryStatus);
        }
        lowBatteryStatus.reportConditionAndLogWithThrottle(userOpModeRunning);
    }

    private static abstract class ConditionStatus {
        final ElapsedTime timeSinceConditionLastReported = new ElapsedTime();
        final ElapsedTime timeSinceConditionLogged = new ElapsedTime(0);
        final LynxModuleIntf lynxModule;
        final int logFrequencySeconds;
        boolean conditionPreviouslyTrue = false;
        boolean conditionTrueDuringOpModeRun = false;

        ConditionStatus(LynxModuleIntf lynxModule, int logFrequencySeconds) {
            this.lynxModule = lynxModule;
            this.logFrequencySeconds = logFrequencySeconds;
        }

        abstract boolean conditionCurrentlyTrue();
        abstract void logCondition();

        void reportConditionAndLogWithThrottle(boolean userOpModeRunning) {
            if (userOpModeRunning) conditionTrueDuringOpModeRun = true; // Don't overwrite a true value with false.
            timeSinceConditionLastReported.reset();
            if (timeSinceConditionLogged.seconds() > logFrequencySeconds) {
                logCondition();
                timeSinceConditionLogged.reset();
            }
        }

        final boolean hasChangedSinceLastCheck() {
            boolean conditionCurrentlyTrue = conditionCurrentlyTrue();
            boolean changedSinceLastCheck = conditionCurrentlyTrue != conditionPreviouslyTrue;
            conditionPreviouslyTrue = conditionCurrentlyTrue;
            return changedSinceLastCheck;
        }
    }

    private static class UnresponsiveStatus extends ConditionStatus {
        private UnresponsiveStatus(LynxModuleIntf lynxModule) {
            super(lynxModule, UNRESPONSIVE_LOG_FREQUENCY_SECONDS);
        }
        @Override boolean conditionCurrentlyTrue() {
            return lynxModule.isNotResponding();
        }
        @Override void logCondition() {
            RobotLog.w("REV Hub #%d is currently unresponsive.", lynxModule.getModuleAddress());
        }
    }

    private static class LowBatteryStatus extends ConditionStatus {
        private LowBatteryStatus(LynxModule lynxModule) {
            super(lynxModule, LOW_BATTERY_LOG_FREQUENCY_SECONDS);
        }

        @Override boolean conditionCurrentlyTrue() {
            return timeSinceConditionLastReported.seconds() < LOW_BATTERY_STATUS_TIMEOUT_SECONDS;
        }

        @Override void logCondition() {
            RobotLog.w("REV Hub #%d currently has a battery too low to run motors and servos.");
        }
    }

    private class LynxModuleWarningSource implements GlobalWarningSource {
        private int warningMessageSuppressionCount = 0; // Protected by warningMessageLock

        @Override public String getGlobalWarning() {
            synchronized (warningMessageLock) {
                if (warningMessageSuppressionCount > 0) {
                    return "";
                }
                boolean cacheValid = cachedWarningMessage != null;

                if (cacheValid) {
                    // Check if there is new low battery information
                    for (LowBatteryStatus status : modulesReportedLowBattery.values()) {
                        if (status.hasChangedSinceLastCheck()) {
                            cacheValid = false;
                            break;
                        }
                    }

                    // Check if there is new information about disconnected modules
                    for (UnresponsiveStatus status : modulesReportedUnresponsive.values()) {
                        if (status.hasChangedSinceLastCheck() || !cacheValid) {
                            cacheValid = false;
                            break;
                        }
                    }
                }

                if (!cacheValid) {
                    cachedWarningMessage = composeWarning();
                }
                return cachedWarningMessage;
            }
        }

        private String composeWarning() {
            @Nullable String notRespondingWarning = composeNotRespondingWarning();
            @Nullable String powerIssuesWarning = composePowerIssuesWarning();

            StringBuilder builder = new StringBuilder();
            if (notRespondingWarning != null) {
                builder.append(notRespondingWarning);
                if (powerIssuesWarning != null) builder.append("; ");
            }
            if (powerIssuesWarning != null) {
                builder.append(powerIssuesWarning);
            }
            return builder.toString();
        }

        private @Nullable String composeNotRespondingWarning() {
            if (modulesReportedUnresponsive.size() < 1) return null;

            List<Integer> modulesCurrentlyUnresponsive = new ArrayList<>();
            List<Integer> modulesUnresponsiveDuringOpMode = new ArrayList<>();

            for (Map.Entry<Integer, UnresponsiveStatus> statusEntry : modulesReportedUnresponsive.entrySet()) {
                if (statusEntry.getValue().lynxModule.isNotResponding()) {
                    modulesCurrentlyUnresponsive.add(statusEntry.getKey());
                } else if (statusEntry.getValue().conditionTrueDuringOpModeRun && !modulesReportedReset.contains(statusEntry.getKey())) {
                    // If we know the module reset (lost power) entirely, we don't also need to report that it wasn't responding.
                    modulesUnresponsiveDuringOpMode.add(statusEntry.getKey());
                }
            }

            boolean composedWarning = false;

            StringBuilder builder = new StringBuilder();
            if (modulesCurrentlyUnresponsive.size() > 0) {
                composeModuleList(modulesCurrentlyUnresponsive, builder);
                builder.append(AppUtil.getDefContext().getString(R.string.lynxModuleCurrentlyNotResponding));
                composedWarning = true;
            }

            if (modulesUnresponsiveDuringOpMode.size() > 0) {
                if (modulesCurrentlyUnresponsive.size() > 0) builder.append("; ");
                composeModuleList(modulesUnresponsiveDuringOpMode, builder);
                builder.append(AppUtil.getDefContext().getString(R.string.lynxModulePreviouslyNotResponding));
                composedWarning = true;
            }
            return composedWarning ? builder.toString() : null;
        }

        private @Nullable String composePowerIssuesWarning() {
            if (modulesReportedReset.size() < 1 && modulesReportedLowBattery.size() < 1) return null;

            StringBuilder builder = new StringBuilder();
            boolean powerLossWarningAdded = composePowerLossWarning(builder);
            boolean batteryLowWarningAdded = composeBatteryLowWarning(builder);
            if (powerLossWarningAdded || batteryLowWarningAdded) {
                composePowerIssueTip(userOpModeRunning, builder);
                return builder.toString();
            } else {
                return null;
            }
        }

        // Returns true if power loss warning was added
        private boolean composePowerLossWarning(final StringBuilder builder) {
            if (modulesReportedReset.size() < 1) return false;

            composeModuleList(modulesReportedReset, builder);
            builder.append(AppUtil.getDefContext().getString(R.string.lynxModulePowerLost)).append(" ");
            return true;
        }

        // Returns true if battery low warning was added
        private boolean composeBatteryLowWarning(final StringBuilder builder) {
            if (modulesReportedLowBattery.size() < 1) return false;

            boolean warningAdded = false;
            List<Integer> modulesCurrentlyReporting = new ArrayList<>();
            List<Integer> modulesReportedDuringOpMode = new ArrayList<>();

            for (Map.Entry<Integer, LowBatteryStatus> statusEntry : modulesReportedLowBattery.entrySet()) {
                if (statusEntry.getValue().conditionCurrentlyTrue()) {
                    modulesCurrentlyReporting.add(statusEntry.getKey());
                } else if (statusEntry.getValue().conditionTrueDuringOpModeRun) {
                    modulesReportedDuringOpMode.add(statusEntry.getKey());
                }
            }

            if (modulesCurrentlyReporting.size() > 0) {
                composeModuleList(modulesCurrentlyReporting, builder);
                builder.append(AppUtil.getDefContext().getString(R.string.lynxModuleBatteryIsCurrentlyLow)).append(" ");
                warningAdded = true;
            }

            if (modulesReportedDuringOpMode.size() > 0) {
                composeModuleList(modulesReportedDuringOpMode, builder);
                builder.append(AppUtil.getDefContext().getString(R.string.lynxModuleBatteryWasLow)).append(" ");
                warningAdded = true;
            }
            return warningAdded;
        }

        private void composePowerIssueTip(boolean runningUserOpMode, final StringBuilder builder) {
            if (runningUserOpMode) {
                builder.append(AppUtil.getDefContext().getString(R.string.powerIssueTip));
            } else {
                builder.append(AppUtil.getDefContext().getString(R.string.robotOffTip));
            }
        }

        void composeModuleList(Collection<Integer> moduleNumbers, final StringBuilder builder) {
            builder.append("REV Hub");
            if (moduleNumbers.size() > 1) {
                builder.append("s ");
            } else {
                builder.append(" ");
            }

            Iterator<Integer> iterator = moduleNumbers.iterator();
            while (iterator.hasNext()) {
                builder.append("#").append(iterator.next());
                if (iterator.hasNext()) builder.append(", ");
            }
            builder.append(" ");
        }

        @Override public void suppressGlobalWarning(boolean suppress) {
            synchronized (warningMessageLock) {
                if (suppress)
                    warningMessageSuppressionCount++;
                else
                    warningMessageSuppressionCount--;
            }
        }

        @Override public void clearGlobalWarning() {
            synchronized (warningMessageLock) {
                cachedWarningMessage = null;
                modulesReportedUnresponsive.clear();
                modulesReportedReset.clear();
                modulesReportedLowBattery.clear();
                warningMessageSuppressionCount = 0;
            }
        }

        @Override public void setGlobalWarning(String warning) {
            // Ignore, this class controls its own warning message
        }
    }

    private class WarningManagerOpModeListener implements OpModeManagerNotifier.Notifications {
        @Override public void onOpModePreInit(OpMode opMode) {
            userOpModeRunning = !(opMode instanceof OpModeManagerImpl.DefaultOpMode);
        }

        @Override public void onOpModePreStart(OpMode opMode) { }

        @Override public void onOpModePostStop(OpMode opMode) {
            userOpModeRunning = false;
        }
    }
}
