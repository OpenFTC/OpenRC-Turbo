package org.firstinspires.inspection;

import com.google.gson.annotations.SerializedName;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyData {
    @SerializedName("hw")
    private Map<String, Integer> hardwareData = new HashMap<>();
    @SerializedName("of")
    private Map<OpModeMeta.Flavor, Integer> opModeFlavors = new HashMap<>();
    @SerializedName("os")
    private Map<OpModeMeta.Source, Integer> opModeSources = new HashMap<>();
    @SerializedName("af")
    private boolean advancedGamepadFeatures;

public Map<String, Integer> getHardwareData() {
        return hardwareData;
    }

    public void setHardwareData(Map<String, Integer> hardwareData) {
        this.hardwareData = hardwareData;
    }

    public Map<OpModeMeta.Flavor, Integer> getOpModeFlavors() {
        return opModeFlavors;
    }

    public void setOpModeFlavors(Map<OpModeMeta.Flavor, Integer> opModeFlavors) {
        this.opModeFlavors = opModeFlavors;
    }

    public Map<OpModeMeta.Source, Integer> getOpModeSources() {
        return opModeSources;
    }

    public void setOpModeSources(Map<OpModeMeta.Source, Integer> opModeSources) {
        this.opModeSources = opModeSources;
    }

    public boolean isAdvancedGamepadFeatures() {
        return advancedGamepadFeatures;
    }

    public void setAdvancedGamepadFeatures(boolean advancedGamepadFeatures) {
        this.advancedGamepadFeatures = advancedGamepadFeatures;
    }
}
