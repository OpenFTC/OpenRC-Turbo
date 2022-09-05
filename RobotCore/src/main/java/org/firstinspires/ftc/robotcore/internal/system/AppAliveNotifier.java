/*
Copyright (c) 2020 REV Robotics
All rights reserved.
Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:
Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.
Neither the name of REV Robotics nor the names of its contributors may be used to
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
package org.firstinspires.ftc.robotcore.internal.system;

import android.content.Intent;
import android.os.Debug;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;

/**
 * On Control Hubs running a sufficiently recent OS, this periodically notifies the OS that the
 * Robot Controller app is alive and operating correctly. If the app were to crash or the main event
 * loop thread were to hang, the OS will kill and restart the RC app.
 *
 * All public methods are a no-op when running on an OS that does not support this feature
 *
 * We set the OS timeout value to be pretty high (20 seconds at startup, 10 seconds normally),
 * because the most important thing is that this feature doesn't cause any harm.
 */
public class AppAliveNotifier {
    private static final String TAG = "AppAliveNotifier";
    private static final AppAliveNotifier instance = new AppAliveNotifier();
    private static final Intent ALIVE_NOTIFICATION = new Intent(Intents.ACTION_FTC_NOTIFY_RC_ALIVE);

    //----------------------------------------------------------------------------------------------
    // Configuration
    //----------------------------------------------------------------------------------------------

    // Wait at least this long between sending notifications
    private static final int MIN_NOTIFICATION_PERIOD_MS = 1500;
    // How long we want the OS to wait after receiving a notification before restarting us (a high number avoids harm)
    private static final int OS_TIMEOUT_VALUE_SECONDS = 10;
    // How long the OS timeout should be during app startup
    private static final int APP_STARTUP_OS_TIMEOUT_VALUE_SECONDS = 20;
    private static final int SECONDS_IN_ONE_YEAR = 365 * 24 * 60 * 60;

    public static AppAliveNotifier getInstance() {
        return instance;
    }

    private final boolean enabled;
    private final ElapsedTime timeSinceLastAliveNotification;

    private volatile boolean appFinishedStartup = false;
    private volatile boolean previouslyDetectedDebugger = false;

    private AppAliveNotifier() {
        enabled = AndroidBoard.getInstance().hasRcAppWatchdog();
        timeSinceLastAliveNotification = enabled ? new ElapsedTime() : null;
    }

    public void onAppStartup() {
        if (!enabled) return;
        setOsTimeout(APP_STARTUP_OS_TIMEOUT_VALUE_SECONDS);
        checkForDebugger();
    }

    /**
     * Notify the Control Hub OS that we are still alive
     * <p>
     * This needs to be called on every event loop iteration, and whenever a long-running action
     * occurs on the event loop thread.
     */
    public void notifyAppAlive() {
        if (!enabled) return;
        if (!appFinishedStartup && !previouslyDetectedDebugger) {
            appFinishedStartup = true;
            setOsTimeout(OS_TIMEOUT_VALUE_SECONDS);
        }
        if (timeSinceLastAliveNotification.milliseconds() > MIN_NOTIFICATION_PERIOD_MS) {
            AppUtil.getDefContext().sendBroadcast(ALIVE_NOTIFICATION);
            timeSinceLastAliveNotification.reset();
        }
        checkForDebugger();
    }

    public void disableAppWatchdogUntilNextAppStart() {
        if (!enabled) return;
        // We set the timeout to be a whole year. The next time the RC app launches, the timeout
        // will get reset back to 20 seconds, then 10 seconds.
        setOsTimeout(SECONDS_IN_ONE_YEAR);
    }

    private void checkForDebugger() {
        if (!previouslyDetectedDebugger && (Debug.waitingForDebugger() || Debug.isDebuggerConnected())) {
            RobotLog.ii(TAG, "Debugger detected, setting OS's RC Watchdog timeout to 1 year");
            previouslyDetectedDebugger = true;
            setOsTimeout(SECONDS_IN_ONE_YEAR);
        }
    }

    /**
     * Tell the OS how long we want the timeout to be and let it know that we're alive
     */
    private void setOsTimeout(int seconds) {
        RobotLog.ii(TAG, "Telling the OS to set the RC alive notification timeout to %d seconds", seconds);
        Intent rcAliveNotification = new Intent(Intents.ACTION_FTC_NOTIFY_RC_ALIVE);
        rcAliveNotification.putExtra(Intents.EXTRA_RC_ALIVE_NOTIFICATION_TIMEOUT_SECONDS, seconds);
        AppUtil.getDefContext().sendBroadcast(rcAliveNotification);
        timeSinceLastAliveNotification.reset();
    }
}
