package com.cloudesire.tisana4j.exceptions;

public class RestException extends Exception
{

	private static final long serialVersionUID = 8156589269399467370L;
	private final Integer responseCode;

	public RestException(Integer responseCode, String msgError)
	{
		super(msgError);
		this.responseCode = responseCode;
	}

	public int getResponseCode ()
	{
		return responseCode;
	}
}
