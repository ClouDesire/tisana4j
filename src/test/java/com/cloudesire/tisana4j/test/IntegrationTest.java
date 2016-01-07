package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.Pair;
import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.test.dto.NetworkAddressDTO;
import com.cloudesire.tisana4j.test.dto.SlideShowDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void testJson() throws Exception
    {
        RestClient client = RestClientFactory.getDefaultClient();

        NetworkAddressDTO testClass = client.get( new URL( "https://httpbin.org/ip" ), NetworkAddressDTO.class );

        assertNotNull( testClass );
        assertNotNull( testClass.getOrigin() );
    }

    @Test
    public void testXML() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultXmlClient();

        final SlideShowDTO response = client.get( new URL( "https://httpbin.org/xml" ), SlideShowDTO.class );

        assertNotNull(response.getAuthor());
        assertNotNull(response.getTitle());
        assertNotNull(response.getDate());
        assertNotNull(response.getSlide());
        for ( SlideShowDTO.Slide slide : response.getSlide() )
        {
            assertNotNull( slide.getType() );
            assertNotNull( slide.getTitle() );
        }
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
        List<Pair> values = new ArrayList<>();
        values.add( new Pair( "key", "value" ) );

        InputStream response = client.postFormData( new URL( "https://httpbin.org/post" ), values, InputStream.class );

        final JsonNode jsonNode = jsonMapper.readTree( response );
        assertEquals( "value", jsonNode.findValue( "form" ).findValue( "key" ).asText() );
    }

    @Test
    public void testRedirect() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        client.get( new URL("https://httpbin.org/redirect/2") );
    }

}
