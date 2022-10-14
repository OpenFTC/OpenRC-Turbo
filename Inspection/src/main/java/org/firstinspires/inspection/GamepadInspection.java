package org.firstinspires.inspection;

import com.google.gson.annotations.SerializedName;

public class GamepadInspection
    {
    @SerializedName("v")
    int vendor;
    @SerializedName("p")
    int product;

    public GamepadInspection(int vid, int pid)
        {
        this.vendor = vid;
        this.product = pid;
        }
    }
