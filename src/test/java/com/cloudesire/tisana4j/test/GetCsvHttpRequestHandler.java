package com.cloudesire.tisana4j.test;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

public class GetCsvHttpRequestHandler extends BaseHttpRequestHandler
{
	@Override
	public void handle ( HttpRequest request, HttpResponse response, HttpContext context ) throws HttpException, IOException
	{
		try
		{
			String r = "ColumnA,ColumnB,ColumnC\r\nA,B,c\r\n,a,b,c\r\n";
			HttpEntity entity = new StringEntity(r);
			if (!request.getRequestLine().getMethod().equals("GET")) throw new Exception("Not a POST");
			response.addHeader("Content-Type", "text/csv");
			response.setEntity(entity);
		} catch (Exception e)
		{
			response.setStatusCode(500);
		}
	}
}
