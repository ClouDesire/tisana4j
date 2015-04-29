package com.cloudesire.tisana4j.test;

import java.net.URL;

import com.cloudesire.tisana4j.RestClient;

public class ManualTest
{
	private static class NetworkAddress {
		private String ip;

		public String getIp() {
			return ip;
		}

	}

	public static void main(String[] args) throws Exception {
		// default Client: no credentials and server certificate authentication
		// required
		RestClient client = new RestClient();

		NetworkAddress testClass = client.get(
				new URL("http://ip.jsontest.com/"),
				NetworkAddress.class);

		System.out.println("YOUR IP:" + testClass.getIp());

	}

}
