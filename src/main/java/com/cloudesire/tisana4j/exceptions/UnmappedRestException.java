package com.cloudesire.tisana4j.exceptions;

public class UnmappedRestException extends RestException
{
    private static final long serialVersionUID = 8820331083720207313L;

    public UnmappedRestException( int responseCode, String msgError )
    {
        super( responseCode, msgError );

    }

}
