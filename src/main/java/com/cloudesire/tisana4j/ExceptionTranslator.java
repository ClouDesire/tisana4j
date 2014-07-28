package com.cloudesire.tisana4j;

import com.cloudesire.tisana4j.exceptions.RestException;


/**
 * Optional exception translator to handle errors with custom Exceptions.
 * 
 * 
 */
public interface ExceptionTranslator
{
	/**
	 * Return the proper exception for the received response, or null to
	 * throw the tisana default exception.
	 * 
	 * @param responseCode
	 *            HTTP response code
	 * @param responseMessage
	 *            HTTP response status phrase
	 * @param errorStream
	 *            Content of the response
	 * @return the exception to throw, or null to throw tisana default exception.
	 */
	public <T extends RestException > T  translateException ( int responseCode, String responseMessage, String errorStream );
}
