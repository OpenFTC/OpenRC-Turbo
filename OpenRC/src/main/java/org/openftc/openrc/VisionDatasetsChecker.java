/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.openftc.openrc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class VisionDatasetsChecker
{
    private static boolean alreadyCheckedThisSession = false;
    private static Runnable onPeerConnectedRunnable = null;

    static
    {
        if(LynxConstants.isRevControlHub())
        {
            NetworkConnectionHandler.getInstance().registerPeerStatusCallback(new PeerStatusCallback()
            {
                @Override
                public void onPeerConnected()
                {
                    if(onPeerConnectedRunnable != null)
                    {
                        onPeerConnectedRunnable.run();
                    }
                }

                @Override
                public void onPeerDisconnected()
                {

                }
            });
        }
    }

    @OpModeRegistrar
    public static void run(Context context, AnnotatedOpModeManager manager)
    {
        if(alreadyCheckedThisSession)
        {
            return;
        }

        if(!checkFiles())
        {
            showError("Missing files!", "Some Vuforia / TensorFlow dataset files are missing from the FIRST folder on the internal storage. Please check to make sure you copied them as per the setup instructions in the readme");
        }

        alreadyCheckedThisSession = true;
    }

    public static boolean checkFiles()
    {
        String[] files = new String[] {

                /*
                 * Velocity Vortex
                 */
                "/sdcard/FIRST/FTC_2016-17.dat",
                "/sdcard/FIRST/FTC_2016-17.xml",

                /*
                 * Relic Recovery
                 */
                "/sdcard/FIRST/RelicVuMark.dat",
                "/sdcard/FIRST/RelicVuMark.xml",

                /*
                 * Rover Ruckus
                 */
                "/sdcard/FIRST/RoverRuckus.dat",
                "/sdcard/FIRST/RoverRuckus.xml",
                "/sdcard/FIRST/RoverRuckus.tflite",

                /*
                 * SkyStone
                 */
                "/sdcard/FIRST/Skystone.xml",
                "/sdcard/FIRST/Skystone.dat",
                "/sdcard/FIRST/Skystone.tflite"};

        for(String s : files)
        {
            if(!(new File(s).exists()))
            {
                return false;
            }
        }

        return true;
    }

    private static void showError(final String title, final String message)
    {
        RobotLog.setGlobalWarningMessage(message);

        if(LynxConstants.isRevControlHub())
        {
            //If robocol isn't linked yet, register the dialog for later
            if(!NetworkConnectionHandler.getInstance().isPeerConnected())
            {
                onPeerConnectedRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AppUtil.getInstance().showAlertDialog(UILocation.BOTH, title, message);
                    }
                };
            }

            //Robocol is linked, show dialog now
            else
            {
                AppUtil.getInstance().showAlertDialog(UILocation.BOTH, title, message);
            }
        }
        else
        {
            AppUtil.getInstance().showAlertDialog(UILocation.ONLY_LOCAL, title, message);
        }
    }
}
