package Hardware;

import android.os.SystemClock;


import FieldStats.Field;
import HelperClasses.Robot;
import RobotUtilities.MovementVars;
import RobotUtilities.MyPosition;
import RobotUtilities.SpeedOmeter;
import teamcode.Auto1;

import static Hardware.Extension.RunToPositionModes;
import static RobotUtilities.MovementVars.movement_turn;
import static RobotUtilities.MovementVars.movement_x;
import static RobotUtilities.MovementVars.movement_y;

/**
 * This class auto feeds
 */
public class AutoFeeder {
    private Collector myCollector;
    private Lift myLift;
    private Robot myRobot;
    //this counts the number of auto feeds
    public static int numAutoFeeds = 0;
    //link to auto collector
    private AutoCollector myAutoCollector;



    /**
     * Past this percent, the release servo will go down, otherwise up.
     * See setLiftExtensionPowerNice() below for more details
     */
    private final double percentReleaseServoChange = 0.2;


    /////////////////////////////////STATE MACHINE STUFF////////////////////////////////////
    public static int myState = myStates.waiting.ordinal();



    //the time we reversed the roller
    private long rollerReverseTime = 0;
    //this is the percent the collector extension was extended when we first call auto feed
    private double initialExtensionPercent = 0.0;




    public enum myStates {
        waiting,
        /** AUTO FEED STUFF */
        retractingCollectorAndMoveInLift,
        waitingForExchange,
        movingUpLift,
        dumping,
        retractingLift,


        /** OTHER STATES */
        extendLiftToTopOverride,
        restingLiftOverride,
    }
    private long stateStartTime = 0;
    private boolean stageFinished = true;
    /////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Creates a new AutoFeeder object, you need to give it links to stuff
     * @param mRobot the link to the robot
     * @param collector a link specifically to the collector
     * @param lift a link to the lift
     * @param mAutoCollector link to the auto collector object so we can coordinate things
     */
    public AutoFeeder(Robot mRobot, Collector collector, Lift lift,AutoCollector mAutoCollector){
        numAutoFeeds = 0;
        myRobot = mRobot;
        myCollector = collector;
        myLift = lift;
        myAutoCollector = mAutoCollector;
    }


    /**if we are allowed to control the collector*/
    public static boolean canControlCollector = true;


    /**
     * Sets the extension speed of the collector but only if we are allowed to control it
     * @param speed the speed you want to go
     */
    public void setCollectorExtensionPowerNice(double speed) {
        if(canControlCollector){
            myCollector.setExtensionPowerNice(speed);
        }
    }





    //this is the percent the lift was at during
    private double liftDumpPosition = 1.0;



    //the time in milliseconds the last time adjustCollectorTargetRadius was called
    private long lastAdjustCollectorTargetRadiusTime = 0;
    //maximum allowed rate to adjust the collector target radius is 80 centimeters per second
    private final double maxAdjustCollectorRadiusSpeedCMpS = 80.0;

    /**
     * Adjusts the target radius of the collector extension similar to setCollectorExtensionPowerNice
     * but absolute based
     * @param power the "power" to apply, this is just a rate of extension
     */
    public void adjustCollectorTargetRadius(double power){
        if(canControlCollector){
            long currTime = SystemClock.uptimeMillis();
            long elapsedTime = currTime - lastAdjustCollectorTargetRadiusTime;
            double elapsedSeconds = (double) elapsedTime/1000.0;//calculate elapsedSeconds
            lastAdjustCollectorTargetRadiusTime = currTime;//save this for next time
            if(elapsedTime > 100){return;}//return if a rediculous amount of time has elapsed
            myCollector.adjustAbsoluteTargetRadius(power * elapsedSeconds * maxAdjustCollectorRadiusSpeedCMpS);
            myCollector.updateMaintainCurrentLocation();//turn this on now
        }
    }


    /**
     * The last time we dumped minerals (the release servo activates with a delay)
     */
    private long lastDumpTime = 0;

    /**
     * Sets the lift power nice if we are allowed to control it
     * @param power power to apply to the lift (this is not raw mode)
     */
    public void setLiftExtensionPowerNice(double power){
        if(myState == myStates.waiting.ordinal() && !retractingLift){
            myLift.setExtensionSpeedNice(power);

            //if we are greater than 90 % and dumping, release
            if(myLift.getTiltPercent() > 0.9 && lastDumpTime == 0){
                lastDumpTime = currTimeMillis;
            }

            //if 275 millis has past we can release the minerals
            if(currTimeMillis - lastDumpTime > 275 && lastDumpTime != 0){
                myLift.releaseMinerals();//this will release them once
                lastDumpTime = 0;//set this back to 0 since we have dumped now
            }else{
                //if we are going upwards and not dumped, hold them
                if(power > 0 && myLift.getExtensionPercent() > percentReleaseServoChange){
                    myLift.unreleaseMinerals();
                }else{
                    myLift.releaseMinerals();
                }
            }
        }
    }


