package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.Pair;
import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.test.dto.HttpBinRequestDTO;
import com.cloudesire.tisana4j.test.dto.SlideShowDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CrudTestIT extends BaseTestIT
{
    @Test
    public void testGetJson() throws Exception
    {
        HttpBinRequestDTO response = restClient.get( new URL( "https://httpbin.org/get" ), HttpBinRequestDTO.class );

        assertNotNull( response );
        assertNotNull( response.getOrigin() );
    }

    @Test
    public void testGetJsonWithTypeReference() throws Exception
    {
        Map<String, Object> response = restClient
                .get( new URL( "https://httpbin.org/get" ), new TypeReference<Map<String, Object>>(){} );

        assertNotNull( response );
        assertNotNull( response.get( "origin" ) );
    }

    @Test
    public void testPostJson() throws Exception
    {
        String payload = "{}";

        HttpBinRequestDTO response = restClient
                .post( new URL( "https://httpbin.org/post" ), payload, null, HttpBinRequestDTO.class );

        assertNotNull( response );
        assertNotNull( response.getOrigin() );
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
    public void testPutJson() throws Exception
    {
        String payload = "{}";

        HttpBinRequestDTO response = restClient
                .put( new URL( "https://httpbin.org/put" ), payload, null, HttpBinRequestDTO.class );

        assertNotNull( response );
        assertNotNull( response.getOrigin() );
    }

    @Test
    public void testDeleteJson() throws Exception
    {
        restClient.delete( new URL( "https://httpbin.org/delete" ) );
    }

    @Test
    public void testGetXML() throws IOException, RestException
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
    public void errorHttpAlreadyBuilt() throws Exception
    {
        testDeleteJson();
        try
        {
            restClient.setProxyHostname( "whatever" );
        }
        catch ( IllegalArgumentException ex )
        {
            return;
        }
        fail();
    }
}
