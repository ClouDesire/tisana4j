package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class SniTest
{
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
}
