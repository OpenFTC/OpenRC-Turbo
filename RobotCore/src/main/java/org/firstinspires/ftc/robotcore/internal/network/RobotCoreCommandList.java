/*
Copyright (c) 2016-2017 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.robotcore.internal.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Base64;

import com.qualcomm.robotcore.hardware.USBAccessibleLynxModule;
import com.qualcomm.robotcore.util.SerialNumber;

import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters;

import java.io.File;
import java.util.ArrayList;

/**
 * {@link RobotCoreCommandList} contains network commands that are accessible in the RobotCore module
 */
public class RobotCoreCommandList
    {
    //----------------------------------------------------------------------------------------------
    // User interface remoting
    //----------------------------------------------------------------------------------------------

    public static final String CMD_SHOW_TOAST = "CMD_SHOW_TOAST";
    static public class ShowToast
        {
        public int     duration;
        public String  message;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static ShowToast deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, ShowToast.class);
            }
        }

    public static final String CMD_SHOW_PROGRESS = "CMD_SHOW_PROGRESS";
    // This class should be considered a part of the public API, as it is broadcast via WebSocket
    static public class ShowProgress extends ProgressParameters
        {
        public String message;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static ShowProgress deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, ShowProgress.class);
            }
        }

    public static final String CMD_DISMISS_PROGRESS = "CMD_DISMISS_PROGRESS";

    public static final String CMD_SHOW_STACKTRACE = "CMD_SHOW_STACKTRACE";

    public static final String CMD_SHOW_DIALOG = "CMD_SHOW_DIALOG";
    static public class ShowDialog
        {
        public String uuidString;
        public String title;
        public String message;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static ShowDialog deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, ShowDialog.class);
            }
        }

    public static final String CMD_DISMISS_ALL_DIALOGS = "CMD_DISMISS_ALL_DIALOGS";
    public static final String CMD_DISMISS_DIALOG = "CMD_DISMISS_DIALOG";
    static public class DismissDialog
        {
        public String uuidString;

        public DismissDialog(String uuidString) { this.uuidString = uuidString; }
        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static DismissDialog deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, DismissDialog.class);
            }
        }

    public static final String CMD_REQUEST_INSPECTION_REPORT = "CMD_REQUEST_INSPECTION_REPORT";
    public static final String CMD_REQUEST_INSPECTION_REPORT_RESP = "CMD_REQUEST_INSPECTION_REPORT_RESP";

    public static final String CMD_REQUEST_PARTICULAR_CONFIGURATION = "CMD_REQUEST_PARTICULAR_CONFIGURATION"; // also works for (resource-based) templates
    public static final String CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP = "CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP";

    public static final String CMD_DISABLE_BLUETOOTH = "CMD_DISABLE_BLUETOOTH";

    public static final String CMD_REQUEST_ABOUT_INFO = "CMD_REQUEST_ABOUT_INFO";
    public static final String CMD_REQUEST_ABOUT_INFO_RESP = "CMD_REQUEST_ABOUT_INFO_RESP";
    public static class AboutInfo
        {
        public String appVersion;
        public String libVersion;
        public String networkProtocolVersion;
        public String buildTime;
        public String networkConnectionInfo;
        public String osVersion;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static AboutInfo deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, AboutInfo.class);
            }
        }

    //----------------------------------------------------------------------------------------------
    // Robot semantics and management
    //----------------------------------------------------------------------------------------------

    public static final String CMD_NOTIFY_INIT_OP_MODE          = "CMD_NOTIFY_INIT_OP_MODE";
    public static final String CMD_NOTIFY_RUN_OP_MODE           = "CMD_NOTIFY_RUN_OP_MODE";

    public static final String CMD_REQUEST_ACTIVE_CONFIG        = "CMD_REQUEST_ACTIVE_CONFIG";
    public static final String CMD_REQUEST_USER_DEVICE_TYPES    = "CMD_REQUEST_USER_DEVICE_TYPES";
    public static final String CMD_REQUEST_OP_MODE_LIST         = "CMD_REQUEST_OP_MODE_LIST";

    public static final String CMD_NOTIFY_ACTIVE_CONFIGURATION  = "CMD_NOTIFY_ACTIVE_CONFIGURATION";
    public static final String CMD_NOTIFY_OP_MODE_LIST          = "CMD_NOTIFY_OP_MODE_LIST";
    public static final String CMD_NOTIFY_USER_DEVICE_LIST      = "CMD_NOTIFY_USER_DEVICE_LIST";
    public static final String CMD_NOTIFY_ROBOT_STATE           = "CMD_NOTIFY_ROBOT_STATE";

    // Used for sending a (pref,value) pair either from a RC to a DS or the other way around.
    // The pair is always a setting of the robot controller. When sent to the RC, it is a request
    // to update the setting; when sent from the RC, it is an announcement of the current value
    // of the setting.
    public static final String CMD_ROBOT_CONTROLLER_PREFERENCE = "CMD_ROBOT_CONTROLLER_PREFERENCE";

    //----------------------------------------------------------------------------------------------
    // Wi-Fi management
    //----------------------------------------------------------------------------------------------

    public static final String CMD_CLEAR_REMEMBERED_GROUPS                      = "CMD_CLEAR_REMEMBERED_GROUPS";
    public static final String CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED = "CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED";
    public static final String CMD_DISCONNECT_FROM_WIFI_DIRECT = "CMD_DISCONNECT_FROM_WIFI_DIRECT";
    public static final String CMD_VISUALLY_CONFIRM_WIFI_RESET = "CMD_VISUALLY_CONFIRM_WIFI_RESET";
    public static final String CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH = "CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH";

    //----------------------------------------------------------------------------------------------
    // Lynx firmware update support
    //----------------------------------------------------------------------------------------------

    /**
     * For the moment (perhaps forever), firmware images can only either be files or assets
     */
    public static class FWImage
        {
        public File file;
        public boolean isAsset;

        public FWImage(File file, boolean isAsset)
            {
            this.file = file;
            this.isAsset = isAsset;
            }

        public String getName()
            {
            return file.getName();
            }
        }

    public static final String CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES = "CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES";
    public static final String CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP = "CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP";

    public static class LynxFirmwareImagesResp
        {
        /** used to prompt user as to where to load images for updating */
        public File firstFolder = AppUtil.FIRST_FOLDER;
        /** currently available images. files or assets. */
        public ArrayList<FWImage> firmwareImages = new ArrayList<FWImage>();

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static LynxFirmwareImagesResp deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, LynxFirmwareImagesResp.class);
            }
        }
    public static final String CMD_GET_USB_ACCESSIBLE_LYNX_MODULES = "CMD_GET_USB_ACCESSIBLE_LYNX_MODULES";
    public static class USBAccessibleLynxModulesRequest
        {
        public boolean forFirmwareUpdate = false;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static USBAccessibleLynxModulesRequest deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, USBAccessibleLynxModulesRequest.class);
            }
        }
    public static final String CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP = "CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP";

    /** This class should be considered a part of the public JSON API exposed via the webserver */
    public static class USBAccessibleLynxModulesResp
        {
        public ArrayList<USBAccessibleLynxModule> modules = new ArrayList<USBAccessibleLynxModule>();

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static USBAccessibleLynxModulesResp deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, USBAccessibleLynxModulesResp.class);
            }
        }

    public static final String CMD_LYNX_FIRMWARE_UPDATE = "CMD_LYNX_FIRMWARE_UPDATE";
    public static class LynxFirmwareUpdate
        {
        public SerialNumber serialNumber;
        public FWImage firmwareImageFile;
        public String originatorId;

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static LynxFirmwareUpdate deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, LynxFirmwareUpdate.class);
            }
        }
    /** This class should be considered a part of the public JSON API exposed via the webserver */
    public static final String CMD_LYNX_FIRMWARE_UPDATE_RESP = "CMD_LYNX_FIRMWARE_UPDATE_RESP";
    public static class LynxFirmwareUpdateResp
        {
        public boolean success;
        public String errorMessage;
        public String originatorId; // Should match the originatorId of the originating LynxFirmwareUpdate payload, unless the RC is pre-6.0

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static LynxFirmwareUpdateResp deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, LynxFirmwareUpdateResp.class);
            }
        }

    //------------------------------------------------------------------------------------------------
    // Camera Stream
    //------------------------------------------------------------------------------------------------

    public static final String CMD_STREAM_CHANGE = "CMD_STREAM_CHANGE";
    public static class CmdStreamChange
        {
        public boolean available;

        public String serialize()
            {
            return String.valueOf(available);
            }

        public static CmdStreamChange deserialize(String serialized)
            {
            CmdStreamChange cmd = new CmdStreamChange();
            cmd.available = Boolean.parseBoolean(serialized);
            return cmd;
            }
        }

    public static final String CMD_REQUEST_FRAME       = "CMD_REQUEST_FRAME";

    public static final String CMD_RECEIVE_FRAME_BEGIN = "CMD_RECEIVE_FRAME_BEGIN";
    public static class CmdReceiveFrameBegin
        {
        private int frameNum, length;

        public CmdReceiveFrameBegin(int frameNum, int length)
            {
            this.frameNum = frameNum;
            this.length = length;
            }

        public int getFrameNum()
            {
            return frameNum;
            }

        public int getLength()
            {
            return length;
            }

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }

        public static CmdReceiveFrameBegin deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, CmdReceiveFrameBegin.class);
            }
        }

    public static final String CMD_RECEIVE_FRAME_CHUNK = "CMD_RECEIVE_FRAME_CHUNK";
    public static class CmdReceiveFrameChunk
        {
        private int frameNum, chunkNum;

        private transient byte[] data;

        private String encodedData;

        public CmdReceiveFrameChunk(int frameNum, int chunkNum, byte[] data, int offset, int length)
            {
            this.frameNum = frameNum;
            this.chunkNum = chunkNum;
            this.data = data;
            this.encodedData = Base64.encodeToString(data, offset, length, Base64.DEFAULT);
            }

        public int getFrameNum()
            {
            return frameNum;
            }

        public int getChunkNum()
            {
            return chunkNum;
            }

        public byte[] getData()
            {
            return data;
            }

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }

        public static CmdReceiveFrameChunk deserialize(String serialized)
            {
            CmdReceiveFrameChunk cmd =
                    SimpleGson.getInstance().fromJson(serialized, CmdReceiveFrameChunk.class);
            cmd.data = Base64.decode(cmd.encodedData, Base64.DEFAULT);
            return cmd;
            }
        }

    //----------------------------------------------------------------------------------------------
    // Telemetry
    //----------------------------------------------------------------------------------------------
    public static final String CMD_SET_TELEMETRY_DISPLAY_FORMAT = "CMD_SET_TELEM_DISPL_FORMAT";

    //----------------------------------------------------------------------------------------------
    // Gamepads
    //----------------------------------------------------------------------------------------------
    public static final String CMD_RUMBLE_GAMEPAD = "CMD_RUMBLE_EFFECT";
    public static final String CMD_GAMEPAD_LED_EFFECT = "CMD_GAMEPAD_LED_EFFECT";

    //----------------------------------------------------------------------------------------------
    // Telemetry text-to-speech
    //----------------------------------------------------------------------------------------------
    public static final String CMD_TEXT_TO_SPEECH = "CMD_TEXT_TO_SPEECH";
    static public class TextToSpeech
        {
        private String text;
        private @NonNull String languageCode;
        private @NonNull String countryCode;

        public TextToSpeech(String text, @Nullable String languageCode, @Nullable String countryCode)
            {
            this.text = text;
            this.languageCode = (languageCode != null) ? languageCode : "";
            this.countryCode = (countryCode != null) ? countryCode : "";
            }


        public String getText()
            {
            return text;
            }

        public String getLanguageCode()
            {
            return languageCode;
            }

        public String getCountryCode()
            {
            return countryCode;
            }

        public String serialize()
            {
            return SimpleGson.getInstance().toJson(this);
            }
        public static TextToSpeech deserialize(String serialized)
            {
            return SimpleGson.getInstance().fromJson(serialized, TextToSpeech.class);
            }
        }

    }
