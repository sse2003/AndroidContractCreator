package org.sse.contracts;

public class DebugException extends Throwable
{
    public DebugException(String message, Throwable ex)
    {
        super(message, ex);
    }
}
