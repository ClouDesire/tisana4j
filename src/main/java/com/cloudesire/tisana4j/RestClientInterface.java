package com.cloudesire.tisana4j;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface RestClientInterface
{

	void delete ( URL url ) throws Exception;

	void delete ( URL url, Map<String, String> newHeaders ) throws Exception;

	<T> T get ( URL url, Class<T> clazz ) throws Exception;

	<T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception;

	<T> List<T> getCollection ( URL url, Class<T> clazz ) throws Exception;

	<T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception;

	Map<String, String> head ( URL url ) throws Exception;

	Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws Exception;

	String[] options ( URL url ) throws Exception;

	String[] options ( URL url, Map<String, String> newHeaders ) throws Exception;

	void patch ( URL url, Map<String, String> paramMap ) throws Exception;

	void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws Exception;

	<T> T post ( URL url, T obj ) throws Exception;

	<T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws Exception;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws Exception;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass, Map<String, String> responseHeaders ) throws Exception;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws Exception;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws Exception;

	<T> T put ( URL url, T obj ) throws Exception;

	<T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws Exception;

	/**
	 * Set an exception translator for server errors
	 * 
	 * @param exceptionTranslator
	 * 
	 */
	void setExceptionTranslator ( ExceptionTranslator exceptionTranslator );

	void setHttpResponseHandler ( HttpResponseHandler httpResponseHandler );

	void setHeaders ( Map<String, String> headers );

	/**
	 * @param useXml
	 *            if true client uses xml instead of json.
	 */
	void setUseXml ( boolean useXml );

}
