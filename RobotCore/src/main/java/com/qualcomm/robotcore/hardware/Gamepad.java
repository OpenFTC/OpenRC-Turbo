/*
 * Copyright (c) 2014, 2015 Qualcomm Technologies Inc
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
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors may be used to
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

package com.qualcomm.robotcore.hardware;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.network.SendOnceRunnable;
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Monitor a hardware gamepad.
 * <p>
 * The buttons, analog sticks, and triggers are represented a public
 * member variables that can be read from or written to directly.
 * <p>
 * Analog sticks are represented as floats that range from -1.0 to +1.0. They will be 0.0 while at
 * rest. The horizontal axis is labeled x, and the vertical axis is labeled y.
 * <p>
 * Triggers are represented as floats that range from 0.0 to 1.0. They will be at 0.0 while at
 * rest.
 * <p>
 * Buttons are boolean values. They will be true if the button is pressed, otherwise they will be
 * false.
 * <p>
 * The codes KEYCODE_BUTTON_SELECT and KEYCODE_BACK are both be handled as a "back" button event.
 * Older Android devices (Kit Kat) map a Logitech F310 "back" button press to a KEYCODE_BUTTON_SELECT event.
 * Newer Android devices (Marshmallow or greater) map this "back" button press to a KEYCODE_BACK event.
 * Also, the REV Robotics Gamepad (REV-31-1159) has a "select" button instead of a "back" button on the gamepad.
 * <p>
 * The dpad is represented as 4 buttons, dpad_up, dpad_down, dpad_left, and dpad_right
 */
@SuppressWarnings("unused")
public class Gamepad extends RobocolParsableBase {

  /**
   * A gamepad with an ID equal to ID_UNASSOCIATED has not been associated with any device.
   */
  public static final int ID_UNASSOCIATED = -1;

  /**
   * A gamepad with a phantom id a synthetic one made up by the system
   */
  public static final int ID_SYNTHETIC = -2;

  public enum Type {
    // Do NOT change the order/names of existing entries,
    // you will break backwards compatibility!!
    UNKNOWN(LegacyType.UNKNOWN),
    LOGITECH_F310(LegacyType.LOGITECH_F310),
    XBOX_360(LegacyType.XBOX_360),
    SONY_PS4(LegacyType.SONY_PS4), // This indicates a PS4-compatible controller that is being used through our compatibility mode
    SONY_PS4_SUPPORTED_BY_KERNEL(LegacyType.SONY_PS4); // This indicates a PS4-compatible controller that is being used through the DualShock 4 Linux kernel driver.

    private final LegacyType correspondingLegacyType;
    Type(LegacyType correspondingLegacyType) {
      this.correspondingLegacyType = correspondingLegacyType;
    }
  }

  // LegacyType is necessary because robocol gamepad version 3 was written in a way that was not
  // forwards-compatible, so we have to keep sending V3-compatible values.
  public enum LegacyType {
    // Do NOT change the order or names of existing entries, or add new entries.
    // You will break backwards compatibility!!
    UNKNOWN,
    LOGITECH_F310,
    XBOX_360,
    SONY_PS4;
  }

  @SuppressWarnings("UnusedAssignment")
  public Type type = Type.UNKNOWN; // IntelliJ thinks this is redundant, but it is NOT. Must be a bug in the analyzer?

  /**
   * left analog stick horizontal axis
   */
  public float left_stick_x = 0f;

  /**
   * left analog stick vertical axis
   */
  public float left_stick_y = 0f;

  /**
   * right analog stick horizontal axis
   */
  public float right_stick_x = 0f;

  /**
   * right analog stick vertical axis
   */
  public float right_stick_y = 0f;

  /**
   * dpad up
   */
  public boolean dpad_up = false;

  /**
   * dpad down
   */
  public boolean dpad_down = false;

  /**
   * dpad left
   */
  public boolean dpad_left = false;

  /**
   * dpad right
   */
  public boolean dpad_right = false;

  /**
   * button a
   */
  public boolean a = false;

