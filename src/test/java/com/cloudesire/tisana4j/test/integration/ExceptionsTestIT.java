package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.exceptions.BadRequestException;
import com.cloudesire.tisana4j.exceptions.InternalServerErrorException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.UnauthorizedException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ExceptionsTestIT extends BaseTestIT
{
    @Test
    public void test400() throws IOException, RestException
    {
        testException( "https://httpbin.org/status/400", "BAD REQUEST", BadRequestException.class );
    }

    @Test
    public void test401() throws IOException, RestException
    {
        testException( "https://httpbin.org/status/401", "UNAUTHORIZED", UnauthorizedException.class );
    }

    @Test
    public void test501() throws IOException, RestException
    {
        testException( "https://httpbin.org/status/501", "NOT IMPLEMENTED", InternalServerErrorException.class );
    }

    @Test
    public void testUnhandled()
    {
        testException( "https://httpbin.org/status/418", TEAPOT, RestException.class );
    }

    private void testException( final String URL, String expectedMessage, Class<? extends Exception> expectedException )
    {
        Throwable thrown = catchThrowable( new ThrowableAssert.ThrowingCallable()
        {
            @Override
            public void call() throws Throwable
            {
                restClient.get( new URL( URL ) );
            }
        } );
        assertThat( thrown ).isInstanceOf( expectedException ).hasMessage( expectedMessage );
    }

    private static final String TEAPOT =
                "\n" +
                "    -=[ teapot ]=-\n" +
                "\n" + "       _...._\n" +
                "     .'  _ _ `.\n" +
                "    | .\"` ^ `\". _,\n" +
                "    \\_;`\"---\"`|//\n" +
                "      |       ;/\n" +
                "      \\_     _/\n" +
                "        `\"\"\"`\n";
}
