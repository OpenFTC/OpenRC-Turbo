package com.technototes.library.command;

import java.util.function.BooleanSupplier;

/** Simple class for commands that require a certain condition to be true to run
 * @author Alex Stedman
 */
public class ConditionalCommand extends Command {
    private BooleanSupplier supplier;
    private Command ifTrue, ifFalse, currentChoice;

    /** Make a conditional command
     *
     * @param condition The condition
     * @param command The command to run when the condition is true.
     */
    public ConditionalCommand(BooleanSupplier condition, Command command) {
        this(condition, command, null);
    }

    /** Make a conditional command
     *
     * @param condition The condition
     * @param trueCommand The command to run when the condition is true
     * @param falseCommand The command to run when the condition is false
     */
    public ConditionalCommand(BooleanSupplier condition, Command trueCommand, Command falseCommand) {
        supplier = condition;
        ifTrue = trueCommand;
        ifFalse = falseCommand;
    }

    @Override
    public void run() {
        switch (commandState) {
            case RESET:
                currentChoice = supplier.getAsBoolean() ? ifTrue : ifFalse;
                if (currentChoice != null) {
                    currentChoice.init();
                    commandState = CommandState.INITIALIZED;
                }
                return;
            case INITIALIZED:
                currentChoice.execute();
                commandState = currentChoice.isFinished() ? CommandState.EXECUTED : CommandState.INITIALIZED;
                return;
            case EXECUTED:
                currentChoice.end(false);
                commandState = CommandState.RESET;
                return;
        }
    }
}
