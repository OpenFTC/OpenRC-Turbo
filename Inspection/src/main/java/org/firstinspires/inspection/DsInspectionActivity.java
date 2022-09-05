package org.firstinspires.inspection;


public class DsInspectionActivity extends InspectionActivity  {

    @Override protected boolean inspectingRobotController() {
        return false;
    }

    @Override protected boolean inspectingRemoteDevice() {
        return false;
    }

    @Override protected int getMenu() {
        return R.menu.inspection_menu_rcds_local;
    }
}
