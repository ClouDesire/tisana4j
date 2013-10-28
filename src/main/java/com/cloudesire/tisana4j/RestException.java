package com.cloudesire.tisana4j;

public class RestException extends Exception
{

	private static final long serialVersionUID = 8156589269399467370L;
	private final int responseCode;

	public RestException(int responseCode, String msgError)
	{
		super(msgError);
		this.responseCode = responseCode;
	}

	public int getResponseCode ()
	{
		return responseCode;
	}
}
