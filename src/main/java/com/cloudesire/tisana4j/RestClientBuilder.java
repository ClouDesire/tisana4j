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
    private SSLContext ctx;
    private Integer connectionTimeout;
    private Integer socketTimeout;

    public RestClientBuilder withUsername(String username)
    {
        this.username = username;
        return this;
    }

    public RestClientBuilder withPassword(String password)
    {
        this.password = password;
        return this;
    }

    public RestClientBuilder withSkipValidation(boolean skipValidation)
    {
        this.skipValidation = skipValidation;
        return this;
    }

    public RestClientBuilder withHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }

    public RestClientBuilder withCtx(SSLContext ctx)
    {
        this.ctx = ctx;
        return this;
    }

    public RestClientBuilder withConnectionTimeout(int timeOut, TimeUnit timeUnit)
    {
        connectionTimeout = (int) (long) TimeUnit.MILLISECONDS.convert(timeOut, timeUnit);
        return this;
    }

    public RestClientBuilder withSocketTimeout(int timeOut, TimeUnit timeUnit)
    {
        socketTimeout = (int) (long) TimeUnit.MILLISECONDS.convert(timeOut, timeUnit);
        return this;
    }

    public RestClient build()
    {
        return new RestClient(this);
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public boolean getSkipValidation()
    {
        return skipValidation;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public SSLContext getCtx()
    {
        return ctx;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }
}
