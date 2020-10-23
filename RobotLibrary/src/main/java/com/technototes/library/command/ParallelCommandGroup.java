package com.technototes.library.command;

/** Command group to run commands in parallel until all finish
 * @author Alex Stedman
 */
public class ParallelCommandGroup extends CommandGroup {
    /** Make empty group
     *
     */
    public ParallelCommandGroup(){
        super();
    }

    /** Make parallel command group
     *
     * @param commands The commands for the group
     */
    public ParallelCommandGroup(Command... commands) {
        super(commands);
    }

    /** Runs the commands in parallel
     *
     */
    @Override
    public void runCommands() {
        commands.forEach(command -> run());
    }

    /** Is this finished
     *
     * @return If all of the commands are finished
     */
    @Override
    public boolean isFinished() {
        for (Command c : commands) {
            if (!c.isFinished()) {
                return false;
            }
        }
        return true;
    }
}
