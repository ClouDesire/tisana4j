package com.cloudesire.tisana4j;

import com.cloudesire.tisana4j.exceptions.RestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface RestClientInterface
{
    /**
     * @return The response headers of the last executed request
     */
    Map<String, List<String>> getLastResponseHeaders();

    void delete( URL url ) throws RestException;

    void delete( URL url, Map<String, String> newHeaders ) throws RestException;

    <T> T get( URL url, Class<T> clazz ) throws RestException;

    <T> T get( URL url, TypeReference<T> typeReference ) throws RestException;

    <T> T get( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException;

    <T> T get( URL url, TypeReference<T> typeReference, Map<String, String> newHeaders )
            throws RestException;

    InputStream getData( URL url, Map<String, String> newHeaders ) throws RestException;

    InputStream get( URL url ) throws RestException;

    InputStream get( URL url, Map<String, String> headers ) throws RestException;

    <T> List<T> getCollection( URL url, Class<T> clazz ) throws RestException;

    <T> List<T> getCollection( URL url, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException;

    Map<String, String> head( URL url ) throws RestException;

    Map<String, String> head( URL url, Map<String, String> newHeaders ) throws RestException;

    String[] options( URL url ) throws RestException;

    String[] options( URL url, Map<String, String> newHeaders ) throws RestException;

    void patch( URL url, Object object ) throws RestException;

    void patch( URL url, Object object, Map<String, String> newHeaders ) throws RestException;

    @Deprecated
    void patchO( URL url, Map<String, Object> paramMap ) throws RestException;

    @Deprecated
    void patchO( URL url, Map<String, Object> paramMap, Map<String, String> newHeaders )
            throws RestException;

    <T> T patchEntity( URL url, Map<String, String> paramMap, Class<T> clazz )
            throws RestException;

    <T> T patchEntityO( URL url, Map<String, Object> paramMap, Class<T> clazz )
            throws RestException;

    <T> T patchEntity( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException;

    <T> T patchEntityO( URL url, Map<String, Object> paramMap, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException;

    <T> T post( URL url, T obj ) throws RestException;

    <T> T post( URL url, T obj, Map<String, String> newHeaders ) throws RestException;

    <T, R> R post( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass )
            throws RestException;

    <T> T postData( URL url, String filename, InputStream content, Class<T> responseClass )
            throws RestException;

    <T> T postData( URL url, String filename, InputStream content, Class<T> responseClass,
            Map<String, String> newHeaders ) throws RestException;

    <T> T postFormData( URL url, List<Pair> keyValueList, Class<T> responseClass )
            throws RestException;

    <T> T put( URL url, T obj ) throws RestException;

    <T> T put( URL url, T obj, Map<String, String> newHeaders ) throws RestException;

    <T, R> R put( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass )
            throws RestException;

    /**
     * Set an exception translator for server errors
     *
     * @param exceptionTranslator the custom handler for errors
     */
    void setExceptionTranslator( ExceptionTranslator exceptionTranslator );

    Map<String, String> getHeaders();

    void setHeaders( Map<String, String> headers );

    /**
     * @param useXml if true client uses xml instead of json.
     */
    void setUseXml( boolean useXml );

    void toggleAuthentication();

    String getUsername();

    void setHttpContentCompressionOverride( boolean disableContentCompression );

    void setObjectMapperFailOnUknownField( boolean flag );

    ObjectMapper getObjectMapper();

    void setObjectMapper( ObjectMapper mapper );
}
