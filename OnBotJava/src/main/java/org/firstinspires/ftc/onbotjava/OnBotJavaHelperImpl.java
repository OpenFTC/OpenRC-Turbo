/*
 * Copyright (c) 2018 Craig MacFarlane
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Craig MacFarlane nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firstinspires.ftc.onbotjava;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OnBotJavaHelperImpl implements OnBotJavaHelper {

    private final static String TAG = "OnBotJavaHelperImpl";

    @Override
    public ClassLoader createOnBotJavaClassLoader()
    {
        // Build the dex path.
        StringBuilder dexPath = new StringBuilder();
        String delimiter = "";
        for (File dotDexFile : OnBotJavaManager.getOutputDexFiles()) {
          dexPath.append(delimiter).append(dotDexFile.getAbsolutePath());
          delimiter = File.pathSeparator;
        }
        // Create the ClassLoader.
        return new OnBotJavaClassLoader(dexPath.toString(), OnBotJavaManager.getParentClassLoaderForOnBotJava());
    }

    @Override
    public Collection<String> getOnBotJavaClassNames()
    {
        Set<String> classNames = new HashSet<String>();
        for (File dotJarFile : OnBotJavaManager.getOutputJarFiles())
        {
            try
            {
                JarFile jarFile = new JarFile(dotJarFile);
                try
                {
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); )
                    {
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory())
                        {
                            continue;
                        }
                        String entryName = entry.getName();
                        if (entryName.endsWith(".class"))
                        {
                            String className = entryName.substring(0, entryName.lastIndexOf('.'))
                                .replace('/', '.');
                            classNames.add(className);
                        }
                    }
                }
                finally
                {
                    jarFile.close();
                }
            }
            catch (IOException e)
            {
                RobotLog.ee(TAG, e, "getOnBotJavaClassNames");
            }
        }
        return classNames;
    }

    @Override
    public Collection<String> getExternalLibrariesClassNames()
    {
        return ExternalLibraries.getInstance().getClassNames();
    }

    @Override
    public boolean isExternalLibrariesError(NoClassDefFoundError e)
    {
      return ExternalLibraries.getInstance().isExternalLibrariesError(e);
    }
}
