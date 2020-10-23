package com.technototes.library.command;
/** Command group to run commands in parallel until one finishes
 * @author Alex Stedman
 */
public class ParallelRaceGroup extends CommandGroup {
    /** Make a parallel race group
     *
     */
    public ParallelRaceGroup(){
        super();
    }

    /** Make a parallel race group
     *
     * @param commands The commands for the group
     */
    public ParallelRaceGroup(Command... commands) {
        super(commands);
    }

    /** Run all the commands in parallel
     *
     */
    @Override
    public void runCommands() {
        commands.forEach(command -> run());
    }

    /** Is this finished
     *
     * @return If one of the commands is finished
     */
    @Override
    public boolean isFinished() {
        for (Command c : commands) {
            if (c.isFinished()) {
                return true;
            }
        }
        return false;
    }


}
