package com.cloudesire.tisana4j.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudesire.tisana4j.RestClient;

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

	private LocalTestServer server = null;
	private String serverUrl;
	private final static Logger log = LoggerFactory.getLogger(HTTPTest.class);

	private final HttpRequestHandler deleteHandler = new DeleteHttpRequestHandler();
	private final HttpRequestHandler getHandler = new GetHttpRequestHandler();
	private final HttpRequestHandler getCollectionHandler = new GetCollectionHttpRequestHandler();
	private final HttpRequestHandler postHandler = new PostHttpRequestHandler();
	private final HttpRequestHandler putHandler = new PutHttpRequestHandler();
	private final HttpRequestHandler patchHandler = new PatchHttpRequestHandler();
	RestClient client = new RestClient(true);

	@Before
	public void setUp () throws Exception
	{
		server = new LocalTestServer(null, null);
		server.register("/delete/*", deleteHandler);
		server.register("/resource/*", getHandler);
		server.register("/resources/*", getCollectionHandler);
		server.register("/create/*", postHandler);
		server.register("/update/*", putHandler);
		server.register("/patch/*", patchHandler);
		server.start();

		// report how to access the server
		serverUrl = "http://" + server.getServiceAddress().getHostName() + ":" + server.getServiceAddress().getPort();
		log.info("LocalTestServer available at " + serverUrl);
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
	public void testPut () throws Exception
	{
		final int resourceId = 15;
		Resource resource = new Resource();
		resource.setId(resourceId);
		Resource response = client.put(new URL(serverUrl + "/update/" + resourceId), resource);
		assertNotNull(response.getId());
		assertTrue(response.getId().equals(resourceId));
	}

	@After
	public void tearDown () throws Exception
	{
		server.stop();
	}

}
