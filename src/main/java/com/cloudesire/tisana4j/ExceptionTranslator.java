package com.cloudesire.tisana4j;

import com.cloudesire.tisana4j.exceptions.RestException;


/**
 * Optional exception translator to handle errors with custom Exceptions.
 * 
 * 
 */
public interface ExceptionTranslator
{
	public static class ResponseMessage
	{
		private String response = null;

		public String getResponse ()
		{
			return response;
		}

		public void setResponse ( String response )
		{
			this.response = response;
		}
	}
	/**
	 * Return the proper exception for the received response, or null to throw
	 * the tisana default exception.
	 * 
	 * @param responseCode
	 *            HTTP response code
	 * @param responseMessage
	 *            HTTP response status phrase
	 * @param bodyMessage
	 *            Content of the response
	 * @param returnMessageRef
	 *            if method returns null you may set a response message here
	 * 
	 * @return the exception to throw, or null to throw tisana default
	 *         exception.
	 */
	public <T extends RestException > T  translateException ( int responseCode, String responseMessage, String bodyMessage, ResponseMessage returnMessageRef );
}
