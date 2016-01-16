package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientBuilder;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

public class RestClientBuilderTest
{
    @Test
    public void testBuilder() throws Exception
    {
        RestClient newClient = new RestClientBuilder()
                .withUsername( "pippo" )
                .withPassword( "pasticcio" )
                .withSkipValidation( true )
                .withHeaders( new HashMap<String, String>() )
                .withCtx( SSLContext.getInstance( "SSL" ) )
                .withConnectionTimeout( 2, TimeUnit.MINUTES )
                .withSocketTimeout( 1, TimeUnit.MINUTES )
                .build();

        assertNotNull( newClient );
    }
}
