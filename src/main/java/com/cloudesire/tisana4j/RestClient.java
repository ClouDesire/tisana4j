package com.cloudesire.tisana4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class RestClient implements RestClientInterface
{
	private final String username;
	private final String password;
	private SSLContext ctx;
	private final boolean authenticated;
	private boolean useXml = false;
	private ExceptionTranslator exceptionTranslator;
	private HttpResponseHandler httpResponseHandler;
	private final ObjectMapper mapper = new ObjectMapper();
	private final boolean skipValidation;
	private Map<String, String> headers;
	private HttpClient httpClient;
	private static Logger log = LoggerFactory.getLogger(RestClient.class);

	/**
	 * Default settings: no authentication and verify if server certificate is
	 * valid. Uses json. For use xml setUseXml() to true.
	 */
	public RestClient()
	{
		this(null, null, false, null);
	}

	/**
	 * @param skipValidation
	 *            if true skips server certificate validation for Https
	 *            connections
	 */
	public RestClient(boolean skipValidation)
	{
		this(null, null, skipValidation, null);
	}

	/**
	 * @param username
	 *            user for authentication
	 * @param password
	 *            password for authentication
	 * @param skipValidation
	 *            if true skips server certificate validation for Https
	 *            connections
	 */
	public RestClient(String username, String password, boolean skipValidation)
	{
		this(username, password, skipValidation, null);
	}

	/**
	 * @param username
	 *            user for authentication
	 * @param password
	 *            password for authentication
	 * @param skipValidation
	 *            if true skips server certificate validation for Https
	 *            connections
	 * @param headers
	 *            connection properties that will be added by default to any
	 *            connection
	 */
	public RestClient(String username, String password, boolean skipValidation, Map<String, String> headers)
	{
		this(username, password, skipValidation, headers, null);
	}

	/**
	 * @param username
	 *            user for authentication
	 * @param password
	 *            password for authentication
	 * @param skipValidation
	 *            if true skips server certificate validation for Https
	 *            connections
	 * @param headers
	 *            connection properties that will be added by default to any
	 *            connection
	 * @param ctx
	 *            ssl context
	 */
	public RestClient(String username, String password, boolean skipValidation, Map<String, String> headers,
			SSLContext ctx)
	{
		super();
		this.username = username;
		this.password = password;
		this.skipValidation = skipValidation;
		authenticated = username != null;
		this.headers = headers;
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#delete(java.net.URL)
	 */
	@Override
	public void delete ( URL url ) throws Exception
	{
		delete(url, null);
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#delete(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public void delete ( URL url, Map<String, String> newHeaders ) throws Exception
	{
		delete(url, newHeaders, null);
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#delete(java.net.URL,
	 * java.util.Map. java.util.Map)
	 */
	@Override
	public void delete ( URL url, Map<String, String> newHeaders, Map<String, String> responseHeaders )
			throws Exception
	{
		HttpDelete delete = new HttpDelete(url.toURI());
		setupMethod(delete, newHeaders);
		HttpResponse response = execute(delete);
		if (responseHeaders != null && response.getAllHeaders().length != 0)
		{
			for (Header header : response.getAllHeaders())
			{
				responseHeaders.put(header.getName(), header.getValue());
			}
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#get(java.net.URL,
	 * java.lang.Class)
	 */
	@Override
	public <T> T get ( URL url, Class<T> clazz ) throws Exception
	{
		return get(url, clazz, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#get(java.net.URL,
	 * java.lang.Class, java.util.Map)
	 */
	@Override
	public <T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending GET to " + url);
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		return readObject(clazz, execute(get));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#getCollection(java.net.URL,
	 * java.lang.Class)
	 */
	@Override
	public <T> List<T> getCollection ( URL url, Class<T> clazz ) throws Exception
	{
		return getCollection(url, clazz, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#getCollection(java.net.URL,
	 * java.lang.Class, java.util.Map)
	 */
	@Override
	public <T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception
	{
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		HttpResponse response = execute(get);
		try
		{
			List<T> objList = mapper.reader(mapper.getTypeFactory().constructCollectionType(List.class, clazz))
					.readValue(response.getEntity().getContent());
			return objList;
		} catch (JsonParseException e)
		{
			throw new ParseException("Parsing error: " + e.getOriginalMessage());
		}

	}

	public HttpResponseHandler getHttpResponseHandler ()
	{
		return httpResponseHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#head(java.net.URL)
	 */
	@Override
	public Map<String, String> head ( URL url ) throws Exception
	{
		return head(url, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#head(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws Exception
	{
		HttpHead head = new HttpHead(url.toURI());
		setupMethod(head, newHeaders);
		HttpResponse response = execute(head);

		Map<String, String> headers = new HashMap<>();
		Header[] allHeaders = response.getAllHeaders();
		if (allHeaders == null) return headers;
		for (int i = 0; i < allHeaders.length; i++)
			headers.put(allHeaders[i].getName(), allHeaders[i].getValue());
		return headers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#options(java.net.URL)
	 */
	@Override
	public String[] options ( URL url ) throws Exception
	{
		return options(url, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#options(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public String[] options ( URL url, Map<String, String> newHeaders ) throws Exception
	{
		HttpOptions options = new HttpOptions(url.toURI());
		setupMethod(options, newHeaders);
		HttpResponse response = execute(options);
		String allow = null;
		Header[] allHeaders = response.getAllHeaders();
		for (int i = 0; i < allHeaders.length; i++)
			if (allHeaders[i].getName() == "Allow") allow = allHeaders[i].getValue();
		if (allow == null) throw new Exception("Method options not supported.");
		return allow.split(",");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#patch(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public void patch ( URL url, Map<String, String> paramMap ) throws Exception
	{
		patch(url, paramMap, null);
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#patch(java.net.URL,
	 * java.util.Map, java.util.Map)
	 */
	@Override
	public void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws Exception
	{
		HttpPatch patch = new HttpPatch(url.toURI());
		setupMethod(patch, newHeaders);
		writeObject(paramMap, patch);
		execute(patch);
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#post(java.net.URL, T)
	 */
	@Override
	public <T> T post ( URL url, T obj ) throws Exception
	{

		return post(url, obj, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#post(java.net.URL, T,
	 * java.util.Map)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public <T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws Exception
	{
		return (T) post(url, obj, newHeaders, obj.getClass());
	}

	@Override
	public <T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws Exception
	{
		return post(url, obj, newHeaders, responseClass, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#post(java.net.URL, T,
	 * java.util.Map, java.lang.Class)
	 */
	@Override
	public <T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass,
			Map<String, String> responseHeaders ) throws Exception
	{
		HttpPost post = new HttpPost(url.toURI());
		setupMethod(post, newHeaders);
		if (obj != null) writeObject(obj, post);
		HttpResponse response = execute(post);
		if (responseHeaders != null && response.getAllHeaders().length != 0)
		{
			for (Header header : response.getAllHeaders())
			{
				responseHeaders.put(header.getName(), header.getValue());
			}
		}

		if (response.getEntity() == null) return null;
		if (responseClass == null)
		{
			EntityUtils.consumeQuietly(response.getEntity());
			return null;
		}
		return readObject(responseClass, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#postData(java.net.URL,
	 * java.lang.String, java.io.InputStream, java.lang.Class)
	 */
	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws Exception
	{
		return postData(url, filename, content, responseClass, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#postData(java.net.URL,
	 * java.lang.String, java.io.InputStream, java.lang.Class, java.util.Map)
	 */
	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws Exception
	{
		HttpPost post = new HttpPost(url.toURI());

		setupMethod(post, newHeaders);
		MultipartEntity entity = new MultipartEntity();

		InputStreamBody body = new InputStreamBody(content, filename);

		entity.addPart("file", body);
		post.setEntity(entity);
		HttpResponse response = execute(post);
		if (responseClass == null || response.getEntity() == null) return null;
		return readObject(responseClass, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#put(java.net.URL, T)
	 */
	@Override
	public <T> T put ( URL url, T obj ) throws Exception
	{
		return put(url, obj, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#put(java.net.URL, T,
	 * java.util.Map)
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public <T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws Exception
	{
		HttpPut put = new HttpPut(url.toURI());
		setupMethod(put, newHeaders);
		writeObject(obj, put);
		HttpResponse response = execute(put);
		if (response.getEntity() == null) return null;
		return (T) readObject(obj.getClass(), response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#setExceptionTranslator(com
	 * .cloudesire.tisana4j.ExceptionTranslator)
	 */
	@Override
	public void setExceptionTranslator ( ExceptionTranslator exceptionTranslator )
	{
		this.exceptionTranslator = exceptionTranslator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#setHeaders(java.util.Map)
	 */
	@Override
	public void setHeaders ( Map<String, String> headers )
	{
		this.headers = headers;
	}

	@Override
	public void setHttpResponseHandler ( HttpResponseHandler httpResponseHandler )
	{
		this.httpResponseHandler = httpResponseHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cloudesire.tisana4j.RestClientInterface#setUseXml(boolean)
	 */
	@Override
	public void setUseXml ( boolean useXml )
	{
		this.useXml = useXml;
	}

	private void applyHeaders ( HttpRequest request, Map<String, String> newHeaders )
	{
		Map<String, String> mergedHeaders = new HashMap<String, String>();
		if (headers != null) mergedHeaders.putAll(headers);
		if (newHeaders != null) mergedHeaders.putAll(newHeaders);

		for (String k : mergedHeaders.keySet())
			request.addHeader(k, mergedHeaders.get(k));
		return;
	}

	private void checkError ( HttpResponse response ) throws Exception
	{
		int responseCode = response.getStatusLine().getStatusCode();
		if (httpResponseHandler != null)
		{
			httpResponseHandler.setResponse(responseCode, response.getStatusLine().getReasonPhrase());
		}
		if (responseCode < 200 || responseCode >= 300)
		{
			if (exceptionTranslator != null)
			{
				Exception translatedException = exceptionTranslator.translateError(responseCode, response
						.getStatusLine().getReasonPhrase(), response.getEntity().getContent());
				if (translatedException == null) return;
				else throw translatedException;
			}
			else throw new RestException(responseCode, response.getStatusLine().getReasonPhrase());
		}
	}

	/**
	 * Internal execute, log headers, check for errors
	 * 
	 * @param request
	 * @return HttpResponse
	 * @throws Exception
	 */
	private HttpResponse execute ( HttpUriRequest request ) throws Exception
	{
		log.debug(">>>> " + request.getRequestLine());
		for (Header header : request.getAllHeaders())
		{
			log.trace(">>>> " + header.getName() + ": " + header.getValue());
		}

		HttpResponse response = getHttpClient().execute(request);

		log.debug("<<<< " + response.getStatusLine());
		for (Header header : response.getAllHeaders())
		{
			log.trace("<<<< " + header.getName() + ": " + header.getValue());
		}

		checkError(response);
		return response;
	}

	private synchronized HttpClient getHttpClient () throws Exception
	{
		if (httpClient == null)
		{
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
			cm.closeIdleConnections(1, TimeUnit.SECONDS);
			httpClient = new DefaultHttpClient(cm);
			HttpParams params = httpClient.getParams();
			params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
			if (skipValidation)
			{
				log.warn("Configuring HTTPS with no validation!");
				SSLSocketFactory sf = new SSLSocketFactory(getSSLContext(), new AllowAllHostnameVerifier());
				Scheme https = new Scheme("https", 443, sf);
				httpClient.getConnectionManager().getSchemeRegistry().register(https);
			}

		}

		return httpClient;
	}

	private SSLContext getSSLContext () throws NoSuchAlgorithmException, KeyManagementException
	{
		if (ctx != null) return ctx;
		log.trace("Creating SSL context with no certificate validation");
		ctx = SSLContext.getInstance("SSL");
		TrustManager tm = new X509TrustManager()
		{

			@Override
			public void checkClientTrusted ( X509Certificate[] arg0, String arg1 ) throws CertificateException
			{

			}

			@Override
			public void checkServerTrusted ( X509Certificate[] arg0, String arg1 ) throws CertificateException
			{

			}

			@Override
			public X509Certificate[] getAcceptedIssuers ()
			{
				return null;
			}
		};
		ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
		return ctx;
	}

	@SuppressWarnings ( "unchecked" )
	private <T> T readObject ( Class<T> clazz, HttpResponse response ) throws IOException, JsonProcessingException,
			ParseException, JAXBException
	{
		if (!useXml)
		{
			try
			{
				T obj = mapper.reader(clazz).readValue(response.getEntity().getContent());
				return obj;
			} catch (JsonParseException e)
			{
				throw new ParseException("Parsing error: " + e.getOriginalMessage());
			}
		}
		else
		{
			JAXBContext contextB = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshallerB = contextB.createUnmarshaller();
			T obj = (T) unmarshallerB.unmarshal(response.getEntity().getContent());
			return obj;
		}

	}

	private void setupMethod ( HttpRequest request, Map<String, String> newHeaders )
	{
		applyHeaders(request, newHeaders);
		if (authenticated)
		{
			String authorization = "Basic";
			String encoded = Base64Variants.MIME_NO_LINEFEEDS.encode((username + ":" + password).getBytes());
			authorization = "Basic " + encoded;
			request.addHeader("Authorization", authorization);
		}
	}

	private <T> void writeObject ( T obj, HttpEntityEnclosingRequest request ) throws IOException,
			JsonGenerationException, MappingException, JsonProcessingException, ParseException, JAXBException
	{
		if (!useXml)
		{
			try
			{
				request.addHeader("Content-type", "application/json");
				ObjectWriter writer = mapper.writer();
				String payload = writer.writeValueAsString(obj);
				StringEntity entity = new StringEntity(payload);
				log.debug("Payload:\n " + payload);
				request.setEntity(entity);
			} catch (JsonMappingException e)
			{
				throw new MappingException("Error while mapping Object to Json");
			}
		}
		else
		{
			request.addHeader("Content-type", "application/xml");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller m = context.createMarshaller();
			m.marshal(obj, baos);
			String payload = baos.toString();
			StringEntity entity = new StringEntity(payload);
			log.debug("Payload:\n " + payload);
			request.setEntity(entity);
		}
	}
}
