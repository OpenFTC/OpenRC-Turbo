/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.ftccommon.external;

import android.content.Context;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link OnDestroy} provides an easy way to perform final cleanup before the robot controller
 * activity is destroyed.
 *
 * <p>Place an OnDestroy annotation on a public static method in your code, and that method will be
 * automatically called from FtcRobotControllerActivity.onDestroy. The method must take a
 * {@link Context} as its only parameter.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnDestroy {
}
