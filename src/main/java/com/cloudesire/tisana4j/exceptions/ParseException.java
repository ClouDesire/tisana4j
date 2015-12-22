package com.cloudesire.tisana4j.exceptions;

public class ParseException extends RuntimeRestException
{
    private static final long serialVersionUID = -1993161072836719569L;

    public ParseException( Exception e )
    {
        super( e );
    }

    public ParseException( String message )
    {
        super( message );
    }
}
