package com.cloudesire.tisana4j.exceptions;

public class PreconditionFailedException extends RestException
{
    private static final long serialVersionUID = -3953854267084448539L;

    public PreconditionFailedException( int responseCode, String msgError )
    {
        super( responseCode, msgError );

    }

}