  /**
   * button b
   */
  public boolean b = false;

  /**
   * button x
   */
  public boolean x = false;

  /**
   * button y
   */
  public boolean y = false;

  /**
   * button guide - often the large button in the middle of the controller. The OS may
   * capture this button before it is sent to the app; in which case you'll never
   * receive it.
   */
  public boolean guide = false;

  /**
   * button start
   */
  public boolean start = false;

  /**
   * button back
   */
  public boolean back = false;

  /**
   * button left bumper
   */
  public boolean left_bumper = false;

  /**
   * button right bumper
   */
  public boolean right_bumper = false;

  /**
   * left stick button
   */
  public boolean left_stick_button = false;

  /**
   * right stick button
   */
  public boolean right_stick_button = false;

  /**
   * left trigger
   */
  public float left_trigger = 0f;

  /**
   * right trigger
   */
  public float right_trigger = 0f;

  /**
   * PS4 Support - Circle
   */
  public boolean circle = false;

  /**
   * PS4 Support - cross
   */
  public boolean cross = false;

  /**
   * PS4 Support - triangle
   */
  public boolean triangle = false;

  /**
   * PS4 Support - square
   */
  public boolean square = false;

  /**
   * PS4 Support - share
   */
  public boolean share = false;

  /**
   * PS4 Support - options
   */
  public boolean options = false;

  /**
   * PS4 Support - touchpad
   */
  public boolean touchpad = false;
  public boolean touchpad_finger_1;
  public boolean touchpad_finger_2;
  public float touchpad_finger_1_x;
  public float touchpad_finger_1_y;
  public float touchpad_finger_2_x;
  public float touchpad_finger_2_y;

  /**
   * PS4 Support - PS Button
   */
  public boolean ps = false;

  /**
   * Which user is this gamepad used by
   */
  protected byte user = ID_UNASSOCIATED;
  //
  public GamepadUser getUser() {
    return GamepadUser.from(user);
  }
  //
  public void setUser(GamepadUser user) {
    this.user = user.id;
  }

  /**
   * See {@link org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl#runActiveOpMode(Gamepad[])}
   */
  protected byte userForRumble = ID_UNASSOCIATED;
  public void setUserForRumble(byte userForRumble) {
    this.userForRumble = userForRumble;
  }

  /**
   * ID assigned to this gamepad by the OS. This value can change each time the device is plugged in.
   */
  public int id = ID_UNASSOCIATED;  // public only for historical reasons

  public void setGamepadId(int id) {
    this.id = id;
  }
  public int getGamepadId() {
    return this.id;
  }

  /**
   * Relative timestamp of the last time an event was detected
   */
  public long timestamp = 0;

  /**
   * Sets the time at which this Gamepad last changed its state,
   * in the {@link android.os.SystemClock#uptimeMillis} time base.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Refreshes the Gamepad's timestamp to be the current time.
   */
  public void refreshTimestamp() {
    setTimestamp(SystemClock.uptimeMillis());
  }

  /**
   * DPAD button will be considered pressed when the movement crosses this
   * threshold
   */
  protected float dpadThreshold = 0.2f;

  /**
   * If the motion value is less than the threshold, the controller will be
   * considered at rest
   */
  protected float joystickDeadzone = 0.2f; // very high, since we don't know the device type

  // private static values used for packaging the gamepad state into a byte array
  private static final short PAYLOAD_SIZE = 60;
  private static final short BUFFER_SIZE = PAYLOAD_SIZE + RobocolParsable.HEADER_LENGTH;

  private static final byte ROBOCOL_GAMEPAD_VERSION = 5;

  private static final float MAX_MOTION_RANGE = 1.0f;

  private static Set<Integer> gameControllerDeviceIdCache = new HashSet<Integer>();

  // Set of devices to consume input events from. If null, inputs from all detected devices will be used.
  private static Set<DeviceId> deviceWhitelist = null;

