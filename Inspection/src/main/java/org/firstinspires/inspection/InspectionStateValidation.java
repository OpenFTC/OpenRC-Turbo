package org.firstinspires.inspection;

import com.google.gson.annotations.SerializedName;

public class InspectionStateValidation {
    boolean rev;
    boolean name;
    boolean os;
    @SerializedName("av")
    boolean appVersion;
    @SerializedName("bt")
    boolean bluetooth;
    boolean wifi;
    @SerializedName("fw")
    boolean firmware;
    @SerializedName("pw")
    boolean password;
    @SerializedName("am")
    boolean airplaneMode;
    @SerializedName("ln")
    boolean localNetworks;
    // TODO This is in the checklist but no rule requires it...
    @SerializedName("rw")
    boolean rememberedWifi;
    @SerializedName("oa")
    boolean otherApp;
    @SerializedName("vm")
    boolean versionsMatch;
}
