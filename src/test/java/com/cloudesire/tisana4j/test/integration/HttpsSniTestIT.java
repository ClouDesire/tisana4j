package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class HttpsSniTestIT
{
    @Test ( expected = ResourceNotFoundException.class )
    public void sniCustomCert() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://ines-gtt-test.liberologico.com/tisana4j-integration-test" ) );
    }

    @Test ( expected = ResourceNotFoundException.class )
    public void testNoSni() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://web001.liberologico.com/tisana4j-integration-test" ) );
    }

    @Test
    public void testCloudFlareSNI() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        client.get( new URL( "https://cloudesire.cloud/" ) );
    }
}
