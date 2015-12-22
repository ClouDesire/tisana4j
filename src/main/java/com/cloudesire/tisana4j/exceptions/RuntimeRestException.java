package com.cloudesire.tisana4j.exceptions;

public class RuntimeRestException extends RuntimeException
{
    private static final long serialVersionUID = 8233897756123307444L;

    public RuntimeRestException( Exception e )
    {
        super( e );
    }

    public RuntimeRestException( String message )
    {
        super( message );
    }
}
