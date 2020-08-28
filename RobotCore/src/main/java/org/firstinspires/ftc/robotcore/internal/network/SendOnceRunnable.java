package org.firstinspires.ftc.robotcore.internal.network;

import android.os.SystemClock;
import androidx.annotation.NonNull;

import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.KeepAlive;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.RobotCoreGamepadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles batch-sending certain data to the remote device at a regular interval
 */
@SuppressWarnings("WeakerAccess")
public class SendOnceRunnable implements Runnable {

    //----------------------------------------------------------------------------------------------
    // Types
    //----------------------------------------------------------------------------------------------

    public interface DisconnectionCallback {
        /**
         * Will be called periodically while the peer is disconnected
         */
        void disconnected();
    }

    public static class Parameters {
        public boolean                          disconnectOnTimeout = true;
        public boolean                          originateHeartbeats = AppUtil.getInstance().isDriverStation();
        public boolean                          originateKeepAlives = false;
        public volatile RobotCoreGamepadManager gamepadManager      = null;

        public Parameters() { }
    }

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
    public static final int             MS_BATCH_TRANSMISSION_INTERVAL = 40;
    public static final String          TAG = RobocolDatagram.TAG;
    public static       boolean         DEBUG = false;

    public static final double          ASSUME_DISCONNECT_TIMER = 2.0; // in seconds
    public static final int             MAX_COMMAND_ATTEMPTS = 10;
    public static final long            GAMEPAD_UPDATE_THRESHOLD = 1000; // in milliseconds
    public static final int             MS_HEARTBEAT_TRANSMISSION_INTERVAL = 100;
    public static final int             MS_KEEPALIVE_TRANSMISSION_INTERVAL = 20;


    @NonNull protected final ElapsedTime                lastRecvPacket;
    @NonNull protected volatile List<Command>           pendingCommands = new CopyOnWriteArrayList<Command>();
    @NonNull protected Heartbeat                        heartbeatSend = new Heartbeat();
    @NonNull protected KeepAlive                        keepAliveSend = new KeepAlive();
    @NonNull protected DisconnectionCallback            disconnectionCallback;
    @NonNull protected final Parameters                 parameters;
    @NonNull protected final AppUtil                    appUtil = AppUtil.getInstance();

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public SendOnceRunnable(@NonNull  DisconnectionCallback disconnectionCallback,
                            @NonNull  ElapsedTime lastRecvPacket) {
        this.disconnectionCallback  = disconnectionCallback;
        this.lastRecvPacket         = lastRecvPacket;
        this.parameters             = new Parameters();

        RobotLog.vv(TAG, "SendOnceRunnable created");
    }

    //----------------------------------------------------------------------------------------------
    // Operations
    //----------------------------------------------------------------------------------------------

    @Override
    public void run() {
        // We can't initialize this in the constructor, because SendOnceRunnable is instantiated during NetworkConnectionHandler construction.
        final NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();

        boolean sentPacket;
        try {
            // Determine if we're still connected to our peer.
            // It might make more sense for this to live elsewhere, but for now it's a convenient
            // place to continue to leave it.
            double seconds = lastRecvPacket.seconds();
            if (parameters.disconnectOnTimeout && seconds > ASSUME_DISCONNECT_TIMER) {
                disconnectionCallback.disconnected();
                return;
            }

            /*
             * If we are on the DriverStation then send heartbeats at a specific rate.  Heartbeats
             * originate on the DriverStation and are echoed by the RobotController.
             *
             * If we have fresh GamePad data from the DriverStation, then send it.
             *
             * If, through this invocation of SendOnceRunnable we sent neither GamePad data, nor
             * a Heartbeat, then send a KeepAlive if configured to do so.  This ensures a minimum
             * packet rate for devices for which this is necessary to prevent disconnects.
             */
            sentPacket = false;
            if (parameters.originateHeartbeats && heartbeatSend.getElapsedSeconds() > 0.001 * MS_HEARTBEAT_TRANSMISSION_INTERVAL) {
                // generate a new heartbeat packet and send it
                heartbeatSend = Heartbeat.createWithTimeStamp();
                // Add the timezone in there too!
                heartbeatSend.setTimeZoneId(TimeZone.getDefault().getID());
                // keep the next two lines as close together in time as possible in order to improve the quality of time synchronization
                heartbeatSend.t0 = appUtil.getWallClockTime();
                networkConnectionHandler.sendDataToPeer(heartbeatSend);
                sentPacket = true;
                // Do any logging after the transmission so as to minimize disruption of timing calculation
            }

            if (parameters.gamepadManager != null) {
                long now = SystemClock.uptimeMillis();

                for (Gamepad gamepad : parameters.gamepadManager.getGamepadsForTransmission()) {

                    // don't send stale gamepads
                    if (now - gamepad.timestamp > GAMEPAD_UPDATE_THRESHOLD && gamepad.atRest())
                        continue;

                    gamepad.setSequenceNumber();
                    networkConnectionHandler.sendDataToPeer(gamepad);
                    sentPacket = true;
                }
            }

            if ((!sentPacket) && (parameters.originateKeepAlives) && (keepAliveSend.getElapsedSeconds() > 0.001 * MS_KEEPALIVE_TRANSMISSION_INTERVAL)) {
                keepAliveSend = KeepAlive.createWithTimeStamp();
                networkConnectionHandler.sendDataToPeer(keepAliveSend);
            }

            long nanotimeNow = System.nanoTime();

            // send commands
            List<Command> commandsToRemove = new ArrayList<Command>();
            for (Command command : pendingCommands) {

                // if this command has exceeded max attempts or is no longer worth transmitting, give up
                if (command.getAttempts() > MAX_COMMAND_ATTEMPTS || command.hasExpired()) {
                    String msg = String.format(AppUtil.getDefContext().getString(R.string.configGivingUpOnCommand), command.getName(), command.getSequenceNumber(), command.getAttempts());
                    RobotLog.vv(TAG, msg);
                    commandsToRemove.add(command);
                    continue;
                }

                // Commands that we originate we only send out every once in a while so as to give ack's a chance to get back to us
                if (command.isAcknowledged() || command.shouldTransmit(nanotimeNow)) {
                    // log commands we initiated, ack the ones we didn't
                    if (!command.isAcknowledged()) {
                        RobotLog.vv(TAG, "sending %s(%d), attempt: %d", command.getName(), command.getSequenceNumber(), command.getAttempts());
                    } else if (DEBUG) {
                        RobotLog.vv(TAG, "acking %s(%d)", command.getName(), command.getSequenceNumber());
                    }

                    // send the command
                    networkConnectionHandler.sendDataToPeer(command);

                    // if this is a command we handled, remove it
                    if (command.isAcknowledged()) commandsToRemove.add(command);
                }
            }
            pendingCommands.removeAll(commandsToRemove);
        }
        // For robustness and attempted ongoing liveness of the app, we catch
        // *all* types of exception. This will help minimize disruption to the sendLoopService.
        // With (a huge amount of) luck, the next time we're run, things might work better. Though
        // that's unlikely, it seems better than stopping the task, which would prevent this
        // runnable from continuing to execute, breaking most Robocol communication.
        // See the ScheduledExecutorService#scheduleAtFixedRate() javadoc.
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(Command cmd) {
        pendingCommands.add(cmd);
    }

    public boolean removeCommand(Command cmd) {
        return pendingCommands.remove(cmd);
    }

    public void clearCommands() {
        pendingCommands.clear();
    }
}
