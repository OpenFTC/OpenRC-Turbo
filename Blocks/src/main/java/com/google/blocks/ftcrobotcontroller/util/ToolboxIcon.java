/*
 * Copyright 2017 Google LLC
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

package com.google.blocks.ftcrobotcontroller.util;

/**
 * An enum to represent a toolbox icon.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public enum ToolboxIcon {
  ACCELERATION_SENSOR("AccelerationSensor-icon"),
  ANALOG_INPUT("AnalogInput-icon"),
  ANALOG_OUTPUT("AnalogOutput-icon"),
  CR_SERVO("CRServo-icon"),
  COLOR_SENSOR("ColorSensor-icon"),
  COMPASS_SENSOR("CompassSensor-icon"),
  DC_MOTOR("DcMotor-icon"),
  DC_MOTOR_CONTROLLER("DcMotorController-icon"),
  DEVICE_INTERFACE_MODULE("DeviceInterfaceModule-icon"),
  DIGITAL_CHANNEL("DigitalChannel-icon"),
  ELAPSED_TIME("ElapsedTime-icon"),
  GAMEPAD("Gamepad-icon"),
  GYRO_SENSOR("GyroSensor-icon"),
  I2C_DEVICE("I2cDevice-icon"),
  I2C_DEVICER_EADER("I2cDeviceReader-icon"),
  I2C_DEVICE_SYNCH("I2cDeviceSynch-icon"),
  IR_SEEKER_SENSOR("IrSeekerSensor-icon"),
  LED("LED-icon"),
  LEGACY_MODULE("LegacyModule-icon"),
  LIGHT_SENSOR("LightSensor-icon"),
  LINEAR_OPMODE("LinearOpMode-icon"),
  OP_MODE("OpMode-icon"),
  OPTICAL_DISTANCE_SENSOR("OpticalDistanceSensor-icon"),
  PWM_OUTPUT("PwmOutput-icon"),
  ROBOT_CONTROLLER("RobotController-icon"),
  SERVO("Servo-icon"),
  SERVO_CONTROLLER("ServoController-icon"),
  TOUCH_SENSOR("TouchSensor-icon"),
  TOUCH_SENSOR_MULTIPLEXER("TouchSensorMultiplexer-icon"),
  ULTRASONIC_SENSOR("UltrasonicSensor-icon"),
  VOLTAGE_SENSOR("VoltageSensor-icon");

  public final String cssClass;

  ToolboxIcon(String cssClass) {
    this.cssClass = cssClass;
  }
}
