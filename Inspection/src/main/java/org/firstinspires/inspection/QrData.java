package org.firstinspires.inspection;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class QrData {
    @SerializedName("rc")
    InspectionState rcInspectionState;
    @SerializedName("ds")
    InspectionState dsInspectionState;
    @SerializedName("rcVal")
    InspectionStateValidation rcValidation;
    @SerializedName("dsVal")
    InspectionStateValidation dsValidation;
    @SerializedName("gp")
    List<GamepadInspection> gamepads = new ArrayList<>();
    @SerializedName("sv")
    SurveyData surveyData;
    @SerializedName("val")
    boolean validSurvey;
    @SerializedName("v")
    int version = 0; // in case we need to version this.
}
