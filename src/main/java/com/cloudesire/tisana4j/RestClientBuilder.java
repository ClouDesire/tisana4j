package com.cloudesire.tisana4j;

import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestClientBuilder
{
    private String username;
    private String password;
    private boolean skipValidation;
    private Map<String, String> headers;
    private Integer connectionTimeout;
    private Integer socketTimeout;
    private String proxyHostname;
    private int proxyPort = 8080;
    private String proxyScheme = "http";

    public RestClientBuilder withUsername( String username )
    {
        this.username = username;
        return this;
    }

    public RestClientBuilder withPassword( String password )
    {
        this.password = password;
        return this;
    }

    public RestClientBuilder withSkipValidation( boolean skipValidation )
    {
        this.skipValidation = skipValidation;
        return this;
    }

    public RestClientBuilder withHeaders( Map<String, String> headers )
    {
        this.headers = headers;
        return this;
    }

    public RestClientBuilder withCtx( SSLContext ctx )
    {
        return this;
    }

    public RestClientBuilder withConnectionTimeout( int timeOut, TimeUnit timeUnit )
    {
        connectionTimeout = (int) (long) TimeUnit.MILLISECONDS.convert( timeOut, timeUnit );
        return this;
    }

    public RestClientBuilder withSocketTimeout( int timeOut, TimeUnit timeUnit )
    {
        socketTimeout = (int) (long) TimeUnit.MILLISECONDS.convert( timeOut, timeUnit );
        return this;
    }

    public RestClientBuilder withProxyHostname( String proxyHostname )
    {
        this.proxyHostname = proxyHostname;
        return this;
    }

    public RestClientBuilder withProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
        return this;
    }

    public RestClientBuilder withProxyScheme( String proxyScheme )
    {
        this.proxyScheme = proxyScheme;
        return this;
    }

    public RestClient build()
    {
        return new RestClient( this );
    }

    String getUsername()
    {
        return username;
    }

    String getPassword()
    {
        return password;
    }

    boolean getSkipValidation()
    {
        return skipValidation;
    }

    Map<String, String> getHeaders()
    {
        return headers;
    }

    Integer getConnectionTimeout()
    {
        return connectionTimeout;
    }

    Integer getSocketTimeout()
    {
        return socketTimeout;
    }

    String getProxyHostname()
    {
        return proxyHostname;
    }

    int getProxyPort()
    {
        return proxyPort;
    }

    String getProxyScheme()
    {
        return proxyScheme;
    }
}
