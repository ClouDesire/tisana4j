package com.cloudesire.tisana4j.test.handlers;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class GetCollectionHttpRequestHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
		try
		{
			if (!request.getRequestLine().getMethod().equals("GET")) throw new Exception("Not a GET");
			String json = "[{ \"id\": 15 }]";
			setJsonResponseEntity(response, json, 200);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}

	}

}
