package com.cloudesire.tisana4j.exceptions;

public class BadRequestException extends RestException
{
    private static final long serialVersionUID = -610795148684414765L;

    public BadRequestException( int responseCode, String msgError )
    {
        super( responseCode, msgError );
    }
}
