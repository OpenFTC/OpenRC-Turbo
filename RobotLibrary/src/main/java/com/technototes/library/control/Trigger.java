package com.technototes.library.control;

import com.technototes.library.command.Command;

public abstract class Trigger {
    public abstract Trigger whenActivated(Command c);

    public abstract Trigger whenDeactivated(Command c);

    public abstract Trigger whileActivated(Command c);

    public abstract Trigger whileDeactivated(Command c);

    public abstract Trigger toggleWhenActivated(Command c);

    public abstract Trigger toggleWhenDeactivated(Command c);

    public abstract Trigger whenActivated(Runnable r);

    public abstract Trigger whenDeactivated(Runnable r);

    public abstract Trigger whileActivated(Runnable r);

    public abstract Trigger whileDeactivated(Runnable r);

    public abstract Trigger toggleWhenActivated(Runnable r);

    public abstract Trigger toggleWhenDeactivated(Runnable r);


}
