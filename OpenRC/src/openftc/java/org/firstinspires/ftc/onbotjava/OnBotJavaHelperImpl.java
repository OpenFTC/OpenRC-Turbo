package org.firstinspires.ftc.onbotjava;

import android.util.ArraySet;

import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class OnBotJavaHelperImpl extends ClassLoader implements OnBotJavaHelper {

    @Override
    public ClassLoader createOnBotJavaClassLoader() {
        return OnBotJavaHelperImpl.class.getClassLoader();
    }

    @Override
    public Set<String> getOnBotJavaClassNames() {
        return new TreeSet<>();
    }

    @Override
    public Collection<String> getExternalLibrariesClassNames() {
        return new ArrayList<String>();
    }

    @Override
    public boolean isExternalLibrariesError(NoClassDefFoundError e) {
        return false;
    }
}
