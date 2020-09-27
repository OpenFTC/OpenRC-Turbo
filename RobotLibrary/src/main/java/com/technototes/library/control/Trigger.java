package com.technototes.library.control;

import com.technototes.library.command.Command;

public abstract class Trigger {
    public abstract Trigger whenActivated(Command c);

    public abstract Trigger whenDeactivated(Command c);

    public abstract Trigger whileActivated(Command c);

    public abstract Trigger whileDeactivated(Command c);

    public abstract Trigger toggleWhenActivated(Command c);

    public abstract Trigger toggleWhenDeactivated(Command c);


}
