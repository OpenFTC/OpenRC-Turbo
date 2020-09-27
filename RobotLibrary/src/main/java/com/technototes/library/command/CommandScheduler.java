package com.technototes.library.command;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class CommandScheduler implements Runnable {
    private static CommandScheduler initInstance, runInstance, endInstance;
    private Map<Command, Command.CommandState> scheduledCommands = new LinkedHashMap<>();

    public static synchronized CommandScheduler getInitInstance() {
        initInstance = getSelectedInstance(initInstance);
        return initInstance;
    }

    public static synchronized CommandScheduler getRunInstance() {
        runInstance = getSelectedInstance(runInstance);
        return runInstance;
    }

    public static synchronized CommandScheduler getEndInstance() {
        endInstance = getSelectedInstance(endInstance);
        return endInstance;
    }

    public static synchronized CommandScheduler getSelectedInstance(CommandScheduler c) {
        if (c == null) {
            c = new CommandScheduler();
        }
        return c;
    }

    public void schedule(Command c) {
        scheduledCommands.put(c, c.commandState);
    }

    public void schedule(BooleanSupplier b, Command c) {
        schedule(new ConditionalCommand(b, c));
    }

    //for finalizing all commands
    public void runLastTime() {
        scheduledCommands.forEach((command, state) -> {
            command.run();
            if (command.commandState.state != Command.State.RESET) {
                command.end();
                command.commandState.state = Command.State.RESET;
            }
        });
    }

    @Override
    public void run() {
        scheduledCommands.forEach((command, state) -> {
            command.run();
        });
    }
}
