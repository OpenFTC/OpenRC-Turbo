package com.technototes.library.control;

import com.technototes.control.gamepad.GamepadButton;
import com.technototes.library.command.Command;

public interface Trigger<T> {
    /** Schedule command when gamepad button is just activated
     *
     * @param command The command
     * @return this
     */
    T whenActivated(Command command);
    /** Schedule command when gamepad button is just deactivated
     *
     * @param command The command
     * @return this
     */
    T whenDeactivated(Command command);
    /** Schedule command when gamepad button is activated
     *
     * @param command The command
     * @return this
     */
    T whileActivated(Command command);

    /** Schedule command when gamepad button is deactivated
     *
     * @param command The command
     * @return this
     */
    T whileDeactivated(Command command);

    /** Schedule command when gamepad button is just toggled
     *
     * @param command The command
     * @return this
     */
    T whenToggled(Command command);
    /** Schedule command when gamepad button is just inverse toggled
     *
     * @param command The command
     * @return this
     */
    T whenInverseToggled(Command command);

    /** Schedule command when gamepad button is toggled
     *
     * @param command The command
     * @return this
     */
    T whileToggled(Command command);
    /** Schedule command when gamepad button is inverse toggled
     *
     * @param command The command
     * @return this
     */
    T whileInverseToggled(Command command);

}
