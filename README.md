tisana4j
========

[![Join the chat at https://gitter.im/ClouDesire/tisana4j](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ClouDesire/tisana4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ClouDesire/tisana4j.svg?branch=master)](https://travis-ci.org/ClouDesire/tisana4j)

Yet another (but missing) simple RESTful client library for Java, that support both JSON and XML REST API.
We are using this library for the integration tests of our backend modules, and for a bunch of cloud providers open-source libraries, like [joyent-api-client](https://github.com/ClouDesire/joyent-api-client) and [azure-api-client](https://github.com/ClouDesire/azure-api-client).

Features
========

* Internally use [Apache HttpClient](http://hc.apache.org/httpcomponents-client-4.3.x/index.html)
* Support for GET, POST, PUT, PATCH, DELETE methods
* Standard java.net.URL to supply request URL
* De/Serilization of Request/Response bodies via supplied .class (via Jackson and JAXB)
* Errors always mapped to a [RestException hierarchy](https://github.com/ClouDesire/tisana4j/tree/master/src/main/java/com/cloudesire/tisana4j/exceptions)
* Ability to define a custom [ExceptionTranslator](https://github.com/ClouDesire/tisana4j/blob/master/src/main/java/com/cloudesire/tisana4j/ExceptionTranslator.java) to map your Exceptions or custom error handling

Usage
=====

```
<dependency>
    <groupId>com.cloudesire</groupId>
    <artifactId>tisana4j</artifactId>
    <version>0.0.23</version>
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
		// default Client: no credentials
		RestClient client = new RestClient();
		NetworkAddress testClass = client.get(new URL("http://ip.jsontest.com/"), NetworkAddress.class);

		System.out.println("YOUR IP:" + testClass.getIp());
	}
}
```
