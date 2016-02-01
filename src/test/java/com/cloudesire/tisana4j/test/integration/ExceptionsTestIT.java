package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.exceptions.BadRequestException;
import com.cloudesire.tisana4j.exceptions.InternalServerErrorException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.UnauthorizedException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionsTestIT extends BaseTestIT
{
    @Test( expected = BadRequestException.class )
    public void test400() throws IOException, RestException
    {
        restClient.get( new URL( "https://httpbin.org/status/400" ) );
    }

    @Test( expected = UnauthorizedException.class )
    public void test401() throws IOException, RestException
    {
        restClient.get( new URL( "https://httpbin.org/status/401" ) );
    }

    @Test( expected = InternalServerErrorException.class )
    public void test501() throws IOException, RestException
    {
        restClient.get( new URL( "https://httpbin.org/status/501" ) );
    }

    @Test
    public void testUnhandled()
    {
        try
        {
            restClient.get( new URL( "https://httpbin.org/status/418" ) );
        }
        catch ( RestException e )
        {
            assertEquals(418, e.getResponseCode());
            assertTrue("expected error response not found", e.getMessage().contains( "teapot" ));
        }
        catch ( IOException e )
        {
            fail("got exception");
        }
    }
}
