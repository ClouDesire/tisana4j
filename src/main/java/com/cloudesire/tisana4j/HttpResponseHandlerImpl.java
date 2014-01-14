package com.cloudesire.tisana4j;

/**
 * 
 * @author Tazio Ceri <t.ceri@liberologico.com>
 * 
 */
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
		if (code != null)
		{
			HttpResponse r = new HttpResponse(code, response);
			code = null;
			return r;
		}
		return null;
	}
}
