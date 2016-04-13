package com.cloudesire.tisana4j;

public class RestClientFactory
{
    public static RestClient getDefaultClient()
    {
        return new RestClient();
    }

    public static RestClient getDefaultXmlClient()
    {
        final RestClient client = getDefaultClient();
        client.setUseXml( true );
        return client;
    }

    public static RestClient getNoValidationClient()
    {
        return new RestClient( true );
    }

    public static RestClient getNoCompressionClient()
    {
        final RestClient client = getDefaultClient();
        client.setHttpContentCompressionOverride( true );
        return client;
    }
}
