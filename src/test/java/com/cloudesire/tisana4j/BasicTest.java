package com.cloudesire.tisana4j;

import java.net.URL;

public class BasicTest {
	private static class NetworkAddress {
		private String ip;

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
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
