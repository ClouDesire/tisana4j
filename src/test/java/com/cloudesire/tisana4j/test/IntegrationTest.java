package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTest
{
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void test() throws Exception
    {
        RestClient client = RestClientFactory.getDefaultClient();

        NetworkAddress testClass = client.get( new URL( "https://httpbin.org/ip" ), NetworkAddress.class );

        assertNotNull( testClass );
        assertNotNull( testClass.getOrigin() );
    }

    @Test
    public void testNoCompression() throws Exception
    {
        RestClient defaultClient = RestClientFactory.getDefaultClient();
        RestClient noCompressionClient = RestClientFactory.getNoCompressionClient();

        InputStream gzippedResponse = defaultClient.get( new URL( "https://httpbin.org/gzip" ), InputStream.class );
        final JsonNode gzippedJsonNode = jsonMapper.readTree( gzippedResponse );
        assertTrue( "value", gzippedJsonNode.findValue( "gzipped" ).asBoolean() );


        InputStream plainTextResponse = noCompressionClient.get( new URL( "https://httpbin.org/get" ), InputStream.class );
        final JsonNode plainTextJsonNode = jsonMapper.readTree( plainTextResponse );
        assertNotNull( "value", plainTextJsonNode.findValue( "headers" ) );
        assertNull( "value", plainTextJsonNode.findValue( "headers" ).findValue( "Accept-Encoding" ) );
    }

    @Test
    public void testPostForm() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        List<BasicNameValuePair> values = new ArrayList<>();
        values.add( new BasicNameValuePair( "key", "value" ) );

        InputStream response = client.postFormData( new URL( "https://httpbin.org/post" ), values, InputStream.class );

        final JsonNode jsonNode = jsonMapper.readTree( response );
        assertEquals( "value", jsonNode.findValue( "form" ).findValue( "key" ).asText() );
    }

    @Test ( expected = ResourceNotFoundException.class )
    public void sniCustomCert() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://ines-gtt-test.liberologico.com/asd" ) );
    }

    @Test ( expected = ResourceNotFoundException.class )
    public void testNoSni() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://web001.liberologico.com/asd" ) );
    }

    @Test
    public void testCloudFlareSNI() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        client.get( new URL( "https://cloudesire.cloud/" ) );
    }

    private static class NetworkAddress
    {
        private String origin;

        public String getOrigin()
        {
            return origin;
        }
    }
}