    /**
     * This is used in emergencies where auto feed or auto collect has failed and will abort everything
     */
    public void abortAutoFeed() {
        nextStage();
        myState = myStates.waiting.ordinal();
        myLift.setExtensionPowerRaw(0);
        myCollector.setExtensionPowerRaw(0);
        myCollector.setRunToPositionMode(RunToPositionModes.powerControl);
        MovementVars.movement_x = 0;
        MovementVars.movement_y = 0;
        MovementVars.movement_turn = 0;
        canControlCollector = true;
        retractingLift = false;

        //if we are below the cutoff, move the servo up to not stall and otherwise down
        if(myLift.getExtensionPercent() < percentReleaseServoChange){
            myLift.releaseMinerals();
        }else{
            myLift.unreleaseMinerals();
        }
    }



    //this is true if we are still resetting the lift after an auto feed.
    //it will prevent setting the power so that it can reset
    private boolean retractingLift = false;


    //if the robot will move during the auto feed (using telemetry or not)
    private boolean allowedToDrive = false;







    /**
     * DECIDES IF WE ARE FEEDING FROM THE CRATER OR DEPOT
     * call this at the beginning of auto feed
     */
    private void initializeMasterVariables() {


        //this is being redone
//        //the absolute angle to the target feeding location (but we want to point 180 degrees from this)
//        double angleToTarget = Math.atan2(Robot.feedTargetY-myRobot.getYPos(),Robot.feedTargetX-myRobot.getXPos());
//
//        //we want it to be 30 degrees to the target
//        double targetAngleToTarget = Math.toRadians(40);
//        //calculate the delta angleToGo
//        double angleToGo = MyPosition.subtractAngles(targetAngleToTarget,angleToTarget);

//        myRobot.setCraterMode(Math.abs(angleToGo) < Math.toRadians(50));
        myRobot.setCraterMode(true);//always be crater
    }



    /**
     * Sends the lift to the top using position mode
     */
    public void extendLiftToTop() {
        nextStage();
        myState = myStates.extendLiftToTopOverride.ordinal();
    }

    /**
     * Resets the lift to the bottom using position mode
     */
    public void resetLiftToBottom() {
        nextStage();
        myState = myStates.restingLiftOverride.ordinal();
    }




    //if we are going to auto collect after auto feeding
    private boolean queueAutoCollect = false;
    /**
     * This method allows the user to queue a collecting location after the auto feed
     */
    public void collectAfterFeed() {
        queueAutoCollect = true;
    }





    /**If true, we won't wait the entire dumping time,
     * we will terminate that state a little early*/
    private boolean earlyFinishDump = false;
    /**
     * this won't wait as long for the dump in auto feed
     */
    public void earlyFinishDump() {
        earlyFinishDump = true;
    }

    /**
     * this wait's the full dump time amount
     */
    public void setRegularDump(){
        earlyFinishDump = false;
    }









    private double blockStartingX = 0;
    private double blockStartingY = 0;
    private double blockStartingAngle = 0;

    /**
     * Call this the first update of each state
     */
    private void initializeStateVariables(){
        stageFinished = false;
        stateStartTime = SystemClock.uptimeMillis();

        //record our starting coordinates
        blockStartingX = myRobot.getXPos();
        blockStartingY = myRobot.getYPos();
        blockStartingAngle = myRobot.getAngle_rad();
    }


    /**
     * Call this to increment the stage
     */
    public void nextStage(){
        stageFinished = true;
        myState ++;
    }





    //if we will modify our target based on the cube index
    private boolean useCubeLocationInAutoFeed = false;
    //sets the parameter
    public void setUseCubeLocationInAutoFeed(boolean useCubeLocationInAutoFeed){
        this.useCubeLocationInAutoFeed = useCubeLocationInAutoFeed;
    }


    /**The last time (millis) that we updated the lift **/
    private long lastUpdateTime = 0;
    private long currTimeMillis = 0;
    /**The elapsed time during this update*/
    private long elapsedTimeThisUpdate = 0;




    private boolean needToAbortCalled = false;


    /**
     * If it is a good time or not to use orbit mode
     * @return boolean true or false
     */
    public boolean isOkToOrbit() {
        return (rollerReverseTime != 0 &&
                currTimeMillis - rollerReverseTime > 100);
    }

