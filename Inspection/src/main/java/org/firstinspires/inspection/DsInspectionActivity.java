package org.firstinspires.inspection;


public class DsInspectionActivity extends InspectionActivity  {

    @Override protected boolean inspectingRobotController() {
        return false;
    }

    @Override protected boolean inspectingRemoteDevice() {
        return false;
    }

    @Override protected boolean useMenu() {
        return true;
    }
}
