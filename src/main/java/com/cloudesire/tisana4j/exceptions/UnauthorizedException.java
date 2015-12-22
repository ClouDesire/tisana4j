package com.cloudesire.tisana4j.exceptions;

public class UnauthorizedException extends RestException
{
    private static final long serialVersionUID = 4781730468095052369L;

    public UnauthorizedException( int responseCode, String msgError )
    {
        super( responseCode, msgError );
    }
}
