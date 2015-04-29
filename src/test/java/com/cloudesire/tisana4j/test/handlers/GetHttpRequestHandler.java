package com.cloudesire.tisana4j.test.handlers;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class GetHttpRequestHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
		try
		{
			if (!request.getRequestLine().getMethod().equals("GET")) throw new Exception("Not a GET");
			String[] uriArray = request.getRequestLine().getUri().split("/");
			String json = "{ \"id\": " + uriArray[2] + " }";
			setJsonResponseEntity(response, json, 200);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}
	}

}
