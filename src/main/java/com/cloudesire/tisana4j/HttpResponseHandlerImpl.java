package com.cloudesire.tisana4j;

public class HttpResponseHandlerImpl implements HttpResponseHandler
{
	private Integer code;
	private String response;

	@Override
	public void setResponse ( int code, String response )
	{
		this.code = code;
		this.response = response;
	}

	@Override
	public HttpResponse getResponse ()
	{
		if (code != null && response != null) return new HttpResponse(code, response);
		return null;
	}

}