  /**
   * Container class to identify a vendor/product ID combination.
   *
   * Reusing Map.Entry which provides appropriate .equals/.hashCode
   */
  private static class DeviceId extends java.util.AbstractMap.SimpleEntry<Integer, Integer> {
    private static final long serialVersionUID = -6429575391769944899L;

    public DeviceId(int vendorId, int productId) {
      super(vendorId, productId);
    }

    @SuppressWarnings("unused")
    public int getVendorId() {
      return getKey();
    }

    @SuppressWarnings("unused")
    public int getProductId() {
      return getValue();
    }

  }

  public Gamepad() {
    this.type = type();
  }

  /**
   * Copy the state of a gamepad into this gamepad
   * @param gamepad state to be copied from
   * @throws RobotCoreException if the copy fails - gamepad will be in an unknown
   *         state if this exception is thrown
   */
  public void copy(Gamepad gamepad) throws RobotCoreException {
    // reuse the serialization code; since that reduces the chances of bugs
    fromByteArray(gamepad.toByteArray());
  }

  /**
   * Reset this gamepad into its initial state
   */
  public void reset() {
    try {
      copy(new Gamepad());
    } catch (RobotCoreException e) {
      // we should never hit this
      RobotLog.e("Gamepad library in an invalid state");
      throw new IllegalStateException("Gamepad library in an invalid state");
    }
  }

  @Override
  public MsgType getRobocolMsgType() {
    return RobocolParsable.MsgType.GAMEPAD;
  }

  @Override
  public byte[] toByteArray() throws RobotCoreException {

    ByteBuffer buffer = getWriteBuffer(PAYLOAD_SIZE);

    try {
      int buttons = 0;

      buffer.put(ROBOCOL_GAMEPAD_VERSION);
      buffer.putInt(id);
      buffer.putLong(timestamp).array();
      buffer.putFloat(left_stick_x).array();
      buffer.putFloat(left_stick_y).array();
      buffer.putFloat(right_stick_x).array();
      buffer.putFloat(right_stick_y).array();
      buffer.putFloat(left_trigger).array();
      buffer.putFloat(right_trigger).array();

      buttons = (buttons << 1) + (touchpad_finger_1 ? 1 : 0);
      buttons = (buttons << 1) + (touchpad_finger_2 ? 1 : 0);
      buttons = (buttons << 1) + (touchpad ? 1 : 0);
      buttons = (buttons << 1) + (left_stick_button ? 1 : 0);
      buttons = (buttons << 1) + (right_stick_button ? 1 : 0);
      buttons = (buttons << 1) + (dpad_up ? 1 : 0);
      buttons = (buttons << 1) + (dpad_down ? 1 : 0);
      buttons = (buttons << 1) + (dpad_left ? 1 : 0);
      buttons = (buttons << 1) + (dpad_right ? 1 : 0);
      buttons = (buttons << 1) + (a ? 1 : 0);
      buttons = (buttons << 1) + (b ? 1 : 0);
      buttons = (buttons << 1) + (x ? 1 : 0);
      buttons = (buttons << 1) + (y ? 1 : 0);
      buttons = (buttons << 1) + (guide ? 1 : 0);
      buttons = (buttons << 1) + (start ? 1 : 0);
      buttons = (buttons << 1) + (back ? 1 : 0);
      buttons = (buttons << 1) + (left_bumper ? 1 : 0);
      buttons = (buttons << 1) + (right_bumper ? 1 : 0);
      buffer.putInt(buttons);

      // Version 2
      buffer.put(user);

      // Version 3
      buffer.put((byte) legacyType().ordinal());

      // Version 4
      buffer.put((byte) type.ordinal());

      // Version 5
      buffer.putFloat(touchpad_finger_1_x);
      buffer.putFloat(touchpad_finger_1_y);
      buffer.putFloat(touchpad_finger_2_x);
      buffer.putFloat(touchpad_finger_2_y);
    } catch (BufferOverflowException e) {
      RobotLog.logStacktrace(e);
    }

    return buffer.array();
  }

