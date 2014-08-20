tisana4j
========

Yet another (but missing) simple RESTful client library for Java, that support both JSON and XML REST API.
We are using this library for the integration tests of our backend modules, and for a bunch of cloud providers open-source libraries, like [joyent-api-client](https://github.com/ClouDesire/joyent-api-client), [azure-api-client](https://github.com/ClouDesire/azure-api-client) and [gce-api-client](https://github.com/ClouDesire/gce-api-client).

Usage
=====

```
<dependency>
    <groupId>com.cloudesire</groupId>
    <artifactId>tisana4j</artifactId>
    <version>0.0.14</version>
</dependency>
```

Example
=======

```java
public class Test {
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
		
		NetworkAddress testClass = client.get(new URL("http://ip.jsontest.com/"),		NetworkAddress.class);
		
		System.out.println("YOUR IP:" + testClass.getIp());


	}

}
```
