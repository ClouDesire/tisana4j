package com.cloudesire.tisana4j;

import java.io.InputStream;

/**
 * Optional exception translator to handle errors with custom Exceptions.
 * 
 * @author d.napolitano
 * 
 */
public interface ExceptionTranslator
{
	/**
	 * Return the proper exception for the received response, or null to
	 * suppress the error.
	 * 
	 * @param responseCode
	 *            HTTP response code
	 * @param responseMessage
	 *            HTTP response status phrase
	 * @param errorStream
	 *            Content of the response
	 * @return the exception to throw, or null to suppress the error.
	 */
	public Exception translateError ( int responseCode, String responseMessage, InputStream errorStream );
}
