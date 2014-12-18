package com.cloudesire.tisana4j;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;

import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.RuntimeRestException;

public interface RestClientInterface
{
	void delete ( URL url ) throws RestException, RuntimeRestException;

	void delete ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	void delete ( URL url, Map<String, String> newHeaders, Map<String, String> responseHeaders ) throws RestException, RuntimeRestException;

	<T> T get ( URL url, Class<T> clazz ) throws RestException, RuntimeRestException;

	<T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	InputStream getData ( URL url, Map<String, String> newHeaders ) throws RuntimeRestException, RestException;

	<T> List<T> getCollection ( URL url, Class<T> clazz ) throws RestException, RuntimeRestException;

	<T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	Map<String, String> head ( URL url ) throws RestException, RuntimeRestException;

	Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	String[] options ( URL url ) throws RestException, RuntimeRestException;

	String[] options ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	void patch ( URL url, Map<String, String> paramMap ) throws RestException, RuntimeRestException;

	void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	<T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz ) throws RestException, RuntimeRestException;

	<T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	<T> T post ( URL url, T obj ) throws RestException, RuntimeRestException;

	<T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws RestException, RuntimeRestException;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass, Map<String, String> responseHeaders ) throws RestException, RuntimeRestException;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws RestException, RuntimeRestException;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	<T> T postFormData ( URL url, List<BasicNameValuePair> formData, Class<T> responseClass) throws RestException, RuntimeRestException;

	<T> T put ( URL url, T obj ) throws RestException, RuntimeRestException;

	<T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException;

	/**
	 * Set an exception translator for server errors
	 *
	 * @param exceptionTranslator
	 *
	 */
	void setExceptionTranslator ( ExceptionTranslator exceptionTranslator );

	void setHttpResponseHandler ( HttpResponseHandler httpResponseHandler );

	void setHeaders ( Map<String, String> headers );

	Map<String, String> getHeaders();

	/**
	 * @param useXml
	 *            if true client uses xml instead of json.
	 */
	void setUseXml ( boolean useXml );

	void toggleAuthentication();

	String getUsername();
}
