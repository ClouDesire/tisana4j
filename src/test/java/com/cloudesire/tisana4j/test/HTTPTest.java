package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.ExceptionTranslator;
import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.AccessDeniedException;
import com.cloudesire.tisana4j.exceptions.BadRequestException;
import com.cloudesire.tisana4j.exceptions.InternalServerErrorException;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTTPTest
{
    private final static Logger log = LoggerFactory.getLogger( HTTPTest.class );
    private final RestClient client = RestClientFactory.getDefaultClient();
    private MockWebServer server;
    private String serverUrl = "";

    @Before
    public void setUp() throws Exception
    {
        server = new MockWebServer();
    }

    @Test
    public void testGetData() throws Exception
    {
        String data = "ColumnA,ColumnB,ColumnC\r\nA,B,c\r\n,a,b,c\r\n";
        MockResponse response = new MockResponse();
        response.setResponseCode( 200 );
        response.addHeader( "Content-Type", "text/csv" );
        response.setBody( data );
        server.enqueue( response );
        server.start( 9999 );

        InputStream stream = client.getData( server.getUrl( "/get/csv" ), null );
        byte[] b = new byte[1024];
        int r = stream.read( b );
        assertTrue( r > 0 );
        String out = new String( b );
        String[] lines = out.split( "\r\n" );
        assertEquals( 4, lines.length );
    }

    @Test
    public void testDelete() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 204 );
        server.enqueue( response );
        server.start( 9999 );

        client.delete( server.getUrl( "/delete/15" ) );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/delete/15", request.getPath() );
        assertEquals( "DELETE", request.getMethod() );
    }

    @Test
    public void testPatch() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 204 );
        server.enqueue( response );
        server.start( 9999 );

        Map<String, String> map = new HashMap<>();
        map.put( "action", "doThings" );

        client.patch( server.getUrl( "/patch/15" ), map );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/patch/15", request.getPath() );
        assertEquals( "PATCH", request.getMethod() );
        assertTrue( request.getBody().size() > 0 );
    }

    @Test
    public void testGet() throws Exception
    {
        final int resourceId = 15;
        String json = "{ \"id\": " + resourceId + " }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 200 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        Resource resource = client.get( server.getUrl( "/resource/" + resourceId ), Resource.class );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/resource/" + resourceId, request.getPath() );
        assertEquals( "GET", request.getMethod() );
        assertEquals( 0, request.getBody().size() );
        assertNotNull( resource.getId() );
        assertTrue( resource.getId().equals( resourceId ) );
    }

    @Test
    public void testGetRaw() throws Exception
    {
        final int resourceId = 15;
        String json = "{ \"id\": " + resourceId + " }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 200 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        InputStream inputStream = client.get( server.getUrl( "/resource/" + resourceId ) );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/resource/" + resourceId, request.getPath() );
        assertEquals( "GET", request.getMethod() );
        assertEquals( 0, request.getBody().size() );
        assertNotNull( response );
        String s = IOUtils.toString( inputStream, StandardCharsets.UTF_8.toString() );
        assertNotNull( s );
        assertEquals( "{ \"id\": 15 }", s );
    }

    @Test
    public void testGetCollection() throws Exception
    {
        String json = "[{ \"id\": 15 }]";

        MockResponse response = new MockResponse();
        response.setResponseCode( 200 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        List<Resource> collection = client.getCollection( server.getUrl( "/resources" ), Resource.class );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/resources", request.getPath() );
        assertEquals( "GET", request.getMethod() );
        assertEquals( 0, request.getBody().size() );
        assertFalse( collection.isEmpty() );
        assertEquals( "15", collection.get( 0 ).id.toString() );
    }

    @Test
    public void testPost() throws Exception
    {
        final int resourceId = 15;
        String json = "{ \"id\": " + resourceId + " }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 201 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        Resource resource = new Resource();
        resource.setId( resourceId );
        Resource postResponse = client.post( server.getUrl( "/create" ), resource );

        RecordedRequest request = server.takeRequest();
        assertEquals( "/create", request.getPath() );
        assertEquals( "POST", request.getMethod() );
        assertTrue( request.getBody().size() > 0 );
        assertNotNull( postResponse.getId() );
        assertEquals( resourceId, postResponse.getId().intValue() );
    }

    @Test
    public void testEmptyPostEmptyResponse() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 201 );
        server.enqueue( response );
        server.start( 9999 );

        Resource postResponse = client.post( server.getUrl( "/create" ), null, null, null );
        RecordedRequest request = server.takeRequest();
        assertEquals( 0, request.getBody().size() );
        assertEquals( "/create", request.getPath() );
        assertEquals( "POST", request.getMethod() );
        assertNull( postResponse );
    }

    @Test
    public void testPut() throws Exception
    {
        final int resourceId = 15;
        String json = "{ \"id\": " + resourceId + " }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 200 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        Resource resource = new Resource();
        resource.setId( resourceId );
        Resource putResponse = client.put( server.getUrl( "/update/" + resourceId ), resource );

        RecordedRequest request = server.takeRequest();
        assertTrue( request.getBody().size() > 0 );
        assertEquals( "/update/" + resourceId, request.getPath() );
        assertEquals( "PUT", request.getMethod() );
        assertNotNull( putResponse.getId() );
        assertTrue( putResponse.getId().equals( resourceId ) );
    }

    @Test
    public void testInternalServerError() throws Exception
    {
        String json = "{ \"error\": \"Customized Internal Server Error\" }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 500 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        try
        {
            client.get( server.getUrl( "/fail/500" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof InternalServerErrorException ) ) fail();
            RestException re = (RestException) e;
            assertEquals( 500, re.getResponseCode() );
            assertEquals( "{ \"error\": \"Customized Internal Server Error\" }", re.getMessage() );
        }
    }

    @Test
    public void testBadRequestError() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 400 );
        server.enqueue( response );
        server.start( 9999 );

        try
        {
            client.get( server.getUrl( "/fail/400" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof BadRequestException ) ) fail();
        }
    }

    @Test
    public void testAccessDeniedError() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 403 );
        server.enqueue( response );
        server.start( 9999 );

        try
        {
            client.get( server.getUrl( "/fail/403" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof AccessDeniedException ) ) fail();
        }
    }

    @Test
    public void testResourceNotFoundError() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 404 );
        server.enqueue( response );
        server.start( 9999 );

        try
        {
            client.get( server.getUrl( "/fail/404" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof ResourceNotFoundException ) ) fail();
        }
    }

    @Test
    public void testUnprocessableEntityError() throws Exception
    {
        MockResponse response = new MockResponse();
        response.setResponseCode( 422 );
        server.enqueue( response );
        server.start( 9999 );

        try
        {
            client.get( server.getUrl( "/fail/422" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof UnprocessableEntityException ) ) fail();
        }
    }

    @Test
    public void testTranslateError() throws Exception
    {
        String json = "{ \"error\": \"Customized Internal Server Error\" }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 500 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        RestClient client2 = new RestClient( true );
        client2.setExceptionTranslator( new TestExceptionTranslator() );

        try
        {
            client2.get( server.getUrl( "/fail/500" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof RestException ) ) fail();
            RestException re = (RestException) e;
            assertEquals( 500, re.getResponseCode() );
            assertEquals( "Customized Internal Server Error", re.getMessage() );
        }
    }

    @Test
    public void testTranslateError2() throws Exception
    {
        String json = "{ \"error\": \"Customized Bad Request\" }";

        MockResponse response = new MockResponse();
        response.setResponseCode( 400 );
        response.addHeader( "Content-Type", "application/json" );
        response.setBody( json );
        server.enqueue( response );
        server.start( 9999 );

        RestClient client2 = new RestClient( true );
        client2.setExceptionTranslator( new TestExceptionTranslator() );
        try
        {
            client2.get( server.getUrl( "/fail/400" ), Resource.class );
            fail();
        }
        catch ( Exception e )
        {
            if ( !( e instanceof BadRequestException ) ) fail();
            BadRequestException bre = (BadRequestException) e;
            assertEquals( 400, bre.getResponseCode() );
            assertEquals( "Customized Bad Request", bre.getMessage() );
        }
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            server.shutdown();
        }
        catch ( IllegalStateException e )
        {
            log.info( "Mock server not started" );
        }
    }

    public static class Resource
    {
        private Integer id;

        public Integer getId()
        {
            return id;
        }

        public void setId( Integer id )
        {
            this.id = id;
        }
    }

    public static class TestExceptionTranslator implements ExceptionTranslator
    {

        @SuppressWarnings ( "unchecked" )
        @Override
        public RestException translateException( int responseCode, String responseMessage, String errorStream,
                ResponseMessage returnMessageRef, Header[] headers )
        {
            if ( responseCode == 400 )
            {
                returnMessageRef.setResponse( "Customized Bad Request" );
                return null;
            }
            if ( responseCode != 500 ) return null;
            ObjectMapper mapper = new ObjectMapper();
            try
            {
                ErrorDto obj = mapper.reader( ErrorDto.class ).readValue( errorStream );
                return new RestException( responseCode, obj.getError() );
            }
            catch ( Exception e )
            {
                return null;
            }
        }

    }

    public static class ErrorDto
    {
        private String error;

        public ErrorDto()
        {
        }

        public ErrorDto( String errorMsg )
        {
            this.error = errorMsg;
        }

        public String getError()
        {
            return error;
        }

        public void setError( String errorMsg )
        {
            this.error = errorMsg;
        }

    }

}
