package com.cloudesire.tisana4j.test.dto;

import java.util.Map;

public class HttpBinRequestDTO
{
    private Map<String, String> args;
    private String data;
    private Map<String, String> headers;
    private String origin;
    private String url;

    public Map<String, String> getArgs()
    {
        return args;
    }

    public void setArgs( Map<String, String> args )
    {
        this.args = args;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders( Map<String, String> headers )
    {
        this.headers = headers;
    }

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin( String origin )
    {
        this.origin = origin;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getData()
    {
        return data;
    }

    public void setData( String data )
    {
        this.data = data;
    }
}
