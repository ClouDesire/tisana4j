package com.cloudesire.tisana4j.exceptions;

public class ExceptionFactory
{
    public static RestException getException( int statusCodeNumber, String exceptionMessage )
    {
        switch ( statusCodeNumber )
        {
            case 301:
            case 302:
                return new RedirectException( statusCodeNumber, exceptionMessage );
            case 400:
                return new BadRequestException( statusCodeNumber, exceptionMessage );
            case 401:
                return new UnauthorizedException( statusCodeNumber, exceptionMessage );
            case 403:
                return new AccessDeniedException( statusCodeNumber, exceptionMessage );
            case 404:
                return new ResourceNotFoundException( statusCodeNumber, exceptionMessage );
            case 405:
                return new MethodNotAllowedException( statusCodeNumber, exceptionMessage );
            case 409:
                return new ConflictException( statusCodeNumber, exceptionMessage );
            case 412:
                return new PreconditionFailedException( statusCodeNumber, exceptionMessage );
            case 422:
                return new UnprocessableEntityException( statusCodeNumber, exceptionMessage );
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
                return new InternalServerErrorException( statusCodeNumber, exceptionMessage );
        }
        return new UnmappedRestException( statusCodeNumber, exceptionMessage );
    }

    public static RestException getException( int statusCodeNumber, String statusCodeDescription, String exceptionMessage )
    {
        String msgError = exceptionMessage != null && exceptionMessage.length() > 0 ? exceptionMessage : statusCodeDescription;
        return getException( statusCodeNumber, msgError );
    }
}
