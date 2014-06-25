package com.cloudesire.tisana4j.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class DeleteHttpRequestHandler extends BaseHttpRequestHandler
{

	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException,
			IOException
	{
		if (request.getRequestLine().getMethod().equals("DELETE")) response.setStatusCode(204);
		else response.setStatusCode(500);
	}

}
