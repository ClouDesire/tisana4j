package com.cloudesire.tisana4j;

public class RestClientFactory
{
    public static RestClient getDefaultClient()
    {
        return new RestClient();
    }

    public static RestClient getNoValidationClient()
    {
        return new RestClient(true);
    }

    public static RestClient getNoCompressionClient()
    {
        final RestClient client = getDefaultClient();
        client.setSkipContentCompression( true );
        return client;
    }
}
