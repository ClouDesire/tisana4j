package com.cloudesire.tisana4j.exceptions;

public class RuntimeRestException extends RestException
{
	private static final long serialVersionUID = 8233897756123307444L;

	public RuntimeRestException( Exception e )
	{
		super(e);
	}
}