    /**
     * Called every update
     */
    public void update(){
        currTimeMillis = SystemClock.uptimeMillis();
        elapsedTimeThisUpdate = currTimeMillis-lastUpdateTime;
        lastUpdateTime = currTimeMillis;

        myRobot.telemetry.addLine("\nABORTED YET: " + needToAbortCalled + "\n");

        //when in teleop, the auto state will be in endDoNothing, and this is forced
        boolean isAuto = Auto1.programStage != Auto1.progStates.endDoNothing.ordinal();

        myRobot.telemetry.addLine("TRIPS: " + numAutoFeeds);
        myRobot.telemetry.addLine("rollerReverseTime: " + rollerReverseTime);



        /**
         * FIRST HANDLE DRIVING TOWARDS THE THING
         * if we are between (inclusive) the states retractingCollector and exchangeDump
         */
        boolean doneDrivingToAutoFeed = false;


        //We're not going to actually feed if we are way out on the other side of the field
        boolean isAllowedToFeed = myRobot.getYPos() < Field.FIELD_LENGTH*0.8 || isAuto;

        if(allowedToDrive && isAllowedToFeed){
            if(myState >= myStates.retractingCollectorAndMoveInLift.ordinal() &&
                    myState <= myStates.dumping.ordinal()){
                if(myCollector.getExtensionPercent() < 0.20) {
                    //if this isn't auto, go whenever. But if it is, wait for the roller to reverse
                    if (!useCubeLocationInAutoFeed) {//we use the overloaded method if we are using cube location (modifies the auto feed position)
                        doneDrivingToAutoFeed = myRobot.driveToAutoFeed();//this will point to the feeding location
                    } else {
                        doneDrivingToAutoFeed = myRobot.driveToAutoFeed(Auto1.cubeLocation);//this will point to the feeding location
                    }
                }


                //slow down if this is auto
                if(isAuto && (rollerReverseTime == 0 ||
                        currTimeMillis - rollerReverseTime < 100)){
                    MovementVars.movement_y *= 0.5;
                    MovementVars.movement_x *= 0.5;
                    MovementVars.movement_turn *= 0.35;
                }
            }
        }






        //if we are really close, make the roller slower
        double currRollerMaxPower = myCollector.getExtensionPercent() > -0.03 ? -0.8 : -0.1;


        /*
          This is the first real state of auto feed. Currently it will slide all the components in
         */
        if(myState == myStates.retractingCollectorAndMoveInLift.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                initRetractCollector();
                myLift.unDump();
                myLift.resetErrorSum();
            }
            long elapsedTime = currTimeMillis - stateStartTime;

            //Retracts collector dumper
            myCollector.retractDumper();


            //give the collector time to dump
            if(elapsedTime > 50){
                //wait at 20% if the lift isn't back yet
                double targetPercent = myLift.getExtensionPercent() > 0.04 ? 0.2 : 0;
                retractCollector(targetPercent);
            }



            /* Moves lift to waiting position */
            myLift.resetLift(1.0);


            //if we are going to be there in 50 millis we can reverse early
            double collectorWithPrediction = myCollector.getExtensionPercent() +
                    myCollector.getExtensionCurrentSpeedPercent() * 0.055;//0.05


            //this is if we are able to move on due to moving to fast or not
            //if the collector is in the exchange position, it doesn't matter
            //if we're moving
            boolean isOkBecauseOfMoving =
                    (Math.abs(SpeedOmeter.getDegPerSecond()) < 30 &&
                            Math.abs(SpeedOmeter.getSpeedX()) < 40 &&
                            Math.abs(SpeedOmeter.getSpeedY()) < 40) ||
                            myCollector.getExtensionPercent() < 0.03;



            //sometimes the lift might not come all the way down in which we will
            //use a timeout to recalibrate it
            boolean liftIsDown = myLift.getExtensionPercent() < 0.025;
            if(!liftIsDown && elapsedTime > 2000){
//                myLift.setCurrentPositionTicks(0);
            }



            //we can activate the roller early if the collector is tilted, the lift is down
            //ANNNNDDD we aren't turning very fast
            boolean activateRoller = collectorWithPrediction < 0.045
                    && myCollector.getTiltPercent() < 0.05 &&
                    liftIsDown &&
                    (isOkBecauseOfMoving || myCollector.getExtensionPercent() < 0.025) &&
                    elapsedTime > 350;
            if(activateRoller){
//                myCollector.manualControl();
//                myCollector.setRollerPower(-(0.55 +
//                        ((myCollector.getExtensionPercent()/0.15)*(1.0-0.55))));
//                myCollector.reverseCollector();
                myCollector.manualControl();
                myCollector.setRollerPower(currRollerMaxPower);

                if(rollerReverseTime == 0){
                    rollerReverseTime = currTimeMillis;
                }
            }

            /*
             * Termination
             */
            if(activateRoller){
                if(doneDrivingToAutoFeed){
                    nextStage();//only go to the next stage if we're done driving
                }

                myLift.setExtensionPowerRaw(0);
            }
        }


