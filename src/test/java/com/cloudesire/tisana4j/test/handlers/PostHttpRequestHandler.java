package com.cloudesire.tisana4j.test.handlers;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
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
			setJsonResponseEntity(response, json, 201);
		} catch (JsonMappingException e)
		{
			// empty body request
			response.setStatusCode(200);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}
	}
}
