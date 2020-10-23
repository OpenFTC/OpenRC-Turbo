package com.technototes.library.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/** Root class for all command groups
 * @author Alex Stedman
 */
public abstract class CommandGroup extends Command {
    protected List<Command> commands = new LinkedList<>();

    /** Basic constructor
     *
     */
    public CommandGroup(){

    }
    /** Create a command group with commands
     *
     * @param command Commands for group
     */
    public CommandGroup(Command... command) {
        commands.addAll(Arrays.asList(command));
    }

    /** Add a command to the group
     *
     * @param command The command
     * @return this
     */
    public CommandGroup addCommand(Command command) {
        commands.add(command);
        return this;
    }

    @Override
    public void run() {
        switch (commandState) {
            case EXECUTED:
                commandState = CommandState.RESET;
                return;
            default:
                runCommands();
                commandState = isFinished() ? CommandState.EXECUTED : CommandState.INITIALIZED;
        }
    }

    /** Run the commands
     *
     */
    public abstract void runCommands();

    @Override
    /** Return if tis is finished
     *
     */
    public abstract boolean isFinished();
}