  @Override
  public void fromByteArray(byte[] byteArray) throws RobotCoreException {
    if (byteArray.length < BUFFER_SIZE) {
      throw new RobotCoreException("Expected buffer of at least " + BUFFER_SIZE + " bytes, received " + byteArray.length);
    }

    ByteBuffer byteBuffer = getReadBuffer(byteArray);

    int buttons = 0;

    byte version = byteBuffer.get();

    // extract version 1 values
    if (version >= 1) {
      id = byteBuffer.getInt();
      timestamp = byteBuffer.getLong();
      left_stick_x = byteBuffer.getFloat();
      left_stick_y = byteBuffer.getFloat();
      right_stick_x = byteBuffer.getFloat();
      right_stick_y = byteBuffer.getFloat();
      left_trigger = byteBuffer.getFloat();
      right_trigger = byteBuffer.getFloat();

      buttons = byteBuffer.getInt();
      touchpad_finger_1   = (buttons & 0x20000) != 0;
      touchpad_finger_2   = (buttons & 0x10000) != 0;
      touchpad            = (buttons & 0x08000) != 0;
      left_stick_button   = (buttons & 0x04000) != 0;
      right_stick_button  = (buttons & 0x02000) != 0;
      dpad_up             = (buttons & 0x01000) != 0;
      dpad_down           = (buttons & 0x00800) != 0;
      dpad_left           = (buttons & 0x00400) != 0;
      dpad_right          = (buttons & 0x00200) != 0;
      a                   = (buttons & 0x00100) != 0;
      b                   = (buttons & 0x00080) != 0;
      x                   = (buttons & 0x00040) != 0;
      y                   = (buttons & 0x00020) != 0;
      guide               = (buttons & 0x00010) != 0;
      start               = (buttons & 0x00008) != 0;
      back                = (buttons & 0x00004) != 0;
      left_bumper         = (buttons & 0x00002) != 0;
      right_bumper        = (buttons & 0x00001) != 0;
    }

    // extract version 2 values
    if (version >= 2) {
      user = byteBuffer.get();
    }

    // extract version 3 values
    if (version >= 3) {
      type = Type.values()[byteBuffer.get()];
    }

    if (version >= 4) {
      byte v4TypeValue = byteBuffer.get();
      if (v4TypeValue < Type.values().length) {
        // Yes, this will replace the version 3 value. That is a good thing, since the version 3
        // value was not forwards-compatible.
        type = Type.values()[v4TypeValue];
      } // Else, we don't know what the number means, so we just stick with the value we got from the v3 type field
    }

    if(version >= 5) {
      touchpad_finger_1_x = byteBuffer.getFloat();
      touchpad_finger_1_y = byteBuffer.getFloat();
      touchpad_finger_2_x = byteBuffer.getFloat();
      touchpad_finger_2_y = byteBuffer.getFloat();
    }

    updateButtonAliases();
  }

  /**
   * Are all analog sticks and triggers in their rest position?
   * @return true if all analog sticks and triggers are at rest; otherwise false
   */
  public boolean atRest() {
    return (
        left_stick_x == 0f && left_stick_y == 0f &&
        right_stick_x == 0f && right_stick_y == 0f &&
        left_trigger == 0f && right_trigger == 0f);
  }

  /**
   * Get the type of gamepad as a {@link Type}. This method defaults to "UNKNOWN".
   * @return gamepad type
   */
  public Type type() {
    return type;
  }

  /**
   * Get the type of gamepad as a {@link LegacyType}. This method defaults to "UNKNOWN".
   * @return gamepad type
   */
  private LegacyType legacyType() {
    return type.correspondingLegacyType;
  }


  /**
   * Display a summary of this gamepad, including the state of all buttons, analog sticks, and triggers
   * @return a summary
   */
  @Override
  public String toString() {

    switch (type) {
      case SONY_PS4:
      case SONY_PS4_SUPPORTED_BY_KERNEL:
        return ps4ToString();

      case UNKNOWN:
      case LOGITECH_F310:
      case XBOX_360:
      default:
        return genericToString();
    }
  }


