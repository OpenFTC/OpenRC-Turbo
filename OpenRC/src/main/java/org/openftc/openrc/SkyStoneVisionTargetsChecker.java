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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class SkyStoneVisionTargetsChecker
{
    private static boolean alreadyCheckedThisSession = false;

    @OpModeRegistrar
    public static void run(Context context, AnnotatedOpModeManager manager)
    {
        if(alreadyCheckedThisSession)
        {
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);

        if(!checkFiles())
        {
            AppUtil.getInstance().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppUtil.getInstance().getActivity());
                    builder.setTitle("Missing files!");
                    builder.setMessage("Some required files are missing from the FIRST folder on the internal storage. Please check the setup instructions in the readme. The app will now be closed.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            System.exit(1);
                        }
                    });
                    builder.show();
                }
            });

            try
            {
                latch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        alreadyCheckedThisSession = true;
    }

    public static boolean checkFiles()
    {
        String[] files = new String[] {"/sdcard/FIRST/Skystone.xml", "/sdcard/FIRST/Skystone.dat", "/sdcard/FIRST/Skystone.tflite"};

        for(String s : files)
        {
            if(!(new File(s).exists()))
            {
                return false;
            }
        }

        return true;
    }
}
