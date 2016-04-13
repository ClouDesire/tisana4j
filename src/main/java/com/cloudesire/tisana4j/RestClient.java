package com.cloudesire.tisana4j;

import com.cloudesire.tisana4j.ExceptionTranslator.ResponseMessage;
import com.cloudesire.tisana4j.exceptions.AccessDeniedException;
import com.cloudesire.tisana4j.exceptions.BadRequestException;
import com.cloudesire.tisana4j.exceptions.ConflictException;
import com.cloudesire.tisana4j.exceptions.DefaultExceptionTranslator;
import com.cloudesire.tisana4j.exceptions.InternalServerErrorException;
import com.cloudesire.tisana4j.exceptions.MethodNotAllowedException;
import com.cloudesire.tisana4j.exceptions.ParseException;
import com.cloudesire.tisana4j.exceptions.PreconditionFailedException;
import com.cloudesire.tisana4j.exceptions.RedirectException;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.RuntimeRestException;
import com.cloudesire.tisana4j.exceptions.UnauthorizedException;
import com.cloudesire.tisana4j.exceptions.UnmappedRestException;
import com.cloudesire.tisana4j.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestClient implements RestClientInterface
{
    private final static Logger log = LoggerFactory.getLogger( RestClient.class );
    private final static Object jaxbGuard = new Object();
    private final String username;
    private final String password;
    private ObjectMapper mapper = new ObjectMapper();
    private final boolean skipValidation;
    private int CONNECTION_TIMEOUT = 60000 * 2;
    private int SOCKET_TIMEOUT = 60000 * 2;
    private boolean authenticated;
    private boolean useXml = false;
    private ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();
    private boolean skipContentCompression = false;
    private Map<String, String> headers;
    private Map<String, List<String>> responseHeaders;
    private SSLContext ctx;
    private HttpClient httpClient;

    /**
     * Default settings: no authentication and verify if server certificate is
     * valid. Uses json. For use xml setUseXml() to true.
     */
    public RestClient()
    {
        this( null, null, false, null );
    }

    /**
     * @param skipValidation if true skips server certificate validation for Https
     *                       connections
     */
    public RestClient( boolean skipValidation )
    {
        this( null, null, skipValidation, null );
    }

    /**
     * @param username       user for authentication
     * @param password       password for authentication
     * @param skipValidation if true skips server certificate validation for Https
     *                       connections
     */
    public RestClient( String username, String password, boolean skipValidation )
    {
        this( username, password, skipValidation, null );
    }

    /**
     * @param username       user for authentication
     * @param password       password for authentication
     * @param skipValidation if true skips server certificate validation for Https
     *                       connections
     * @param headers        connection properties that will be added by default to any
     *                       connection
     */
    public RestClient( String username, String password, boolean skipValidation, Map<String, String> headers )
    {
        this( username, password, skipValidation, headers, null );
    }

    /**
     * @param username       user for authentication
     * @param password       password for authentication
     * @param skipValidation if true skips server certificate validation for Https
     *                       connections
     * @param headers        connection properties that will be added by default to any
     *                       connection
     * @param ctx            ssl context
     */
    public RestClient( String username, String password, boolean skipValidation, Map<String, String> headers,
            SSLContext ctx )
    {
        this( username, password, skipValidation, headers, ctx, null, null );

    }

    /**
     * @param username          user for authentication
     * @param password          password for authentication
     * @param skipValidation    if true skips server certificate validation for Https
     *                          connections
     * @param headers           connection properties that will be added by default to any
     *                          connection
     * @param ctx               ssl context
     * @param connectionTimeOut connection timeout Milliseconds - default 2 minutes
     * @param socketTimeOut     socket timeout Milliseconds - default 2 minutes
     */
    public RestClient( String username, String password, boolean skipValidation, Map<String, String> headers,
            SSLContext ctx, Integer connectionTimeOut, Integer socketTimeOut )
    {
        super();
        this.username = username;
        this.password = password;
        this.skipValidation = skipValidation;
        authenticated = username != null;
        this.headers = headers;
        this.ctx = ctx;
        if ( connectionTimeOut != null ) this.CONNECTION_TIMEOUT = connectionTimeOut;
        if ( socketTimeOut != null ) this.SOCKET_TIMEOUT = socketTimeOut;
    }

    public RestClient( RestClientBuilder builder )
    {
        this( builder.getUsername(), builder.getPassword(), builder.getSkipValidation(), builder.getHeaders(),
                null, builder.getConnectionTimeout(), builder.getSocketTimeout() );
    }

    // wrap newInstance to avoid http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7122142
    private static JAXBContext getJaxbContext( Class<?> clazz ) throws JAXBException
    {
        synchronized ( jaxbGuard )
        {
            return JAXBContext.newInstance( clazz );
        }
    }

    @Override
    public void toggleAuthentication()
    {
        authenticated = !authenticated;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public void delete( URL url ) throws RestException, RuntimeRestException
    {
        delete( url, null );
    }

    private Map<String, List<String>> parseResponseHeaders( HttpResponse response )
    {
        responseHeaders = new HashMap<>();
        if ( response.getAllHeaders().length != 0 )
        {
            for ( Header header : response.getAllHeaders() )
            {
                String name = header.getName();
                String value = header.getValue();
                if ( responseHeaders.containsKey( name ) ) responseHeaders.get( name ).add( value );
                else
                {
                    List<String> x = new LinkedList<>();
                    x.add( value );
                    responseHeaders.put( name, x );
                }
            }
        }
        return responseHeaders;
    }

    @Override
    public void delete( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
    {
        HttpDelete delete;
        try
        {
            delete = new HttpDelete( url.toURI() );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
        prepareRequest( delete, newHeaders );
        HttpResponse response = execute( delete );
        parseResponseHeaders( response );
        EntityUtils.consumeQuietly( response.getEntity() );
    }

    @Override
    public <T> T get( URL url, Class<T> clazz ) throws RestException, RuntimeRestException
    {
        return get( url, clazz, null );
    }

    @Override
    public <T> T get( URL url, Class<T> clazz, Map<String, String> newHeaders )
            throws RuntimeRestException, RestException
    {
        try
        {
            HttpResponse response = getInternal( url, newHeaders );
            return readObject( clazz, response );
        }
        catch ( ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    private HttpResponse getInternal( URL url, Map<String, String> headers ) throws RuntimeRestException, RestException
    {
        try
        {
            HttpGet get = new HttpGet( url.toURI() );
            prepareRequest( get, headers );
            return execute( get );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public InputStream get( URL url ) throws RuntimeRestException, RestException, IOException
    {
        return getInternal( url, new HashMap<String, String>() ).getEntity().getContent();
    }

    @Override
    public InputStream get( URL url, Map<String, String> headers )
            throws RuntimeRestException, RestException, IOException
    {
        return getInternal( url, headers ).getEntity().getContent();
    }

    @Override
    public <T> List<T> getCollection( URL url, Class<T> clazz ) throws RestException, RuntimeRestException
    {
        return getCollection( url, clazz, null );

    }

    @Override
    public <T> List<T> getCollection( URL url, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpGet get = new HttpGet( url.toURI() );
            prepareRequest( get, newHeaders );
            HttpResponse response = execute( get );
            parseResponseHeaders( response );
            try ( InputStream stream = response.getEntity().getContent() )
            {
                return mapper.reader( mapper.getTypeFactory().constructCollectionType( List.class, clazz ) )
                        .readValue( stream );
            }
        }
        catch ( IOException | URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public Map<String, String> head( URL url ) throws RuntimeRestException, RestException
    {
        return head( url, null );
    }

    @Override
    public Map<String, String> head( URL url, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpHead head = new HttpHead( url.toURI() );
            prepareRequest( head, newHeaders );
            HttpResponse response = execute( head );
            EntityUtils.consumeQuietly( response.getEntity() );

            Map<String, String> headers = new HashMap<>();
            Header[] allHeaders = response.getAllHeaders();
            if ( allHeaders == null ) return headers;
            for ( Header allHeader : allHeaders )
                headers.put( allHeader.getName(), allHeader.getValue() );
            return headers;
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public String[] options( URL url ) throws RuntimeRestException, RestException
    {
        return options( url, null );
    }

    @Override
    public String[] options( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
    {
        HttpOptions options;
        try
        {
            options = new HttpOptions( url.toURI() );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
        prepareRequest( options, newHeaders );
        HttpResponse response = execute( options );
        EntityUtils.consumeQuietly( response.getEntity() );
        String allow = null;
        Header[] allHeaders = response.getAllHeaders();
        for ( Header allHeader : allHeaders )
            if ( Objects.equals( allHeader.getName(), "Allow" ) ) allow = allHeader.getValue();
        if ( allow == null ) throw new BadRequestException( 404, "Method options not supported." );
        return allow.split( "," );
    }

    @Override
    public void patch( URL url, Map<String, String> paramMap ) throws RestException, RuntimeRestException
    {
        patch( url, paramMap, null );
    }

    @Override
    public void patchO( URL url, Map<String, Object> paramMap ) throws RestException, RuntimeRestException
    {
        patchO( url, paramMap, null );
    }

    @Override
    public <T> T patchEntity( URL url, Map<String, String> paramMap, Class<T> clazz )
            throws RestException, RuntimeRestException
    {
        return patchEntity( url, paramMap, clazz, null );
    }

    @Override
    public <T> T patchEntityO( URL url, Map<String, Object> paramMap, Class<T> clazz )
            throws RestException, RuntimeRestException
    {
        return patchEntityO( url, paramMap, clazz, null );
    }

    @Override
    public <T> T patchEntity( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        return patchEntityO( url, paramMap != null ? new HashMap<String, Object>( paramMap ) : null, clazz,
                newHeaders );
    }

    @Override
    public <T> T patchEntityO( URL url, Map<String, Object> paramMap, Class<T> clazz, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpPatch patch = new HttpPatch( url.toURI() );

            prepareRequest( patch, newHeaders );
            writeObject( paramMap, patch );
            return readObject( clazz, execute( patch ) );
        }
        catch ( URISyntaxException | ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public void patch( URL url, Map<String, String> paramMap, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        patchO( url, paramMap != null ? new HashMap<String, Object>( paramMap ) : null, newHeaders );
    }

    @Override
    public void patchO( URL url, Map<String, Object> paramMap, Map<String, String> newHeaders )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpPatch patch = new HttpPatch( url.toURI() );

            prepareRequest( patch, newHeaders );
            writeObject( paramMap, patch );
            HttpResponse response = execute( patch );
            parseResponseHeaders( response );
            EntityUtils.consumeQuietly( response.getEntity() );
        }
        catch ( URISyntaxException | ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public <T> T post( URL url, T obj ) throws RestException, RuntimeRestException
    {
        return post( url, obj, null );
    }

    @Override
    @SuppressWarnings ( "unchecked" )
    public <T> T post( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
    {
        return (T) post( url, obj, newHeaders, obj.getClass() );
    }

    @Override
    public <T, R> R post( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpPost post = new HttpPost( url.toURI() );

            prepareRequest( post, newHeaders );
            writeObject( obj, post );
            HttpResponse response = execute( post );

            if ( response.getEntity() == null )
            {
                parseResponseHeaders( response );
                return null;
            }
            if ( responseClass == null )
            {
                parseResponseHeaders( response );
                EntityUtils.consumeQuietly( response.getEntity() );
                return null;
            }
            return readObject( responseClass, response );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public <T> T postData( URL url, String filename, InputStream content, Class<T> responseClass )
            throws RestException, RuntimeRestException
    {
        return postData( url, filename, content, responseClass, null );
    }

    @Override
    public <T> T postData( URL url, String filename, InputStream content, Class<T> responseClass,
            Map<String, String> newHeaders ) throws RestException, RuntimeRestException
    {

        try
        {
            HttpPost post = new HttpPost( url.toURI() );

            prepareRequest( post, newHeaders );

            InputStreamBody body = new InputStreamBody( content, filename );

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart( "file", body )
                    .build();

            post.setEntity( entity );
            HttpResponse response = execute( post );
            if ( responseClass == null || response.getEntity() == null )
            {
                parseResponseHeaders( response );
                EntityUtils.consumeQuietly( response.getEntity() );
                return null;
            }
            return readObject( responseClass, response );
        }
        catch ( URISyntaxException | ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public <T> T postFormData( URL url, List<Pair> pairs, Class<T> responseClass )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpPost post = new HttpPost( url.toURI() );
            prepareRequest( post, null );

            List<NameValuePair> formData = new ArrayList<>();
            for ( Pair pair: pairs )
            {
                formData.add( new BasicNameValuePair( pair.getKey(), pair.getValue() ) );
            }

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formData, "UTF-8" );
            post.setEntity( entity );
            HttpResponse response = execute( post );
            if ( responseClass == null || response.getEntity() == null )
            {
                parseResponseHeaders( response );
                EntityUtils.consumeQuietly( response.getEntity() );
                return null;
            }
            return readObject( responseClass, response );
        }
        catch ( URISyntaxException | UnsupportedEncodingException | ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public <T> T put( URL url, T obj ) throws RestException, RuntimeRestException
    {
        return put( url, obj, null );
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T put( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
    {
        return (T) put( url, obj, newHeaders, obj.getClass() );
    }

    @Override
    public <T, R> R put( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass )
            throws RestException, RuntimeRestException
    {
        try
        {
            HttpPut put = new HttpPut( url.toURI() );
            prepareRequest( put, newHeaders );
            writeObject( obj, put );
            HttpResponse response = execute( put );
            if ( response.getEntity() == null )
            {
                parseResponseHeaders( response );
                return null;
            }
            return readObject( responseClass, response );
        }
        catch ( URISyntaxException | ParseException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public void setExceptionTranslator( ExceptionTranslator exceptionTranslator )
    {
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    @Override
    public void setHeaders( Map<String, String> headers )
    {
        this.headers = headers;
    }

    @Override
    public void setUseXml( boolean useXml )
    {
        this.useXml = useXml;
    }

    private void applyHeaders( HttpRequest request, Map<String, String> newHeaders )
    {
        Map<String, String> mergedHeaders = new HashMap<>();
        if ( headers != null ) mergedHeaders.putAll( headers );
        if ( newHeaders != null ) mergedHeaders.putAll( newHeaders );

        for ( String k : mergedHeaders.keySet() )
            request.addHeader( k, mergedHeaders.get( k ) );
    }

    private void checkError( HttpResponse response ) throws RestException
    {
        int responseCode = response.getStatusLine().getStatusCode();
        if ( responseCode < 200 || responseCode >= 300 )
        {
            try ( InputStream stream = response.getEntity().getContent() )
            {
                ContentType type = ContentType.getOrDefault( response.getEntity() );
                String charset = type.getCharset() != null ? type.getCharset().name() : "UTF-8";

                String errorStream = IOUtils.toString( stream, charset );

                ResponseMessage responseMessage = new ResponseMessage();
                RestException translatedException = exceptionTranslator
                        .translateException( responseCode, response.getStatusLine().getReasonPhrase(), errorStream,
                                responseMessage, response.getAllHeaders() );

                if ( translatedException != null ) throw translatedException;

                throw getDefaultException( responseCode, response.getStatusLine().getReasonPhrase(),
                        responseMessage.getResponse() != null ? responseMessage.getResponse() : errorStream );

            }
            catch ( IllegalStateException | IOException e )
            {
                throw new RestException( responseCode, e.getMessage() );
            }
        }
    }

    private RestException getDefaultException( int responseCode, String reasonPhrase, String responseMessage )
    {
        String msgError = responseMessage != null ? responseMessage : reasonPhrase;

        switch ( responseCode )
        {
            case 301:
            case 302:
                return new RedirectException( responseCode, msgError );
            case 400:
                return new BadRequestException( responseCode, msgError );
            case 401:
                return new UnauthorizedException( responseCode, msgError );
            case 403:
                return new AccessDeniedException( responseCode, msgError );
            case 404:
                return new ResourceNotFoundException( responseCode, msgError );
            case 405:
                return new MethodNotAllowedException( responseCode, msgError );
            case 409:
                return new ConflictException( responseCode, msgError );
            case 412:
                return new PreconditionFailedException( responseCode, msgError );
            case 422:
                return new UnprocessableEntityException( responseCode, msgError );
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
                return new InternalServerErrorException( responseCode, msgError );
        }
        return new UnmappedRestException( responseCode, msgError );
    }

    /**
     * Internal execute, log headers, check for errors
     *
     * @param request The request object to be executed
     *
     * @return HttpResponse Response replied by the server
     *
     * @throws RestException
     * @throws RuntimeRestException
     */
    private HttpResponse execute( HttpUriRequest request ) throws RestException, RuntimeRestException
    {
        log.debug( ">>>> " + request.getRequestLine() );
        for ( Header header : request.getAllHeaders() )
        {
            log.trace( ">>>> " + header.getName() + ": " + header.getValue() );
        }

        HttpResponse response;
        try
        {
            response = getHttpClient().execute( request );
            log.debug( "<<<< " + response.getStatusLine() );
            for ( Header header : response.getAllHeaders() )
            {
                log.trace( "<<<< " + header.getName() + ": " + header.getValue() );
            }

            if ( response.getStatusLine().getStatusCode() == 204 )
            {
                log.debug( "Consuming quietly the response entity since server returned no content" );
                EntityUtils.consumeQuietly( response.getEntity() );
            }

            checkError( response );
            return response;
        }
        catch ( KeyManagementException | NoSuchAlgorithmException | IOException e )
        {
            throw new RuntimeRestException( e );
        }
        finally
        {
            // https://issues.apache.org/jira/browse/HTTPCLIENT-1523
            DateUtils.clearThreadLocal();
        }
    }

    private synchronized HttpClient getHttpClient() throws KeyManagementException, NoSuchAlgorithmException
    {
        if ( httpClient == null )
        {
            HttpClientBuilder httpClientBuilder = HttpClients.custom();

            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout( CONNECTION_TIMEOUT )
                    .setSocketTimeout( SOCKET_TIMEOUT ).build();

            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive( true ).setSoTimeout( SOCKET_TIMEOUT )
                    .build();

            httpClientBuilder.setDefaultRequestConfig( requestConfig ).setDefaultSocketConfig( socketConfig );

            if ( skipContentCompression ) httpClientBuilder.disableContentCompression();

            if ( skipValidation || ctx != null )
            {
                try
                {
                    if ( ctx != null )
                    {
                        httpClientBuilder.setSslcontext( ctx );
                    }
                    else
                    {
                        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial( new TrustStrategy()
                        {
                            @Override
                            public boolean isTrusted( X509Certificate[] chain, String authType )
                                    throws CertificateException
                            {
                                return true;
                            }
                        } ).build();

                        httpClientBuilder.setSslcontext( sslContext );
                    }

                    httpClientBuilder.setSSLHostnameVerifier( NoopHostnameVerifier.INSTANCE );
                }
                catch ( KeyStoreException e )
                {
                    log.warn( "Cannot setup skipValidation", e );
                }
            }



            httpClient = httpClientBuilder.build();
        }

        return httpClient;
    }

    private <T> T readObject( Class<T> clazz, HttpResponse response ) throws RuntimeRestException
    {
        parseResponseHeaders( response );
        if ( response.getEntity() == null ) return null;

        if ( clazz.isAssignableFrom( InputStream.class ) )
        {
            try
            {
                return (T) response.getEntity().getContent();
            }
            catch ( IOException ex )
            {
                throw new RuntimeRestException( ex );
            }
        }

        Header contentType = response.getEntity().getContentType();
        if ( contentType != null )
        {
            if ( contentType.getValue().contains( ContentType.APPLICATION_JSON.getMimeType() ) )
                return parseJson( clazz, response );
            if ( contentType.getValue().toLowerCase().contains( "xml" ) ) return parseXml( clazz, response );
        }
        throw new ParseException(
                "Unsupported content type " + ( contentType != null ? contentType.getValue() : "null" ) );
    }

    private <T> T parseJson( Class<T> clazz, HttpResponse response ) throws RuntimeRestException
    {
        try ( InputStream stream = response.getEntity().getContent() )
        {
            return mapper.reader( clazz ).readValue( stream );
        }
        catch ( JsonProcessingException e )
        {
            throw new ParseException( e );
        }
        catch ( IllegalStateException | IOException e1 )
        {
            throw new RuntimeRestException( e1 );
        }
    }

    private <T> T parseXml( Class<T> clazz, HttpResponse response ) throws RuntimeRestException
    {
        try ( InputStream stream = response.getEntity().getContent() )
        {
            JAXBContext contextB = getJaxbContext( clazz );
            Unmarshaller unmarshallerB = contextB.createUnmarshaller();
            try
            {
                return (T) unmarshallerB.unmarshal( stream );
            }
            catch ( JAXBException e )
            {
                throw new ParseException( e );
            }
        }
        catch ( IllegalStateException | IOException | JAXBException e1 )
        {
            throw new RuntimeRestException( e1 );
        }
    }

    private void prepareRequest( HttpRequest request, Map<String, String> newHeaders )
    {
        applyHeaders( request, newHeaders );
        if ( authenticated )
        {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( username, password );
            try
            {
                request.addHeader( new BasicScheme().authenticate( credentials, request, null ) );
            }
            catch ( AuthenticationException e )
            {
                log.warn( "it should not happens", e );
            }
        }
    }

    private <T> void writeObject( T obj, HttpEntityEnclosingRequest request ) throws ParseException
    {
        if ( obj == null ) return;
        String payload = useXml ? serializeXMLContent( obj, request ) : serializeJsonContent( obj, request );
        HttpRequestBase httpRequest = (HttpRequestBase) request;
        log.debug( "{} payload: {}", httpRequest.getMethod(), payload );
    }

    private <T> String serializeJsonContent( T obj, HttpEntityEnclosingRequest request ) throws ParseException
    {
        try
        {
            request.addHeader( "Content-type", "application/json" );
            ObjectWriter writer = mapper.writer();
            String payload = writer.writeValueAsString( obj );
            StringEntity entity = new StringEntity( payload, ContentType.APPLICATION_JSON );
            request.setEntity( entity );
            return payload;
        }
        catch ( JsonProcessingException e )
        {
            throw new ParseException( e );
        }
    }

    private <T> String serializeXMLContent( T obj, HttpEntityEnclosingRequest request ) throws ParseException
    {
        request.addHeader( "Content-type", "application/xml" );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBContext context;
        try
        {
            context = JAXBContext.newInstance( obj.getClass() );
            Marshaller m = context.createMarshaller();
            m.marshal( obj, baos );
            String payload = baos.toString();
            StringEntity entity = new StringEntity( payload, ContentType.APPLICATION_XML );
            request.setEntity( entity );
            return payload;
        }
        catch ( JAXBException e )
        {
            throw new ParseException( e );
        }
    }

    @Override
    public InputStream getData( URL url, Map<String, String> newHeaders ) throws RuntimeRestException, RestException
    {
        try
        {
            HttpGet get;
            get = new HttpGet( url.toURI() );
            prepareRequest( get, newHeaders );
            HttpResponse response = execute( get );
            return response.getEntity().getContent();
        }
        catch ( URISyntaxException | IllegalStateException | IOException e )
        {
            throw new RuntimeRestException( e );
        }
    }

    @Override
    public Map<String, List<String>> getLastResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * Use to disable content compression
     * @param disableContentCompression true if content compression should be disabled
     */
    @Override
    public void setHttpContentCompressionOverride( boolean disableContentCompression )
    {
        if ( httpClient == null ) this.skipContentCompression = disableContentCompression;
        else throw new IllegalArgumentException( "httpClient already initialized, too late to disable compression" );
    }

    /**
     * Change failure behaviour in deserialization if an unknown field is found
     * @param flag if true fail, if false ignore unknown field
     */
    @Override
    public void setObjectMapperFailOnUknownField( boolean flag )
    {
        this.mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, flag );
    }

    @Override
    public void setObjectMapper( ObjectMapper mapper )
    {
        this.mapper = mapper;
    }
}
