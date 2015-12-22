package com.cloudesire.tisana4j.exceptions;

public class AccessDeniedException extends RestException
{
    private static final long serialVersionUID = -3953854267084448539L;

    public AccessDeniedException( int responseCode, String msgError )
    {
        super( responseCode, msgError );

    }

}
