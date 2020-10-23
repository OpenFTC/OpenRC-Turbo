package com.technototes.library.command;

/** Command group to run commands in sequence
 * @author Alex Stedman
 */
public class SequentialCommandGroup extends CommandGroup {
    private int currentCommandIndex = 0;

    /** Make sequential command group
     *
     */
    public SequentialCommandGroup(){
        super();
    }

    /** Make sequential command group
     *
     * @param commands The commands to run
     */
    public SequentialCommandGroup(Command... commands) {
        super(commands);
    }

    /** Run the commands in sequence
     *
     */
    @Override
    public void runCommands() {
        getCurrentCommand().run();

    }

    /** Returns if all the commands are finished
     *
     * @return Is the command group finished
     */
    @Override
    public boolean isFinished() {
        if (getCurrentCommand().isFinished()) {
            currentCommandIndex++;
        }
        //if the command goes one above index, the 0 index will equal the 1 index
        return currentCommandIndex == commands.size();
    }

    /** Get the command being currently run
     *
     * @return The current command
     */
    public Command getCurrentCommand() {
        return commands.get(currentCommandIndex);
    }
}
