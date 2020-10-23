package com.technototes.library.command;


import com.qualcomm.robotcore.util.ElapsedTime;
import com.technototes.library.subsystem.Subsystem;


import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** The root Command class
 * @author Alex Stedman
 */
public class Command implements Runnable {

    protected ElapsedTime commandRuntime;
    protected CommandState commandState;
    protected Set<Subsystem> requirements;

    /** Make a Command
     *
     */
    public Command() {
        commandState = CommandState.RESET;
        commandRuntime = new ElapsedTime();
        requirements = new LinkedHashSet<>();
        commandRuntime.reset();
    }

    /** Add requirement subsystems to command
     *
     * @param requirements The subsystems
     * @return this
     */
    public final Command addRequirements(Subsystem... requirements) {
        this.requirements.addAll(Arrays.asList(requirements));
        return this;
    }

    /** Init the command
     *
     */
    public void init() {

    }

    /** Execute the command
     *
     */
    public void execute() {

    }

    /** Return if the command is finished
     *
     * @return Is command finished
     */
    public boolean isFinished() {
        return true;
    }

    /** End the command
     *
     * @param cancel If the command was cancelled or ended naturally
     */
    public void end(boolean cancel) {

    }

    /** Add a command to be run after this
     *
     * @param command The next command
     * @return A {@link SequentialCommandGroup} for this and the added command
     */
    public final Command andThen(Command command) {
        if (command instanceof SequentialCommandGroup) {
            SequentialCommandGroup c2 = new SequentialCommandGroup(this);
            c2.commands.addAll(((SequentialCommandGroup) command).commands);
            return c2;
        }
        return new SequentialCommandGroup(this, command);
    }

    /** Add a command to be run after this when a condition is met
     *
     * @param condition The condition
     * @param command The command
     * @return A {@link SequentialCommandGroup} with the condition as a condition for the command
     */
    public final Command waitUntil(BooleanSupplier condition, Command command) {
        return andThen(new ConditionalCommand(condition, command));
    }

    /** Run the commmand
     *
     */
    @Override
    public void run() {
        switch (commandState) {
            case RESET:
                commandRuntime.reset();
                init();
                commandState = CommandState.INITIALIZED;
                //THERE IS NO RETURN HERE SO IT FALLS THROUGH TO POST-INITIALIZATION
            case INITIALIZED:
                execute();
                commandState = isFinished() ? CommandState.EXECUTED : CommandState.INITIALIZED;
                if(!isFinished()){
                    return;
                }
            case EXECUTED:
                end(false);
                commandState = CommandState.RESET;
                return;
        }
    }

    /** The command state enum
     *
     */
    public enum CommandState {
        RESET, INITIALIZED, EXECUTED
    }


    /** Return the command runtime
     *
     * @return The runtime as an {@link ElapsedTime}
     */
    public ElapsedTime getRuntime() {
        return commandRuntime;
    }

    /** Return the command state
     *
     * @return The state as an {@link CommandState}
     */
    public CommandState getState() {
        return commandState;
    }

    /** Return the subsystem requirements for this command
     *
     * @return The {@link Subsystem} requirements
     */
    public Set<Subsystem> getRequirements() {
        return requirements;
    }
}
