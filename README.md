tisana4j
========

[![Join the chat at https://gitter.im/ClouDesire/tisana4j](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ClouDesire/tisana4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 
[![Build Status](https://travis-ci.org/ClouDesire/tisana4j.svg?branch=master)](https://travis-ci.org/ClouDesire/tisana4j) 
[![Requirements Status](https://requires.io/github/ClouDesire/tisana4j/requirements.svg?branch=master)](https://requires.io/github/ClouDesire/tisana4j/requirements/?branch=master) 
[![maven latest release](https://img.shields.io/maven-central/v/com.cloudesire/tisana4j.svg)](http://mvnrepository.com/artifact/com.cloudesire/tisana4j)


Yet another (but missing for us) simple RESTful client Java library, that support both JSON and XML REST API, based on the great [Apache HttpClient](https://hc.apache.org/httpcomponents-client-ga/index.html).

We begun writing this library when coding the first integration tests for [ClouDesire](https://cloudesire.com) backend.

Nowadays there are a lot of alternatives, like [OkHttp](http://square.github.io/okhttp/), [Unirest](http://unirest.io/java.html), but we are still in love with this.

Features
========

* Internally use [Apache HttpClient](http://hc.apache.org/httpcomponents-client-4.3.x/index.html)
* Support for GET, POST, PUT, PATCH, DELETE methods
* Standard java.net.URL to supply request URL and GET parameters
* De/Serilization of Request/Response bodies via supplied .class (via Jackson and JAXB)
* Errors always mapped to a [RestException hierarchy](https://github.com/ClouDesire/tisana4j/tree/master/src/main/java/com/cloudesire/tisana4j/exceptions)
* Ability to define a custom [ExceptionTranslator](https://github.com/ClouDesire/tisana4j/blob/master/src/main/java/com/cloudesire/tisana4j/ExceptionTranslator.java) to map your Exceptions or custom error handling

Usage
=====

```
<dependency>
    <groupId>com.cloudesire</groupId>
    <artifactId>tisana4j</artifactId>
    <version>0.0.24</version>
</dependency>
```

Example
=======

More examples in the [integration tests package](https://github.com/ClouDesire/tisana4j/tree/master/src/test/java/com/cloudesire/tisana4j/test/integration).

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
