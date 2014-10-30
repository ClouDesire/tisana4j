package com.cloudesire.tisana4j;

import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.RuntimeRestException;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface RestClientInterface
{
	void delete ( URL url ) throws RestException, URISyntaxException;

	void delete ( URL url, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	void delete ( URL url, Map<String, String> newHeaders, Map<String, String> responseHeaders ) throws RestException, URISyntaxException;

	<T> T get ( URL url, Class<T> clazz ) throws RestException, URISyntaxException;

	<T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	InputStream getData ( URL url, Map<String, String> newHeaders ) throws URISyntaxException, RuntimeRestException, RestException, IllegalStateException, IOException;

	<T> List<T> getCollection ( URL url, Class<T> clazz ) throws RestException, URISyntaxException;

	<T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	Map<String, String> head ( URL url ) throws RestException, URISyntaxException;

	Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	String[] options ( URL url ) throws RestException, URISyntaxException;

	String[] options ( URL url, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	void patch ( URL url, Map<String, String> paramMap ) throws RestException, URISyntaxException;

	void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	<T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz ) throws RestException, URISyntaxException;

	<T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	<T> T post ( URL url, T obj ) throws RestException, URISyntaxException;

	<T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws RestException, URISyntaxException;

	<T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass, Map<String, String> responseHeaders ) throws RestException, URISyntaxException;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws RestException, URISyntaxException;

	<T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws RestException, URISyntaxException;

	<T> T postFormData ( URL url, List<BasicNameValuePair> formData, Class<T> responseClass) throws RestException, URISyntaxException, UnsupportedEncodingException;

	<T> T put ( URL url, T obj ) throws RestException, URISyntaxException;

	<T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, URISyntaxException;

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
