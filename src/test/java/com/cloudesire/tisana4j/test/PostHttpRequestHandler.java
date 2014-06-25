package com.cloudesire.tisana4j.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import com.cloudesire.tisana4j.test.HTTPTest.Resource;

public class PostHttpRequestHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
		try
		{
			if (!request.getRequestLine().getMethod().equals("POST")) throw new Exception("Not a POST");
			Resource r = getJsonBodyMessage(request, Resource.class);
			String json = "{ \"id\": " + r.getId() + " }";
			setResponseEntity(response, json, 201);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}

	}

}
