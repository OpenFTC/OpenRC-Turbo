package com.technototes.subsystem;

/** An interface for drivebase subsystems
 * @author Alex Stedman
 */
public interface DrivebaseSubsystem extends SpeedSubsystem {
    /** Enum for built in drive speeds (Deprecated because its better to do it custom)
     *
     */
    @Deprecated
    enum DriveSpeed{
        SNAIL(0.2), NORMAL(0.5), TURBO(1);
        public double spe;
        DriveSpeed(double s){
            spe = s;
        }
        public double getSpeed(){
            return spe;
        }
    }

    /** Returns the maximum of the given doubles, for better speed scaling
     *
     * @param powers Motor powers
     * @return The maximum of all the given powers
     */
    default double getScale(double... powers){
        double max = 0;
        for(double d : powers){
            max = Math.abs(d) > max ? Math.abs(d) : max;
        }
        return max;
    }

    DriveSpeed getDriveSpeed();

    /** Return DriveSpeed as a double (again, dont use built in speeds, override this method)
     *
     * @return The double
     */
    @Override
    default double getSpeed(){
        return getDriveSpeed().getSpeed();
    }
    @Override
    default void setSpeed(double speed){

    }
}