        /*
          This state waits for the exchange to occur
         */
        if(myState == myStates.waitingForExchange.ordinal()){
            if(stageFinished){
                initializeStateVariables();
            }

            myCollector.manualControl();
            myCollector.setRollerPower(currRollerMaxPower);

//            myCollector.setRollerPower(-(0.55 +
//                    ((myCollector.getExtensionPercent()/0.15)*(1.0-0.55))));
//            myCollector.reverseCollector();



            //don't move up the lift unless we're completely clear
            if(currTimeMillis > rollerReverseTime + 375 && doneDrivingToAutoFeed &&
                    Math.abs(SpeedOmeter.getDegPerSecond()) < 5){
                if(isAllowedToFeed){
                    nextStage();
                }else{
                    abortAutoFeed();
                }
            }
        }



        //If it is time to move up the lift (if we are in the moving up lift state or dumping)
        //call that method
        if(myState >= myStates.movingUpLift.ordinal() &&
                myState <= myStates.dumping.ordinal()){
            myLift.moveUpLift(0.9);
        }


        /*
          Moving up lift
         */
        if(myState == myStates.movingUpLift.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                // we can put the servo down
                myLift.unreleaseMinerals();
            }

            //stop movement if we aren't trying to straif
            //currently because we are extending the lift
//            if(Math.abs(Robot.orbitAmount) < 0.001 && (myLift.getExtensionPercent() < 0.67
//                    || Math.abs(myLift.getExtensionCurrentSpeedPercent()) > 0.1) ){
//                myRobot.stopMovement();//stop the movement while moving up the lift
//            }

            long elapsedTime = SystemClock.uptimeMillis() - stateStartTime;

            //stop if it is less than 100 millis to let the servo start to activate
            if(elapsedTime < 100) {
                //this will automatically move the servo
                myLift.setExtensionPowerRaw(0.01);
            }


            //moves out the collector
            if(myLift.getExtensionPercent() < 0.2 &&
                    myCollector.getExtensionPercent() < 0.04){
                myCollector.setRunToPositionMode(RunToPositionModes.powerControl);
                myCollector.setExtensionPowerRaw(1.0);
            }else{
                myCollector.setExtensionPowerRaw(0);
            }


            //moves the collector back in when the lift is going up
            if(myLift.getExtensionPercent() > 0.2){
                //THEN MOVE BACK IN
                myCollector.resetExtension(0.0);

                //this enforce being back
                myCollector.retractDumper();
                //turn back on the roller
                myCollector.turnOnRoller();
                // we can have control back
                canControlCollector = true;
            }





