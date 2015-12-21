package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.RestClient;
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

import static org.junit.Assert.assertNotNull;

public class IntegrationTest
{
    private ObjectMapper jsonMapper = new ObjectMapper();

    private static class NetworkAddress {
        private String origin;

        public String getOrigin() {
            return origin;
        }
    }

    @Test
    public void test() throws Exception
    {
        RestClient client = new RestClient();

        NetworkAddress testClass = client.get(
                new URL("https://httpbin.org/ip"),
                NetworkAddress.class);

        assertNotNull( testClass );
        assertNotNull( testClass.getOrigin() );
    }

    @Test
    public void testPostForm() throws IOException, RestException
    {
        RestClient client = new RestClient();
        List<BasicNameValuePair> values = new ArrayList<>();
        values.add( new BasicNameValuePair( "key", "value" ) );

        InputStream response = client.postFormData( new URL( "https://httpbin.org/post" ), values, InputStream.class );

        final JsonNode jsonNode = jsonMapper.readTree( response );
        assertNotNull( jsonNode.findValue( "form" ) );
    }


    @Test( expected = ResourceNotFoundException.class )
    public void sniCustomCert() throws IOException, RestException
    {
        RestClient client = new RestClient( true );
        client.get(
                new URL("https://ines-gtt-test.liberologico.com/asd")
        );
    }

    @Test( expected = ResourceNotFoundException.class )
    public void testNoSni() throws IOException, RestException
    {
        RestClient client = new RestClient( true );
        client.get(
                    new URL("https://web001.liberologico.com/asd")
        );
    }

    @Test
    public void testCloudFlareSNI() throws IOException, RestException
    {
        RestClient client = new RestClient( false );
        client.get(
                new URL("https://cloudesire.cloud/")
        );
    }
}
