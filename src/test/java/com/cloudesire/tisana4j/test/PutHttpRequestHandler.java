package com.cloudesire.tisana4j.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import com.cloudesire.tisana4j.test.HTTPTest.Resource;

public class PutHttpRequestHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
		try
		{
			if (!request.getRequestLine().getMethod().equals("PUT")) throw new Exception("Not a PUT");
			Resource r = getJsonBodyMessage(request, Resource.class);
			String json = "{ \"id\": " + r.getId() + " }";
			setResponseEntity(response, json, 200);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}

	}

}