            //dump when we get to 67%
            if(myLift.getExtensionPercent() > 0.67 &&
                    Math.abs(myRobot.getDeltaRadiusForFeeding()) < 10 &&
                    Math.abs(myRobot.currRelativePointAngleAutoFeed) < Math.toRadians(3)
                    || currTimeMillis - stateStartTime > 2000){
                nextStage();
                myCollector.setExtensionPowerRaw(0);
                canControlCollector = true;
            }
        }


        //this state dumps the minerals
        if(myState == myStates.dumping.ordinal()) {
            //allow orbitModeFeeder to do some stuff but slower
//            movement_turn *= 0.5;
//            movement_y *= 0.5;
//            movement_x *= 0.5;
            if (stageFinished) {
                initializeStateVariables();
                myRobot.stopMovement();
            }

            //dump the minerals
            myLift.startDumpAdvanced();
//            myLift.dump();

            //release the minerals when 200 millis has passed
            if(currTimeMillis - stateStartTime > 200){
                myLift.releaseMinerals();
            }



            //only abort if you put the collector down
            boolean needToAbort = myCollector.getTiltPercent() > 0.9;

            if(needToAbort){needToAbortCalled = true;}

            //terminate if 1.25 seconds have elapsed, or 0.65 if we are early dumping
            int timeToWait = !earlyFinishDump ? 1150 : 700;//TODO: re-enable early dump

            //up the dumping time in teleop because we can cancel
            if(!isAuto){timeToWait = 1600;}

            if (SystemClock.uptimeMillis() - stateStartTime > timeToWait ||
                    needToAbort) {//or we turned a bit
                nextStage();
                numAutoFeeds++;
                myRobot.stopMovement();//stop movement because driving to autoFeed cuts out here
            }
        }
        if(myState == myStates.retractingLift.ordinal()){
            if(stageFinished){
                initializeStateVariables();
            }

            myLift.unDump();//undump the servo

            //give it a second to retract the dumper
            if(currTimeMillis - stateStartTime > 200){
                retractingLift = true;
                myState = myStates.waiting.ordinal();
                //if we have queued an auto collect, go collect
                if(queueAutoCollect){
                    myAutoCollector.autoCollect();
                }
            }
        }


        //we may not have resetted the lift at the end of retracting lift, so it will continue
        //to go down. we need to unflag retracting lift when that is done
        if(retractingLift) {
            //when retractLift() returns true, we will be done so retractingLift is false
            retractingLift = !retractLift();
            //move this back when done
            if(myLift.getExtensionPercent() < 0.25){
                myLift.releaseMinerals();
            }
            if(!retractingLift){myLift.setExtensionPowerRaw(0);}
        }

        myRobot.telemetry.addLine("AutoFeeder State: " + myState);


        //This state extends the lift to the top if you want to in manual control
        if(myState == myStates.extendLiftToTopOverride.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                //Initialize the moving up lift variables
                myLift.moveUpLift(0.9);
            }
            if(myLift.getExtensionPercent() > 0.97){
                myLift.setExtensionPowerRaw(0);
                nextStage();
                myState = 0;
            }
        }

        /*
          This can be used in manual mode to automatically reset the lift
         */
        if(myState == myStates.restingLiftOverride.ordinal()){
            if(stageFinished){
                initializeStateVariables();
            }
            if(retractLift()){
                nextStage();
                myState = 0;
            }
        }



    }


    //is true when we are on position mode retracting the collector
    private boolean retractIsPositionMode = false;

    /**
     * Call this before retracting the collector
     */
    private void initRetractCollector(){
        retractIsPositionMode = false;
    }
    /**
     * This retracts the collector to a target percent
     * @param targetPercent the target percent
     */
    private void retractCollector(double targetPercent) {
        double predictedTimeUntilTarget = Math.abs(targetPercent-myCollector.getExtensionPercent())/
                Math.abs(myCollector.getExtensionCurrentSpeedPercent());

        //retracts at full speed until we are 0.2 seconds from the target (predicted)
        if(predictedTimeUntilTarget > secondsPredictionRetractCollector && !retractIsPositionMode
                && Math.abs(targetPercent - myCollector.getExtensionPercent()) > 0.25){
            myCollector.setRunToPositionMode(RunToPositionModes.powerControl);
            myCollector.setExtensionPowerRaw(-retractCollectorFastPower);
        }else{
            retractIsPositionMode = true;//now we are on position mode
            myCollector.resetExtension(targetPercent);
            myCollector.setExtensionPowerRaw(collectorPositionModePower);
        }
    }

    public static double secondsPredictionRetractCollector = 0.25;
    public static double retractCollectorFastPower = 1.0;
    public static double collectorPositionModePower = 0.85;





    /**
     * Call this every update you want to retract the lift
     */
    private boolean retractLift() {
        myLift.resetLift(1.0);
        return myLift.getExtensionPercent() < 0.03;
    }





    /**
     * Starts the next auto feed
     * @param allowedToDrive if the robot will auto aim or not
     */
    public void autoFeed(boolean allowedToDrive){
        if(isDoneAutoFeed()){
            this.allowedToDrive = allowedToDrive;
            nextStage();
            myAutoCollector.abortAutoCollect();
            myState = myStates.retractingCollectorAndMoveInLift.ordinal();
            initializeMasterVariables();
            myCollector.retractDumper();//retract the collector


            //set everything to default power control and set power to 0
            myLift.setExtensionPowerRaw(0);

            myCollector.setRunToPositionMode(RunToPositionModes.speedControl);
            myCollector.setExtensionPowerRaw(0);
            canControlCollector = false;
            initialExtensionPercent = myCollector.getExtensionPercent();
            rollerReverseTime = 0;//we haven't reversed the roller

            retractingLift = false;

            myLift.releaseMinerals();

            myRobot.initAutoFeed();

        }
    }


    /**
     * This allows turning off of allowed to drive in auto feed dynamically
     */
    public void setAllowedToDrive(boolean allowedToDrive){
        this.allowedToDrive = allowedToDrive;
    }

    /**
     * Returns if we are feeding or not
     * @return a boolean true or false
     */
    public boolean isDoneAutoFeed(){
        return myState == myStates.waiting.ordinal();
    }


}