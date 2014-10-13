package com.cloudesire.tisana4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.entity.ContentType;
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

import com.cloudesire.tisana4j.ExceptionTranslator.ResponseMessage;
import com.cloudesire.tisana4j.exceptions.AccessDeniedException;
import com.cloudesire.tisana4j.exceptions.BadRequestException;
import com.cloudesire.tisana4j.exceptions.ConflictException;
import com.cloudesire.tisana4j.exceptions.DefaultExceptionTranslator;
import com.cloudesire.tisana4j.exceptions.InternalServerErrorException;
import com.cloudesire.tisana4j.exceptions.ParseException;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.cloudesire.tisana4j.exceptions.RuntimeRestException;
import com.cloudesire.tisana4j.exceptions.UnauthorizedException;
import com.cloudesire.tisana4j.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class RestClient implements RestClientInterface
{
	private final String username;
	private final String password;
	private SSLContext ctx;
	private final boolean authenticated;
	private boolean useXml = false;
	private ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();
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
	public void delete ( URL url ) throws URISyntaxException, RestException, RuntimeRestException
	{
		delete(url, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#delete(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public void delete ( URL url, Map<String, String> newHeaders ) throws URISyntaxException, RestException, RuntimeRestException
	{
		delete(url, newHeaders, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#delete(java.net.URL,
	 * java.util.Map. java.util.Map)
	 */
	@Override
	public void delete ( URL url, Map<String, String> newHeaders, Map<String, String> responseHeaders ) throws URISyntaxException, RestException, RuntimeRestException

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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#get(java.net.URL,
	 * java.lang.Class)
	 */
	@Override
	public <T> T get ( URL url, Class<T> clazz ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
	public <T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws URISyntaxException, ParseException, RuntimeRestException, RestException
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
	public <T> List<T> getCollection ( URL url, Class<T> clazz ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
	public <T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		HttpResponse response = execute(get);
		try (InputStream stream = response.getEntity().getContent())
		{
			try
			{
				List<T> objList = mapper.reader(mapper.getTypeFactory().constructCollectionType(List.class, clazz))
						.readValue(stream);
				return objList;
			} catch (JsonProcessingException  e)
			{
				throw new ParseException(e);
			}
		} catch ( IOException e)
		{
			throw new RuntimeRestException(e);
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
	public Map<String, String> head ( URL url ) throws ParseException, RuntimeRestException, RestException, URISyntaxException
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
	public Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpHead head = new HttpHead(url.toURI());
		setupMethod(head, newHeaders);
		HttpResponse response = execute(head);
		EntityUtils.consumeQuietly(response.getEntity());

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
	public String[] options ( URL url ) throws ParseException, RuntimeRestException, RestException, URISyntaxException
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
	public String[] options ( URL url, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpOptions options = new HttpOptions(url.toURI());
		setupMethod(options, newHeaders);
		HttpResponse response = execute(options);
		EntityUtils.consumeQuietly(response.getEntity());
		String allow = null;
		Header[] allHeaders = response.getAllHeaders();
		for (int i = 0; i < allHeaders.length; i++)
			if (allHeaders[i].getName() == "Allow") allow = allHeaders[i].getValue();
		if (allow == null) throw new BadRequestException(404,"Method options not supported.");
		return allow.split(",");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#patch(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public void patch ( URL url, Map<String, String> paramMap ) throws  RestException, RuntimeRestException, URISyntaxException, ParseException
	{
		patch(url, paramMap, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#patchEntity(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public <T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		return patchEntity(url, paramMap, clazz, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.cloudesire.tisana4j.RestClientInterface#patchEntity(java.net.URL,
	 * java.util.Map)
	 */
	@Override
	public <T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders )
			throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpPatch patch = new HttpPatch(url.toURI());
		setupMethod(patch, newHeaders);
		writeObject(paramMap, patch);
		return readObject(clazz, execute(patch));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#patch(java.net.URL,
	 * java.util.Map, java.util.Map)
	 */
	@Override
	public void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpPatch patch = new HttpPatch(url.toURI());
		setupMethod(patch, newHeaders);
		writeObject(paramMap, patch);
		HttpResponse response = execute(patch);
		EntityUtils.consumeQuietly(response.getEntity());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#post(java.net.URL, T)
	 */
	@Override
	public <T> T post ( URL url, T obj ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
	public <T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		return (T) post(url, obj, newHeaders, obj.getClass());
	}

	@Override
	public <T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
			Map<String, String> responseHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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

	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		return postData(url, filename, content, responseClass, null);
	}

	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
	{
		HttpPost post = new HttpPost(url.toURI());

		setupMethod(post, newHeaders);
		MultipartEntity entity = new MultipartEntity();

		InputStreamBody body = new InputStreamBody(content, filename);

		entity.addPart("file", body);
		post.setEntity(entity);
		HttpResponse response = execute(post);
		if (responseClass == null || response.getEntity() == null)
		{
			EntityUtils.consumeQuietly(response.getEntity());
			return null;
		}
		return readObject(responseClass, response);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.cloudesire.tisana4j.RestClientInterface#put(java.net.URL, T)
	 */
	@Override
	public <T> T put ( URL url, T obj ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
	public <T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws ParseException, RestException, RuntimeRestException, URISyntaxException
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
	}

	private void checkError ( HttpResponse response ) throws RestException
	{
		int responseCode = response.getStatusLine().getStatusCode();
		if (httpResponseHandler != null)
		{
			httpResponseHandler.setResponse(responseCode, response.getStatusLine().getReasonPhrase());
		}
		if (responseCode < 200 || responseCode >= 300)
		{
			try (InputStream stream = response.getEntity().getContent())
			{
				ContentType type = ContentType.getOrDefault(response.getEntity());
				String charset = type.getCharset() != null ? type.getCharset().name() : "UTF-8";

				String errorStream = IOUtils.toString(stream, charset);

				ResponseMessage responseMessage = new ResponseMessage();
				RestException translatedException = exceptionTranslator.translateException(responseCode, response
					.getStatusLine().getReasonPhrase(), errorStream, responseMessage);

				if (translatedException != null) throw translatedException;

				throw getDefaultException(responseCode, response.getStatusLine().getReasonPhrase(), responseMessage.getResponse() );

			} catch (IllegalStateException | IOException e)
			{
				throw new RestException(responseCode, e.getMessage());
			}

		}
	}

	private RestException getDefaultException ( int responseCode, String reasonPhrase, String responseMessage )
	{
		String msgError = responseMessage != null ? responseMessage : reasonPhrase;

		switch (responseCode)
		{
			case 400:
				return new BadRequestException(responseCode, msgError);
			case 401:
				return new UnauthorizedException(responseCode, msgError);
			case 403:
				return new AccessDeniedException(responseCode, msgError);
			case 404:
				return new ResourceNotFoundException(responseCode, msgError);
			case 409:
				return new ConflictException(responseCode, msgError);
			case 422:
				return new UnprocessableEntityException(responseCode, msgError);
			case 500:
				return new InternalServerErrorException(responseCode, msgError);
		}
		return new RestException(responseCode, msgError);
	}

	/**
	 * Internal execute, log headers, check for errors
	 *
	 * @param request
	 * @return HttpResponse
	 * @throws RestException
	 * @throws RuntimeRestException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws ClientProtocolException
	 * @throws KeyManagementException
	 * @throws Exception
	 */
	private HttpResponse execute ( HttpUriRequest request ) throws RestException, RuntimeRestException
	{
		log.debug(">>>> " + request.getRequestLine());
		for (Header header : request.getAllHeaders())
		{
			log.trace(">>>> " + header.getName() + ": " + header.getValue());
		}

		HttpResponse response;
		try
		{
			response = getHttpClient().execute(request);

		log.debug("<<<< " + response.getStatusLine());
		for (Header header : response.getAllHeaders())
		{
			log.trace("<<<< " + header.getName() + ": " + header.getValue());
		}

		if (response.getStatusLine().getStatusCode() == 204)
		{
			log.debug("Consuming quietly the response entity since server returned no content");
			EntityUtils.consumeQuietly(response.getEntity());
		}

		checkError(response);
		return response;
		} catch (KeyManagementException |  NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	private synchronized HttpClient getHttpClient () throws KeyManagementException, NoSuchAlgorithmException
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

	private <T> T readObject ( Class<T> clazz, HttpResponse response ) throws ParseException, RuntimeRestException
	{
		if (useXml)
		{
			return parseXml(clazz, response);
		}
		return parseJson(clazz, response);
	}

	private <T> T parseJson ( Class<T> clazz, HttpResponse response ) throws ParseException, RuntimeRestException
	{
		try (InputStream stream = response.getEntity().getContent())
		{
			try
			{
				T obj = mapper.reader(clazz).readValue(stream);
				return obj;
			} catch (JsonProcessingException e)
			{
				throw new ParseException(e);
			}
		} catch (IllegalStateException | IOException e1)
		{
			throw new RuntimeRestException(e1);
		}
	}

	@SuppressWarnings ( "unchecked" )
	private <T> T parseXml ( Class<T> clazz, HttpResponse response ) throws ParseException, RuntimeRestException
	{
		try (InputStream stream = response.getEntity().getContent())
		{
			JAXBContext contextB = getJaxbContext(clazz);
			Unmarshaller unmarshallerB = contextB.createUnmarshaller();
			try
			{
				T obj = (T) unmarshallerB.unmarshal(stream);
				return obj;
			} catch (JAXBException e)
			{
				throw new ParseException(e);
			}
		} catch (IllegalStateException | IOException | JAXBException e1)
		{
			throw new RuntimeRestException(e1);
		}
	}

	// wrap newInstance to avoid http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7122142
	private static JAXBContext getJaxbContext(Class<?> clazz) throws JAXBException
	{
		synchronized (jaxbGuard)
		{
			return JAXBContext.newInstance(clazz);
		}
	}
	private final static Object jaxbGuard = new Object();

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

	private <T> void writeObject ( T obj, HttpEntityEnclosingRequest request ) throws ParseException
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
			} catch ( JsonProcessingException | UnsupportedEncodingException e)
			{
				throw new ParseException(e);
			}
		}
		else
		{
			request.addHeader("Content-type", "application/xml");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBContext context;
			try
			{
				context = JAXBContext.newInstance(obj.getClass());
			Marshaller m = context.createMarshaller();
			m.marshal(obj, baos);
			String payload = baos.toString();
			StringEntity entity = new StringEntity(payload);
			log.debug("Payload:\n " + payload);
			request.setEntity(entity);
			} catch (JAXBException | UnsupportedEncodingException e)
			{
				throw new ParseException(e);
			}
		}
	}

	@Override
	public InputStream getData ( URL url, Map<String, String> newHeaders ) throws URISyntaxException, RuntimeRestException, RestException, IllegalStateException, IOException
	{
		log.debug("Sending GET to " + url + " to retrieve CSV file");
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		HttpResponse response = execute(get);
		return response.getEntity().getContent();
	}
}
