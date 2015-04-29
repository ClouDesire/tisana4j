package com.cloudesire.tisana4j.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public abstract class BaseHttpRequestHandler implements HttpRequestHandler
{
	private static final ObjectMapper mapper = new ObjectMapper();

	protected <T> T getJsonBodyMessage ( HttpRequest request, Class<T> clazz ) throws Exception
	{
		if (request instanceof BasicHttpEntityEnclosingRequest)
		{
			BasicHttpEntityEnclosingRequest r = (BasicHttpEntityEnclosingRequest) request;
			try (InputStream content = r.getEntity().getContent())
			{
				return mapper.reader(clazz).readValue(content);
			}
		}
		return null;
	}

	protected void setJsonResponseEntity ( HttpResponse response, String json, int statusCode )
	{
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(new ByteArrayInputStream(json.getBytes()));
		response.setEntity(entity);
		response.setStatusCode(statusCode);
		response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
	}
}
