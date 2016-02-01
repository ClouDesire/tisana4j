package com.cloudesire.tisana4j.exceptions;

public class MethodNotAllowedException extends RestException
{
    private static final long serialVersionUID = -3953854267084448539L;

    public MethodNotAllowedException( int responseCode, String msgError )
    {
        super( responseCode, msgError );

    }

}
