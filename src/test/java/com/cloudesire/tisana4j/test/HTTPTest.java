package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.ExceptionTranslator;
import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HTTPTest
{
	public static class Resource
	{
		private Integer id;

		public Integer getId ()
		{
			return id;
		}

		public void setId ( Integer id )
		{
			this.id = id;
		}
	}

	public static class TestExceptionTranslator implements ExceptionTranslator
	{

		@SuppressWarnings ( "unchecked" )
		@Override
		public RestException translateException ( int responseCode, String responseMessage,
				String errorStream, ResponseMessage returnMessageRef, Header[] headers )
		{
			if(responseCode == 400)
			{
				returnMessageRef.setResponse("Customized Bad Request");
				return null;
			}
			if (responseCode != 500) return null;
			ObjectMapper mapper = new ObjectMapper();
			try
			{
				ErrorDto obj = mapper.reader(ErrorDto.class).readValue(errorStream);
				return new RestException(responseCode, obj.getError());
			} catch (Exception e)
			{
				return null;
			}
		}

	}

	public static class ErrorDto
	{
		private String error;

		public ErrorDto()
		{
		}

		public ErrorDto(String errorMsg)
		{
			this.error = errorMsg;
		}

		public String getError ()
		{
			return error;
		}

		public void setError ( String errorMsg )
		{
			this.error = errorMsg;
		}

	}

	private LocalTestServer server = null;
	private String serverUrl;
	private final static Logger log = LoggerFactory.getLogger(HTTPTest.class);

	private final HttpRequestHandler csvHttpRequest = new GetCsvHttpRequestHandler();
	private final HttpRequestHandler deleteHandler = new DeleteHttpRequestHandler();
	private final HttpRequestHandler getHandler = new GetHttpRequestHandler();
	private final HttpRequestHandler getCollectionHandler = new GetCollectionHttpRequestHandler();
	private final HttpRequestHandler postHandler = new PostHttpRequestHandler();
	private final HttpRequestHandler putHandler = new PutHttpRequestHandler();
	private final HttpRequestHandler patchHandler = new PatchHttpRequestHandler();
	private final HttpRequestHandler serverErrorHandler = new ServerErrorHandler();
	private final RestClient client = new RestClient(true);

	@Before
	public void setUp () throws Exception
	{
		server = new LocalTestServer(null, null);
		server.register("/delete/*", deleteHandler);
		server.register("/resource/*", getHandler);
		server.register("/resources/*", getCollectionHandler);
		server.register("/create/*", postHandler);
		server.register("/update/*", putHandler);
		server.register("/get/csv", csvHttpRequest);
		server.register("/patch/*", patchHandler);
		server.register("/fail/*", serverErrorHandler);
		server.start();

		// report how to access the server
		serverUrl = "http://" + server.getServiceAddress().getHostName() + ":" + server.getServiceAddress().getPort();
		log.info("LocalTestServer available at " + serverUrl);
	}

	@Test
	public void testGetData() throws Exception
	{
		InputStream stream = client.getData(new URL(serverUrl+"/get/csv"), null);
		byte[] b = new byte[1024];
		int r = stream.read(b);
		assertTrue(r > 0 );
		String out = new String(b);
		String[] lines = out.split("\r\n");
		assertEquals(4,lines.length);
	}

	@Test
	public void testDelete () throws Exception
	{
		client.delete(new URL(serverUrl + "/delete/15"));
	}

	@Test
	@Ignore
	// not supported by Apache LocalTestServer
	public void testPatch () throws Exception
	{
		Map<String, String> map = new HashMap<>();
		map.put("action", "doThings");
		client.patch(new URL(serverUrl + "/patch/15"), map);
	}

	@Test
	public void testGet () throws Exception
	{
		final int resourceId = 15;
		Resource response = client.get(new URL(serverUrl + "/resource/" + resourceId), Resource.class);
		assertNotNull(response.getId());
		assertTrue(response.getId().equals(resourceId));
	}

	@Test
	public void testGetRaw () throws Exception
	{
		final int resourceId = 15;
		InputStream response = client.get(new URL(serverUrl + "/resource/" + resourceId));
		assertNotNull(response);
		String s = IOUtils.toString(response, StandardCharsets.UTF_8.toString());
		assertNotNull(s);
		assertEquals("{ \"id\": 15 }", s);
	}

	@Test
	public void testGetCollection () throws Exception
	{
		List<Resource> collection = client.getCollection(new URL(serverUrl + "/resources/"), Resource.class);
		assertFalse(collection.isEmpty());
		assertNotNull(collection.get(0));
	}

	@Test
	public void testPost () throws Exception
	{
		final int resourceId = 15;
		Resource resource = new Resource();
		resource.setId(resourceId);
		Resource response = client.post(new URL(serverUrl + "/create/"), resource);
		assertNotNull(response.getId());
		assertTrue(response.getId().equals(resourceId));
	}

	@Test
	public void testEmptyPostEmptyResponse () throws Exception
	{
		Resource response = client.post(new URL(serverUrl + "/create/"), null, null, null);
		assertNull(response);
	}

	@Test
	public void testPut () throws Exception
	{
		final int resourceId = 15;
		Resource resource = new Resource();
		resource.setId(resourceId);
		Resource response = client.put(new URL(serverUrl + "/update/" + resourceId), resource);
		assertNotNull(response.getId());
		assertTrue(response.getId().equals(resourceId));
	}

	@Test
	public void testInternalServerError () throws Exception
	{
		try
		{
			client.get(new URL(serverUrl + "/fail/500"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof InternalServerErrorException)) fail();
			RestException re = (RestException) e;
			assertEquals(500,re.getResponseCode());
			assertEquals("{ \"error\": \"Customized Internal Server Error\" }",re.getMessage());
		}
	}
	@Test
	public void testBadRequestError () throws Exception
	{
		try
		{
			client.get(new URL(serverUrl + "/fail/400"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof BadRequestException)) fail();
		}
	}
	@Test
	public void testAccesDeniedError () throws Exception
	{
		try
		{
			client.get(new URL(serverUrl + "/fail/403"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof AccessDeniedException)) fail();
		}
	}
	@Test
	public void testResourceNotFoundError () throws Exception
	{
		try
		{
			client.get(new URL(serverUrl + "/fail/404"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof ResourceNotFoundException)) fail();
		}
	}
	@Test
	public void testUnprocessableEntityError () throws Exception
	{
		try
		{
			client.get(new URL(serverUrl + "/fail/422"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof UnprocessableEntityException)) fail();
		}
	}

	@Test
	public void testTranslateError () throws Exception
	{
		RestClient client2 = new RestClient(true);
		client2.setExceptionTranslator(new TestExceptionTranslator());

		try
		{
			client2.get(new URL(serverUrl + "/fail/500"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof RestException)) fail();
			RestException re = (RestException) e;
			assertEquals(500, re.getResponseCode());
			assertEquals("Customized Internal Server Error", re.getMessage());
		}
	}

	@Test
	public void testTranslateError2 () throws Exception
	{
		RestClient client2 = new RestClient(true);
		client2.setExceptionTranslator(new TestExceptionTranslator());
		try
		{
			client2.get(new URL(serverUrl + "/fail/400"), Resource.class);
			fail();
		} catch (Exception e)
		{
			if (!(e instanceof BadRequestException)) fail();
			BadRequestException bre = (BadRequestException) e;
			assertEquals(400, bre.getResponseCode());
			assertEquals("Customized Bad Request", bre.getMessage());
		}
	}

	@After
	public void tearDown () throws Exception
	{
		server.stop();
	}

}