  protected String ps4ToString() {
    String buttons = new String();
    if (dpad_up) buttons += "dpad_up ";
    if (dpad_down) buttons += "dpad_down ";
    if (dpad_left) buttons += "dpad_left ";
    if (dpad_right) buttons += "dpad_right ";
    if (cross) buttons += "cross ";
    if (circle) buttons += "circle ";
    if (square) buttons += "square ";
    if (triangle) buttons += "triangle ";
    if (ps) buttons += "ps ";
    if (share) buttons += "share ";
    if (options) buttons += "options ";
    if (touchpad) buttons += "touchpad ";
    if (left_bumper) buttons += "left_bumper ";
    if (right_bumper) buttons += "right_bumper ";
    if (left_stick_button) buttons += "left stick button ";
    if (right_stick_button) buttons += "right stick button ";

    return String.format("ID: %2d user: %2d lx: % 1.2f ly: % 1.2f rx: % 1.2f ry: % 1.2f lt: %1.2f rt: %1.2f %s",
              id, user, left_stick_x, left_stick_y,
              right_stick_x, right_stick_y, left_trigger, right_trigger, buttons);
  }

  protected String genericToString() {
    String buttons = new String();
    if (dpad_up) buttons += "dpad_up ";
    if (dpad_down) buttons += "dpad_down ";
    if (dpad_left) buttons += "dpad_left ";
    if (dpad_right) buttons += "dpad_right ";
    if (a) buttons += "a ";
    if (b) buttons += "b ";
    if (x) buttons += "x ";
    if (y) buttons += "y ";
    if (guide) buttons += "guide ";
    if (start) buttons += "start ";
    if (back) buttons += "back ";
    if (left_bumper) buttons += "left_bumper ";
    if (right_bumper) buttons += "right_bumper ";
    if (left_stick_button) buttons += "left stick button ";
    if (right_stick_button) buttons += "right stick button ";

    return String.format("ID: %2d user: %2d lx: % 1.2f ly: % 1.2f rx: % 1.2f ry: % 1.2f lt: %1.2f rt: %1.2f %s",
            id, user, left_stick_x, left_stick_y,
            right_stick_x, right_stick_y, left_trigger, right_trigger, buttons);
  }

  /**
   * Add a whitelist filter for a specific device vendor/product ID.
   * <p>
   * This adds a whitelist to the gamepad detection method. If a device has been added to the
   * whitelist, then only devices that match the given vendor ID and product ID will be considered
   * gamepads. This method can be called multiple times to add multiple devices to the whitelist.
   * <p>
   * If no whitelist entries have been added, then the default OS detection methods will be used.
   * @param vendorId the vendor ID
   * @param productId the product ID
   */
  public static void enableWhitelistFilter(int vendorId, int productId) {
    if (deviceWhitelist == null) {
      deviceWhitelist = new HashSet<DeviceId>();
    }
    deviceWhitelist.add(new DeviceId(vendorId, productId));
  }

  /**
   * Clear the device whitelist filter.
   */
  public static void clearWhitelistFilter() {
    deviceWhitelist = null;
  }

