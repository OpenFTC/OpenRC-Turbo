package com.technototes.library.command;


import com.technototes.library.subsystem.Subsystem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** The command scheduler for managing commands
 * @author Alex Stedman
 */
public class CommandScheduler implements Runnable {
    private static CommandScheduler initInstance, runInstance, endInstance;
    private Map<Command, BooleanSupplier> commands = new LinkedHashMap<>();
    private Map<Subsystem<?>, Command> subsystems = new LinkedHashMap<>();
    private Map<Command, Command> cancelledCommands = new LinkedHashMap<>();

    /** Get command scheduler for init period
     *
     * @return The instance
     */
    public static synchronized CommandScheduler getInitInstance() {
        initInstance = getSelectedInstance(initInstance);
        return initInstance;
    }
    /** Get command scheduler for run period
     *
     * @return The instance
     */
    public static synchronized CommandScheduler getRunInstance() {
        runInstance = getSelectedInstance(runInstance);
        return runInstance;
    }
    /** Get command scheduler for end period
     *
     * @return The instance
     */
    public static synchronized CommandScheduler getEndInstance() {
        endInstance = getSelectedInstance(endInstance);
        return endInstance;
    }

    private static CommandScheduler getSelectedInstance(CommandScheduler c) {
        if (c == null) {
            c = new CommandScheduler();
        }
        return c;
    }

    /** Schedule command
     *
     * @param command The command to schedule
     * @return this
     */
    public CommandScheduler schedule(Command command) {
        return schedule(() -> true, command);
    }
    /** Schedule a runnable
     *
     * @param runnable The runnable to schedule
     * @return this
     */
    public CommandScheduler schedule(Runnable runnable) {
        return schedule(new InstantCommand(runnable));
    }

    /** Schedule a command to run when a condition is met
     *
     * @param condition The condition to meet
     * @param command The command
     * @return this
     */
    public CommandScheduler schedule(BooleanSupplier condition, Command command) {
        commands.put(command, condition);
        return this;
    }

    /** Run all commands for the last time
     *
     */
    public void runLastTime() {
        commands.forEach((command, supplier) -> {
            if (command.commandState != Command.CommandState.RESET) {
                command.end(true);
                command.commandState = Command.CommandState.RESET;
            }
        });
    }

    /** Run the commands
     *
     */
    @Override
    public void run() {
        commands.forEach((command, supplier) -> {
            if (supplier.getAsBoolean()) {
                if(command.getRequirements().stream().findFirst().isPresent()){
                    if(!subsystems.containsKey(command.getRequirements().stream().findFirst().get())){
                        command.run();
                        subsystems.put(command.getRequirements().stream().findFirst().get(), command);
                    }
                }else{
                    command.run();
                }
            }
        });
        subsystems = new LinkedHashMap<>();
    }

    /** Cancel a command
     *
     * @param command The command to cancel
     */
    public void cancel(Command command){
        command.end(true);
    }
}
