package com.cloudesire.tisana4j;

/**
 * 
 * @author Tazio Ceri
 * 
 */
public class HttpResponse
{
	private int code;
	private String response;

	public HttpResponse(int code, String response)
	{
		this.code = code;
		this.response = response;
	}

	public int getCode ()
	{
		return code;
	}

	public void setCode ( int code )
	{
		this.code = code;
	}

	public String getResponse ()
	{
		return response;
	}

	public void setResponse ( String response )
	{
		this.response = response;
	}
}
