package com.cloudesire.tisana4j.exceptions;

public class InternalServerErrorException extends RestException
{
	private static final long serialVersionUID = -4816182894418292549L;
	
	public InternalServerErrorException(int responseCode, String msgError)
	{
		super(responseCode, msgError);
	}
}
