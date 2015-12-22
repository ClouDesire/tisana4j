package com.cloudesire.tisana4j.exceptions;

import com.cloudesire.tisana4j.ExceptionTranslator;
import org.apache.http.Header;

public class DefaultExceptionTranslator implements ExceptionTranslator
{

    @SuppressWarnings ( "unchecked" )
    @Override
    public RestException translateException( int responseCode, String responseMessage, String errorStream,
            ResponseMessage returnMessageRef, Header[] headers )
    {
        return null;
    }

}
