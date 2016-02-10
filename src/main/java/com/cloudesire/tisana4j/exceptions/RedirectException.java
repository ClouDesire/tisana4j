package com.cloudesire.tisana4j.exceptions;

/**
 * Returned when a 301 or 302 found reply for a non-GET request
 */
public class RedirectException extends RestException
{
    private static final long serialVersionUID = -610795148684414765L;

    public RedirectException( int responseCode, String msgError )
    {
        super( responseCode, msgError );
    }
}
