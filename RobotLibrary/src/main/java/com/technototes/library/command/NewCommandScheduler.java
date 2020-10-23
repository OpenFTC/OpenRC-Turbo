package com.technototes.library.command;

import com.technototes.library.hardware.HardwareDevice;
import com.technototes.library.subsystem.Subsystem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class NewCommandScheduler implements Runnable {

    private Map<Subsystem<?>, Map<Command, BooleanSupplier>> requirementCommands;
    private Map<Subsystem<?>, Command> runningRequirementCommands;
    private Map<Command, BooleanSupplier> commandsWithoutRequirements;
    private static final Subsystem<HardwareDevice<?>> NULL = new Subsystem<HardwareDevice<?>>() {
        @Override
        public HardwareDevice<?>[] getDevices() {
            return null;
        }
    };

    private static NewCommandScheduler instance;
    public static synchronized NewCommandScheduler getInstance(){
        if(instance == null){
            instance = new NewCommandScheduler();
        }
        return instance;
    }

    private NewCommandScheduler(){
        commandsWithoutRequirements = new HashMap<>();
        requirementCommands = new HashMap<>();
        runningRequirementCommands = new HashMap<>();
        requirementCommands.put(NULL, new HashMap<>());
    }

    public NewCommandScheduler schedule(Command command, BooleanSupplier supplier){
        if(command.getRequirements().isEmpty()){
            commandsWithoutRequirements.put(command, supplier);
        }else{
            command.requirements.forEach((subsystem -> {
                if(!requirementCommands.containsKey(subsystem)){
                    requirementCommands.put(subsystem, new LinkedHashMap<>());
                }
                if(subsystem.getDefaultCommand() == command){
                    runningRequirementCommands.put(subsystem, command);
                }
                requirementCommands.get(subsystem).put(command, supplier);
            }));
        }
        return this;
    }
    @Override
    public void run() {
        requirementCommands.forEach(((subsystem, commandMap) -> {
            commandMap.entrySet().stream().filter((entry) ->
                    entry.getKey().commandState == Command.CommandState.RESET && entry.getValue().getAsBoolean()
                    ).findFirst().ifPresent(m -> Objects.requireNonNull(runningRequirementCommands.put(subsystem, m.getKey())).end(true));
        }));
        runningRequirementCommands.forEach(((subsystem, command) -> run(command, requirementCommands.get(subsystem).get(command))));
        commandsWithoutRequirements.forEach(this::run);
    }
    public void run(Command command, BooleanSupplier supplier){
        if(supplier.getAsBoolean()){
            command.run();
        }else if(command.commandState != Command.CommandState.RESET){
            command.end(true);
            command.commandState = Command.CommandState.RESET;
        }

    }

}
