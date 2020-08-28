package com.qualcomm.robotcore.exception;

/**
 * Indicates a condition where the remote peer cannot communicate with us, which will be self-evident
 * when we send them a packet in response.
 */
public class RobotProtocolException extends Exception {

    public RobotProtocolException(String message)
    {
        super(message);
    }

    public RobotProtocolException(String format, Object... args)
    {
        super(String.format(format, args));
    }
}
