package com.qualcomm.robotcore.hardware;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.R;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.util.HashSet;
import java.util.Iterator;

public class I2cWarningManager implements GlobalWarningSource
{
    // We don't have a getInstance() static method so that callers can't mix up suppressing new
    // device warnings, and suppressing already-present device warnings.
    // This way we can more carefully control the API we present.
    private static final I2cWarningManager instance = new I2cWarningManager();
    static
    {
        RobotLog.registerGlobalWarningSource(instance);
    }

    // We use an explicit, static lock to prevent confusing when locking from a static method vs an instance method
    private static final Object lock = new Object();

    // We use static state for anything that the GlobalWarningSource method overrides don't need
    private static int newProblemDeviceSuppressionCount = 0;

    public static void notifyProblemI2cDevice(@NonNull I2cDeviceSynchSimple dev)
    {
        synchronized (lock)
        {
            if (newProblemDeviceSuppressionCount == 0)
            {
                instance.problemDevices.add(dev);
            }
        }
    }

    public static void removeProblemI2cDevice(I2cDeviceSynchSimple dev)
    {
        synchronized (lock)
        {
            if (instance.problemDevices.isEmpty())
            {
                return;
            }
            instance.problemDevices.remove(dev);
        }
    }

    public static void suppressNewProblemDeviceWarningsWhile(Runnable runnable)
    {
        synchronized (lock)
        {
            newProblemDeviceSuppressionCount++;
        }
        try
        {
            runnable.run();
        }
        finally
        {
            synchronized (lock) {
                newProblemDeviceSuppressionCount--;
            }
        }
    }

    public static void suppressNewProblemDeviceWarnings(boolean suppress)
    {
        synchronized (lock)
        {
            if (suppress)
            {
                newProblemDeviceSuppressionCount++;
            }
            else
            {
                newProblemDeviceSuppressionCount--;
            }
        }
    }

    public static void clearI2cWarnings()
    {
        instance.clearGlobalWarning();
    }

    // Instance state
    private final HashSet<I2cDeviceSynchSimple> problemDevices = new HashSet<>();
    private int warningSourceSuppressionCount = 0;

    @Nullable
    @Override
    public String getGlobalWarning()
    {
        synchronized (lock) {
            if (problemDevices.isEmpty() || warningSourceSuppressionCount > 0)
            {
                return null;
            }

            StringBuilder builder = new StringBuilder();
            builder.append(AppUtil.getDefContext().getString(R.string.warningI2cCommError));
            builder.append(" ");

            Iterator<I2cDeviceSynchSimple> iterator = problemDevices.iterator();

            while (iterator.hasNext())
            {
                HardwareDevice dev = iterator.next();

                String name = ((RobotConfigNameable)dev).getUserConfiguredName();
                if (name != null)
                {
                    builder.append("'");
                    builder.append(name);
                    builder.append("'");
                }

                if (iterator.hasNext())
                {
                    builder.append(", ");
                }
                else
                {
                    builder.append(". ");
                }
            }

            builder.append("Check your wiring and configuration. ");

            return builder.toString();
        }
    }

    @Override
    public boolean shouldTriggerWarningSound()
    {
        return true;
    }

    @Override
    public void suppressGlobalWarning(boolean suppress) {
        synchronized (lock)
        {
            if (suppress)
            {
                warningSourceSuppressionCount++;
            }
            else
            {
                warningSourceSuppressionCount--;
            }
        }
    }

    @Override
    public void setGlobalWarning(String warning)
    {
    }

    @Override
    public void clearGlobalWarning()
    {
        synchronized (lock)
        {
            problemDevices.clear();
        }
    }
}
