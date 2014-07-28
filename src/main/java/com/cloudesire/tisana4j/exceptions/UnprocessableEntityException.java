package com.cloudesire.tisana4j.exceptions;

public class UnprocessableEntityException extends RestException
{
	private static final long serialVersionUID = 5863272304525246593L;

	public UnprocessableEntityException(int responseCode, String msgError)
	{
		super(responseCode,msgError);
	}
}
