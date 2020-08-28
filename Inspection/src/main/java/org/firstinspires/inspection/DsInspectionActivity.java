package org.firstinspires.inspection;


public class DsInspectionActivity extends InspectionActivity  {

    @Override protected boolean inspectingRobotController() {
        return false;
        }

    @Override protected boolean useMenu() {
        return true;
        }

    @Override
    protected boolean validateAppsInstalled(InspectionState state) {

        // Robot Controller cannot be installed
        if (state.isRobotControllerInstalled()) {
            return false;
        }
        // Driver Station is required
        else {
            return state.isDriverStationInstalled();
        }
    }
}
