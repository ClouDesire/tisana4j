package com.cloudesire.tisana4j;

/**
 * 
 * @author Tazio Ceri
 * 
 */
public interface HttpResponseHandler
{
	void setResponse ( int code, String response );

	HttpResponse getResponse ();
}
