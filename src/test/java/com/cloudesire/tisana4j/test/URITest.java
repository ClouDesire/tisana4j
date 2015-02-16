package com.cloudesire.tisana4j.test;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * Created by gionn on 16/02/15.
 */
public class URITest
{
	@Test
	public void JavaURIplusSignReplacementTest () throws Exception
	{
		String scheme = "https";
		String hostname = "localhost";
		String path = "/resources";
		String query = "key=vàlue+";

		String uri = new URI(scheme, hostname, path, query, null).toASCIIString();

		uri = uri.replace("+", "%2B");

		Assert.assertFalse(uri.toString(), uri.contains("+"));
		Assert.assertTrue(uri.toString(), uri.contains("%2B"));

		Assert.assertEquals("https://localhost/resources?key=v%C3%A0lue%2B", uri);
	}
}