  /**
   * Does this device ID belong to a gamepad device?
   * @param deviceId device ID
   * @return true, if gamepad device; false otherwise
   */
  public static synchronized boolean isGamepadDevice(int deviceId) {

    // check the cache
    if (gameControllerDeviceIdCache.contains(deviceId))
      return true;

    // update game controllers cache, since a new controller might have been plugged in
    gameControllerDeviceIdCache = new HashSet<Integer>();
    int[] deviceIds = InputDevice.getDeviceIds();
    for (int id : deviceIds) {
      InputDevice device = InputDevice.getDevice(id);

      int source = device.getSources();
      if ((source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
          || (source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {

        // null mDeviceWhitelist means all devices are valid
        // non-null mDeviceWhitelist means only use devices in mDeviceWhitelist
        if (deviceWhitelist == null
            || deviceWhitelist.contains(new DeviceId(device.getVendorId(), device.getProductId()))) {
          gameControllerDeviceIdCache.add(id);
        }
      }
    }

    // check updated cache
    if (gameControllerDeviceIdCache.contains(deviceId))
      return true;

    // this is not an event from a game pad
    return false;
  }

  // To prevent blowing up the command queue if the user tries to send a rumble command in a tight loop,
  // we have a 1-element evicting blocking queue for the outgoing rumble effect and the event loop periodically
  // just grabs the effect out of it (if any)
  public EvictingBlockingQueue<RumbleEffect> rumbleQueue = new EvictingBlockingQueue<>(new ArrayBlockingQueue<RumbleEffect>(1));
  public long nextRumbleApproxFinishTime = RUMBLE_FINISH_TIME_FLAG_NOT_RUMBLING;

  public static final int RUMBLE_DURATION_CONTINUOUS = -1;

  public static class RumbleEffect {
    public static class Step {
      public int large;
      public int small;
      public int duration;
    }

    public int user;
    public final ArrayList<Step> steps;

    private RumbleEffect(ArrayList<Step> steps) {
      this.steps = steps;
    }

    public String serialize() {
      return SimpleGson.getInstance().toJson(this);
    }

    public static RumbleEffect deserialize(String serialized) {
      return SimpleGson.getInstance().fromJson(serialized, RumbleEffect.class);
    }

    public static class Builder {
      private ArrayList<Step> steps = new ArrayList<>();

      /**
       * Add a "step" to this rumble effect. A step basically just means to rumble
       * at a certain power level for a certain duration. By creating a chain of
       * steps, you can create unique effects. See {@link #rumbleBlips(int)} for a
       * a simple example.
       * @param rumble1 rumble power for rumble motor 1 (0.0 - 1.0)
       * @param rumble2 rumble power for rumble motor 2 (0.0 - 1.0)
       * @param durationMs milliseconds this step lasts
       * @return the builder object, to follow the builder pattern
       */
      public Builder addStep(double rumble1, double rumble2, int durationMs) {
        return addStepInternal(rumble1, rumble2, Math.max(durationMs, 0));
      }

      private Builder addStepInternal(double rumble1, double rumble2, int durationMs) {

        rumble1 = Range.clip(rumble1, 0, 1);
        rumble2 = Range.clip(rumble2, 0, 1);

        Step step = new Step();
        step.large = (int) Math.round(Range.scale(rumble1, 0.0, 1.0, 0, 255));
        step.small = (int) Math.round(Range.scale(rumble2, 0.0, 1.0, 0, 255));
        step.duration = durationMs;
        steps.add(step);

        return this;
      }

      /**
       * After you've added your steps, call this to get a RumbleEffect object
       * that you can then pass to {@link #runRumbleEffect(RumbleEffect)}
       * @return a RumbleEffect object, built from previously added steps
       */
      public RumbleEffect build() {
        return new RumbleEffect(steps);
      }
    }
  }

  /**
   * Run a rumble effect built using {@link RumbleEffect.Builder}
   * The rumble effect will be run asynchronously; your OpMode will
   * not halt execution while the effect is running.
   *
   * Calling this will displace any currently running rumble effect
   */
  public void runRumbleEffect(RumbleEffect effect) {
    queueEffect(effect);
  }

  /**
   * Rumble the gamepad's first rumble motor at maximum power for a certain duration.
   * Calling this will displace any currently running rumble effect.
   * @param durationMs milliseconds to rumble for, or {@link #RUMBLE_DURATION_CONTINUOUS}
   */
  public void rumble(int durationMs) {

    if (durationMs != RUMBLE_DURATION_CONTINUOUS) {
      durationMs = Math.max(0, durationMs);
    }

    RumbleEffect effect = new RumbleEffect.Builder().addStepInternal(1.0, 0, durationMs).build();
    queueEffect(effect);
  }

  /**
   * Rumble the gamepad at a fixed rumble power for a certain duration
   * Calling this will displace any currently running rumble effect
   * @param rumble1 rumble power for rumble motor 1 (0.0 - 1.0)
   * @param rumble2 rumble power for rumble motor 2 (0.0 - 1.0)
   * @param durationMs milliseconds to rumble for, or {@link #RUMBLE_DURATION_CONTINUOUS}
   */
  public void rumble(double rumble1, double rumble2, int durationMs) {

    if (durationMs != RUMBLE_DURATION_CONTINUOUS) {
      durationMs = Math.max(0, durationMs);
    }

    RumbleEffect effect = new RumbleEffect.Builder().addStepInternal(rumble1, rumble2, durationMs).build();
    queueEffect(effect);
  }

  /**
   * Cancel the currently running rumble effect, if any
   */
  public void stopRumble() {
    rumble(0,0,RUMBLE_DURATION_CONTINUOUS);
  }

  /**
   * Rumble the gamepad for a certain number of "blips" using predetermined blip timing
   * This will displace any currently running rumble effect.
   * @param count the number of rumble blips to perform
   */
  public void rumbleBlips(int count) {
    RumbleEffect.Builder builder = new RumbleEffect.Builder();

    for(int i = 0; i < count; i++) {
      builder.addStep(1.0,0,250).addStep(0,0,100);
    }

    queueEffect(builder.build());
  }

  private void queueEffect(RumbleEffect effect) {
    effect.user = userForRumble;
    rumbleQueue.offer(effect);
    nextRumbleApproxFinishTime = calcApproxRumbleFinishTime(effect);
  }

  private static final long RUMBLE_FINISH_TIME_FLAG_NOT_RUMBLING = -1;
  private static final long RUMBLE_FINISH_TIME_FLAG_INFINITE = Long.MAX_VALUE;

  /**
   * Returns an educated guess about whether there is a rumble action ongoing on this gamepad
   * @return an educated guess about whether there is a rumble action ongoing on this gamepad
   */
  public boolean isRumbling() {
    if(nextRumbleApproxFinishTime == RUMBLE_FINISH_TIME_FLAG_NOT_RUMBLING) {
      return false;
    } else if(nextRumbleApproxFinishTime == RUMBLE_FINISH_TIME_FLAG_INFINITE) {
      return true;
    } else {
      return System.currentTimeMillis() < nextRumbleApproxFinishTime;
    }
  }

  private long calcApproxRumbleFinishTime(RumbleEffect effect) {
    // If the effect is only 1 step long and has an infinite duration...
    if(effect.steps.size() == 1 && effect.steps.get(0).duration == RUMBLE_DURATION_CONTINUOUS) {
      // If the power is zero, then that means the gamepad is being commanded to cease rumble
      if (effect.steps.get(0).large == 0 && effect.steps.get(0).small == 0) {
        return RUMBLE_FINISH_TIME_FLAG_NOT_RUMBLING;
      } else { // But if not, that means it's an infinite (continuous) rumble command
        return RUMBLE_FINISH_TIME_FLAG_INFINITE;
      }
    } else { // If the effect has more than one step (or one step with non-infinite duration) we need to sum the step times
      long time = System.currentTimeMillis();
      long overhead = 50 /* rumbleGamepadsInterval in FtcEventLoopHandler */ +
                      SendOnceRunnable.MS_BATCH_TRANSMISSION_INTERVAL +
                      5  /* Slop */;
      for(RumbleEffect.Step step : effect.steps) {
        time += step.duration;
      }
      time += overhead;
      return time;
    }
  }

  /**
   * Alias buttons so that XBOX & PS4 native button labels can be used in use code.
   * Should allow a team to program with whatever controllers they prefer, but
   * be able swap controllers easily without changing code.
   */
  protected void updateButtonAliases(){
    // There is no assignment for touchpad because there is no equivalent on XBOX controllers.
    circle = b;
    cross = a;
    triangle = y;
    square = x;
    share = back;
    options = start;
    ps = guide;
  }
}
