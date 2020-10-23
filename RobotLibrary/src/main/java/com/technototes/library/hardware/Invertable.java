package com.technototes.library.hardware;

/** Interface for hardware devices that can be inverted
 * @author Alex Stedman
 * @param <T> The Hardware device to invert
 */
public interface Invertable<T extends HardwareDevice> {
    /** Set this device to be inverted
     *
     * @param invert Inversion to set device to
     * @return this
     */
    T setInverted(boolean invert);

    /** Get current inversion
     *
     * @return Current inversion
     */
    boolean getInverted();
}
