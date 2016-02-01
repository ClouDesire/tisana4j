package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.RuntimeRestException;
import com.cloudesire.tisana4j.test.dto.NetworkAddressDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EdgesTestIT extends BaseTestIT
{
    @Test
    public void testRedirect() throws IOException, RestException
    {
        IOUtils.closeQuietly( restClient.get( new URL( "https://httpbin.org/redirect/2" ) ) );
        IOUtils.closeQuietly( restClient.get( new URL( "https://httpbin.org/relative-redirect/2" ) ) );
        IOUtils.closeQuietly( restClient.get( new URL( "https://httpbin.org/absolute-redirect/2" ) ) );
    }

    @Test
    public void testUnmappedIgnore() throws IOException, RestException
    {
        restClient.setFailOnUknownField( false );

        final NetworkAddressDTO dto = restClient.get( new URL( "https://httpbin.org/get" ), NetworkAddressDTO.class );

        assertNotNull( dto );
        assertNotNull( dto.getOrigin() );
        assertNull( dto.getUnknownField() );
    }

    @Test ( expected = RuntimeRestException.class )
    public void testUnmappedFail() throws IOException, RestException
    {
        restClient.setFailOnUknownField( true );
        restClient.get( new URL( "https://httpbin.org/get" ), NetworkAddressDTO.class );
    }

    @Test
    public void testNoCompression() throws Exception
    {
        RestClient noCompressionClient = RestClientFactory.getNoCompressionClient();

        InputStream gzippedResponse = restClient.get( new URL( "https://httpbin.org/gzip" ), InputStream.class );

        final JsonNode gzippedJsonNode = jsonMapper.readTree( gzippedResponse );
        assertTrue( "value", gzippedJsonNode.findValue( "gzipped" ).asBoolean() );

        InputStream plainTextResponse = noCompressionClient
                .get( new URL( "https://httpbin.org/get" ), InputStream.class );

        final JsonNode plainTextJsonNode = jsonMapper.readTree( plainTextResponse );
        assertNotNull( "value", plainTextJsonNode.findValue( "headers" ) );
        assertNull( "value", plainTextJsonNode.findValue( "headers" ).findValue( "Accept-Encoding" ) );
    }
}
