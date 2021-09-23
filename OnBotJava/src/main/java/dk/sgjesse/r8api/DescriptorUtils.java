// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// From https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/DescriptorUtils.java
// From https://r8.googlesource.com/r8/+/master/src/main/java/com/android/tools/r8/utils/DescriptorUtils.java

package dk.sgjesse.r8api;

import static dk.sgjesse.r8api.FileUtils.CLASS_EXTENSION;

/**
 * Utilities for descriptors.
 */
class DescriptorUtils {
    public static final char JAVA_PACKAGE_SEPARATOR = '.';

    public static String guessTypeDescriptor(String name) {
        assert name != null;
        assert name.endsWith(CLASS_EXTENSION) :
                "Name " + name + " must have " + CLASS_EXTENSION + " suffix";
        String descriptor = name.substring(0, name.length() - CLASS_EXTENSION.length());
        if (descriptor.indexOf(JAVA_PACKAGE_SEPARATOR) != -1) {
            throw new RuntimeException("Unexpected class file name: " + name);
        }
        return 'L' + descriptor + ';';
    }

    public static String getPathFromDescriptor(String descriptor) {
        // We are quite loose on names here to support testing illegal names, too.
        assert descriptor.startsWith("L");
        assert descriptor.endsWith(";");
        return descriptor.substring(1, descriptor.length() - 1) + CLASS_EXTENSION;
    }
}