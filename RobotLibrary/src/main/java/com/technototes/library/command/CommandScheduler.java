package com.technototes.library.command;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class CommandScheduler implements Runnable {
    private static CommandScheduler initInstance, runInstance, endInstance;
    //private Map<Command, Command.CommandState> scheduledCommands = new LinkedHashMap<>();
    private Map<Command, BooleanSupplier> commands = new LinkedHashMap<>();
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

    public CommandScheduler schedule(Command c) {
        return schedule(() -> true, c);
    }
    public CommandScheduler schedule(Runnable c) {
        return schedule(new InstantCommand(c));
    }

    public CommandScheduler schedule(BooleanSupplier b, Command c) {
        commands.put(c, b);
        return this;
    }

    //for finalizing all commands
    public void runLastTime() {
        commands.forEach((command, supplier) -> {
            if (command.commandState.state != Command.State.RESET) {
                command.end(true);
                command.commandState.state = Command.State.RESET;
            }
        });
    }

    @Override
    public void run() {
        commands.forEach((command, supplier) -> {
            if(supplier.getAsBoolean()){
                command.run();
            }
        });
    }

    public boolean cancel(Command c){
        return commands.remove(c).getAsBoolean();
    }
}
