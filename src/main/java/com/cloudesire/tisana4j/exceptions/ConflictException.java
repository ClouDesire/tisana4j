package com.cloudesire.tisana4j.exceptions;

public class ConflictException extends RestException
{
	private static final long serialVersionUID = 8820331083720207313L;

	public ConflictException(int responseCode, String msgError)
	{
		super(responseCode, msgError);

	}

}
