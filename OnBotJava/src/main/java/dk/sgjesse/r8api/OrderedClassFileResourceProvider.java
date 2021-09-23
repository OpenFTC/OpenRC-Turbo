// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
//
// Author lizlooney@google.com
// Based on OrderedClassFileResourceProvider from
// https://r8.googlesource.com/r8/+/master/src/main/java/com/android/tools/r8/D8CommandParser.java

package dk.sgjesse.r8api;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.ProgramResource;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderedClassFileResourceProvider implements ClassFileResourceProvider, Closeable {
  private final List<ClassFileResourceProvider> providers = new ArrayList<>();
  private final Set<String> descriptors = new HashSet<>();
  private boolean empty = true;
  private volatile boolean closed;

  public void addClassFileResourceProvider(ClassFileResourceProvider provider) {
    providers.add(provider);
    descriptors.addAll(provider.getClassDescriptors());
    empty = false;
  }

  public boolean isEmpty() {
    return empty;
  }

  @Override
  public Set<String> getClassDescriptors() {
    return descriptors;
  }

  @Override
  public ProgramResource getProgramResource(String descriptor) {
    // Search the providers in order. Return the program resource from the first provider that
    // can provide it.
    for (ClassFileResourceProvider provider : providers) {
      if (provider.getClassDescriptors().contains(descriptor)) {
        return provider.getProgramResource(descriptor);
      }
    }
    return null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      for (ClassFileResourceProvider provider : providers) {
        if (provider instanceof Closeable) {
          ((Closeable) provider).close();
        }
      }
      providers.clear();
      descriptors.clear();
    }
  }
}
