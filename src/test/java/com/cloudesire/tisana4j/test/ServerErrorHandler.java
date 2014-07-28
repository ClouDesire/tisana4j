package com.cloudesire.tisana4j.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class ServerErrorHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
			String[] uriArray = request.getRequestLine().getUri().split("/");
			String json = "{ \"Error\": \"Internal Server Error\" }";
			setResponseEntity(response, json, Integer.valueOf(uriArray[2]));
	}

}
