/*
 * Copyright (c) 2020 REV Robotics
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
 * Neither the name of REV Robotics nor the names of its contributors may be used to
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
package org.firstinspires.ftc.robotcore.internal.network;

import androidx.annotation.NonNull;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.qualcomm.robotcore.wifi.NetworkType;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.IOException;
import java.util.EnumSet;

public enum ApChannel {
    UNKNOWN(-1, Band.BAND_2_4_GHZ, false),

    // Auto-selection "channels"
    AUTO_2_4_GHZ(0, Band.BAND_2_4_GHZ, false), AUTO_5_GHZ(0, Band.BAND_5_GHZ, false),

    // 2.4 GHz channels (grouping these together allows us to use EnumSet.range() to get their values)
    CHAN_1(1), CHAN_2(2, true), CHAN_3(3, true), CHAN_4(4, true), CHAN_5(5, true),
    CHAN_6(6), CHAN_7(7, true), CHAN_8(8, true), CHAN_9(9, true), CHAN_10(10, true),
    CHAN_11(11),

    // Non-DFS 5 GHz channels (grouping these together allows us to use EnumSet.range() to get their values)
    CHAN_36(36), CHAN_40(40), CHAN_44(44), CHAN_48(48), CHAN_149(149),
    CHAN_153(153), CHAN_157(157), CHAN_161(161), CHAN_165(165),

    // DFS 5 GHz channels
    CHAN_52(52), CHAN_56(56), CHAN_60(60), CHAN_64(64), CHAN_100(100),
    CHAN_104(104), CHAN_108(108), CHAN_112(112), CHAN_116(116), CHAN_120(120),
    CHAN_124(124), CHAN_128(128), CHAN_132(132), CHAN_136(136), CHAN_140(140);

    // NEVER modify these sets directly. Call EnumSet.copyOf() instead.
    public static final EnumSet<ApChannel> ALL_2_4_GHZ_CHANNELS = EnumSet.range(CHAN_1, CHAN_11);
    public static final EnumSet<ApChannel> NON_DFS_5_GHZ_CHANNELS = EnumSet.range(CHAN_36, CHAN_165);

    // Copied from Android's WifiConfiguration class
    public static final int AP_BAND_2GHZ = 0;
    public static final int AP_BAND_5GHZ = 1;

    private static final int LOWEST_5GHZ_CHANNEL = 36;
    private static final int CHANNEL_AUTO_SELECT = 0;
    private static final String UNKNOWN_DISPLAY_NAME = "unknown";

    // These fields must be manually added to the GsonTypeAdapter inner class
    public final boolean overlapsWithOtherChannels;
    public final int channelNum; // 0 indicates auto-selection
    public final Band band;

    // Constructor for non-overlapping real channels
    ApChannel(int num) {
        this(num,false);
    }

    // Constructor for real channels
    ApChannel(int num, boolean overlaps) {
        this(num, (num < LOWEST_5GHZ_CHANNEL) ? Band.BAND_2_4_GHZ : Band.BAND_5_GHZ, overlaps);
    }

    // All other constructors must ultimately call this one
    ApChannel(int channelNum, Band band, boolean overlaps) {
        this.channelNum = channelNum;
        this.band = band;
        this.overlapsWithOtherChannels = overlaps;
    }

    public String getDisplayName() {
        if (this == UNKNOWN) return UNKNOWN_DISPLAY_NAME;
        if (channelNum == CHANNEL_AUTO_SELECT) {
            if (NetworkConnectionHandler.getNetworkType(AppUtil.getDefContext()) == NetworkType.WIFIDIRECT
                && WifiUtil.is5GHzAvailable()) {
                return "auto (either band)";
            }
            if (band == Band.BAND_2_4_GHZ) {
                return "auto (2.4 GHz)";
            }
            else {
                return "auto (5 GHz)";
            }
        }
        else return String.valueOf(channelNum);
    }

    /**
     * Gets an ApChannel instance from an ApChannel enum value name
     */
    @NonNull public static ApChannel fromName(String name) {
        ApChannel value = Enum.valueOf(ApChannel.class, name);
        if (value == null) value = ApChannel.UNKNOWN;
        return value;
    }

    /**
     * Gets an ApChannel instance from a band and channel
     */
    @NonNull public static ApChannel fromBandAndChannel(int band, int channelNum) {
        for (ApChannel channel : ApChannel.values()) {
            if (channel.band.androidInternalValue == band && channel.channelNum == channelNum) {
                return channel;
            }
        }
        return UNKNOWN;
    }

    public enum Band {
        BAND_2_4_GHZ(AP_BAND_2GHZ), BAND_5_GHZ(AP_BAND_5GHZ);

        public final int androidInternalValue;
        Band(int androidInternalValue) {
            this.androidInternalValue = androidInternalValue;
        }
    }

    public static class GsonTypeAdapter extends TypeAdapter<ApChannel> {
        @Override public void write(JsonWriter writer, ApChannel channel) throws IOException {
            if (channel == null) {
                writer.nullValue();
                return;
            }
            writer.beginObject();
            writer.name("name");
            writer.value(channel.name());

            writer.name("displayName");
            writer.value(channel.getDisplayName());

            writer.name("band");
            writer.value(channel.band.name());

            writer.name("overlapsWithOtherChannels");
            writer.value(channel.overlapsWithOtherChannels);
            writer.endObject();
        }

        @Override public ApChannel read(JsonReader reader) throws IOException {
            ApChannel result = null;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("name".equals(name)) {
                    result = ApChannel.fromName(reader.nextString());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            if (result == null) {
                result = ApChannel.UNKNOWN;
            }
            return result;
        }
    }
}
