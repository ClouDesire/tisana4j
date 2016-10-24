package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.RestClientBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class ProxyTestIT extends BaseTestIT
{
    @Test
    @Ignore("docker run --name squid --publish 3128:3128 sameersbn/squid:3.3.8-20")
    public void proxySupport() throws Exception
    {
        restClient = new RestClientBuilder()
                .withProxyHostname( "localhost" )
                .withProxyPort( 3128 )
                .build();

        InputStream response = restClient.get( new URL( "http://www.repubblica.it" ), InputStream.class );
        final String string = IOUtils.toString( response );
        assertNotNull( string );
    }
}
