package com.cloudesire.tisana4j;

/**
 * 
 * @author Tazio Ceri <t.ceri@liberologico.com>
 * 
 */
public interface HttpResponseHandler
{
	void setResponse ( int code, String response );

	HttpResponse getResponse ();
}
