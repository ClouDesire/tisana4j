package com.cloudesire.tisana4j.exceptions;

public class ResourceNotFoundException extends RestException
{
	private static final long serialVersionUID = 8396595711951630903L;
	
	public ResourceNotFoundException(int responseCode, String msgError)
	{
		super(responseCode,msgError);
	}
}
