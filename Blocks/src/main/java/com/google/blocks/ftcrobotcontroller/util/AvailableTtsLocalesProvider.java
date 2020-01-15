package com.google.blocks.ftcrobotcontroller.util;

import android.os.Looper;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AvailableTtsLocalesProvider {
    private static final AvailableTtsLocalesProvider instance = new AvailableTtsLocalesProvider();
    private static final String TAG = "AvailableTtsLocalesProvider";

    public static AvailableTtsLocalesProvider getInstance() {
        return instance;
    }

    private Set<Locale> availableTtsLocales = null;

    /**
     * This method must NOT be called on the Android main/UI thread
     */
    public synchronized Set<Locale> getAvailableTtsLocales() {
        /* We check if we're running on the main thread here instead of relying on the check in
           AndroidTextToSpeech, because this method may or may not actually call into AndroidTextToSpeech,
           and we don't want any surprises.
         */

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            String errorMessage = "AvailableTtsLocalesProvider used from Android Main Thread. This is not allowed.";
            RobotLog.ee(TAG, errorMessage);
            throw new RuntimeException(errorMessage);
        }

        if (availableTtsLocales == null) {
            availableTtsLocales = new HashSet<>();
            AndroidTextToSpeech textToSpeech = new AndroidTextToSpeech();
            textToSpeech.initialize();
            for (Locale locale : Locale.getAvailableLocales()) {
                if (textToSpeech.isLocaleAvailable(locale)) {
                    availableTtsLocales.add(locale);
                }
            }
            textToSpeech.close();
        }
        return availableTtsLocales;
    }
}
