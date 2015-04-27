package com.cloudesire.tisana4j;


import com.cloudesire.tisana4j.ExceptionTranslator.ResponseMessage;
import com.cloudesire.tisana4j.exceptions.*;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestClient implements RestClientInterface
{
	private final String username;
	private final String password;
	private SSLContext ctx;
	private boolean authenticated;
	private boolean useXml = false;
	private ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();
	private final ObjectMapper mapper = new ObjectMapper();
	private final boolean skipValidation;
	private Map<String, String> headers;
	private Map<String, List<String>> responseHeaders;
	private HttpClient httpClient;
	private final static Logger log = LoggerFactory.getLogger(RestClient.class);

	@Override
	public void toggleAuthentication()
	{
		authenticated = !authenticated;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override public ObjectMapper getObjectMapper ()
	{
		return mapper;
	}

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
	public RestClient(String username, String password, boolean skipValidation, Map<String, String> headers, SSLContext ctx)
	{
		super();
		this.username = username;
		this.password = password;
		this.skipValidation = skipValidation;
		authenticated = username != null;
		this.headers = headers;
		this.ctx = ctx;
	}

	@Override
	public void delete ( URL url ) throws RestException, RuntimeRestException
	{
		delete(url, null);
	}

	private Map<String,List<String>> parseResponseHeaders ( HttpResponse response )
	{
		responseHeaders = new HashMap<>();
		if ( response.getAllHeaders().length != 0)
		{
			for (Header header : response.getAllHeaders())
			{
				String name = header.getName();
				String value = header.getValue();
				if ( responseHeaders.containsKey(name) )
					responseHeaders.get(name).add( value );
				else
				{
					List<String> x = new LinkedList<>();
					x.add(value);
					responseHeaders.put(name, x);
				}
			}
		}
		return responseHeaders;
	}

	@Override
	public void delete ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		HttpDelete delete;
		try
		{
			delete = new HttpDelete(url.toURI());
		} catch (URISyntaxException e)
		{
			throw new RuntimeRestException(e);
		}
		setupMethod(delete, newHeaders);
		HttpResponse response = execute(delete);
		parseResponseHeaders( response );
	}

	@Override
	public <T> T get ( URL url, Class<T> clazz ) throws RestException, RuntimeRestException
	{
		return get(url, clazz, null);
	}

	@Override
	public <T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RuntimeRestException,
			RestException
	{
		log.debug("Sending GET to " + url);
		try
		{
			HttpResponse response = getInternal(url, newHeaders);
			return readObject(clazz, response );
		} catch ( ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	private HttpResponse getInternal ( URL url, Map<String, String> headers ) throws RuntimeRestException, RestException
	{
		log.debug("Sending GET to " + url);
		try
		{
			HttpGet get = new HttpGet(url.toURI());
			setupMethod(get, headers);
			HttpResponse response = execute(get);
			return response;
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public InputStream get ( URL url ) throws RuntimeRestException, RestException, IOException
	{
		return getInternal(url, new HashMap<String, String>()).getEntity().getContent();
	}

	@Override
	public InputStream get ( URL url, Map<String, String> headers ) throws RuntimeRestException, RestException, IOException
	{
		return getInternal(url, headers).getEntity().getContent();
	}

	@Override
	public <T> List<T> getCollection ( URL url, Class<T> clazz ) throws RestException, RuntimeRestException
	{
		return getCollection(url, clazz, null);

	}

	@Override
	public <T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		try
		{
			HttpGet get = new HttpGet(url.toURI());
			setupMethod(get, newHeaders);
			HttpResponse response = execute(get);
			parseResponseHeaders( response );
			try (InputStream stream = response.getEntity().getContent())
			{
				List<T> objList = mapper.reader(mapper.getTypeFactory().constructCollectionType(List.class, clazz)).readValue(stream);
				return objList;
			} 
		}
		catch ( IOException | URISyntaxException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public Map<String, String> head ( URL url ) throws RuntimeRestException, RestException
	{
		return head(url, null);
	}

	@Override
	public Map<String, String> head ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		try
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
		catch (URISyntaxException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public String[] options ( URL url ) throws RuntimeRestException, RestException
	{
		return options(url, null);
	}

	@Override
	public String[] options ( URL url, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		HttpOptions options;
		try
		{
			options = new HttpOptions(url.toURI());
		} catch (URISyntaxException e)
		{
			throw new RuntimeRestException(e);
		}
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

	@Override
	public void patch ( URL url, Map<String, String> paramMap ) throws  RestException, RuntimeRestException
	{
		patch(url, paramMap, null);
	}

	@Override public void patchO ( URL url, Map<String, Object> paramMap ) throws RestException, RuntimeRestException
	{
		patchO(url, paramMap, null);
	}

	@Override
	public <T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz ) throws RestException, RuntimeRestException
	{
		return patchEntity(url, paramMap, clazz, null);
	}

	@Override public <T> T patchEntityO ( URL url, Map<String, Object> paramMap, Class<T> clazz )
			throws RestException, RuntimeRestException
	{
		return patchEntityO(url, paramMap, clazz, null);
	}

	@Override
	public <T> T patchEntity ( URL url, Map<String, String> paramMap, Class<T> clazz, Map<String, String> newHeaders )
			throws RestException, RuntimeRestException
	{
		return patchEntityO(url, paramMap != null ? new HashMap<String, Object>(paramMap) : null, clazz, newHeaders);
	}

	@Override public <T> T patchEntityO ( URL url, Map<String, Object> paramMap, Class<T> clazz,
			Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		try
		{
			HttpPatch patch = new HttpPatch(url.toURI());

			setupMethod(patch, newHeaders);
			writeObject(paramMap, patch);
			return readObject(clazz, execute(patch));
		} catch (URISyntaxException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws RestException,
			RuntimeRestException
	{
		patchO(url, paramMap != null ? new HashMap<String, Object>(paramMap) : null, newHeaders);
	}

	@Override public void patchO ( URL url, Map<String, Object> paramMap, Map<String, String> newHeaders )
			throws RestException, RuntimeRestException
	{
		try
		{
			HttpPatch patch = new HttpPatch(url.toURI());

			setupMethod(patch, newHeaders);
			writeObject(paramMap, patch);
			HttpResponse response = execute(patch);
			parseResponseHeaders( response );
			EntityUtils.consumeQuietly(response.getEntity());
		} catch (URISyntaxException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public <T> T post ( URL url, T obj ) throws RestException, RuntimeRestException
	{
		return post(url, obj, null);
	}

	@Override
	@SuppressWarnings ( "unchecked" )
	public <T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		return (T) post(url, obj, newHeaders, obj.getClass());
	}	
	
	@Override
	public <T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass )
			throws RestException, RuntimeRestException
	{
		try
		{
			HttpPost post = new HttpPost(url.toURI());

			setupMethod(post, newHeaders);
			if (obj != null) writeObject(obj, post);
			HttpResponse response = execute(post);

			if (response.getEntity() == null)
			{
				parseResponseHeaders( response );
				return null;
			}
			if (responseClass == null)
			{
				parseResponseHeaders( response );
				EntityUtils.consumeQuietly(response.getEntity());
				return null;
			}
			return readObject(responseClass, response);
		}
		catch (URISyntaxException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}
	
	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws RestException, RuntimeRestException
	{
		return postData(url, filename, content, responseClass, null);
	}

	@Override
	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{

		try
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
				parseResponseHeaders( response );
				EntityUtils.consumeQuietly(response.getEntity());
				return null;
			}
			return readObject(responseClass, response);
		} catch (URISyntaxException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public <T> T postFormData ( URL url, List<BasicNameValuePair> formData, Class<T> responseClass )
			throws RestException, RuntimeRestException
	{
		try
		{
			HttpPost post = new HttpPost(url.toURI());
			setupMethod(post, null);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData, "UTF-8");
			post.setEntity(entity);
			HttpResponse response = execute(post);
			if (responseClass == null || response.getEntity() == null)
			{
				parseResponseHeaders( response );
				EntityUtils.consumeQuietly(response.getEntity());
				return null;
			}
			return readObject(responseClass, response);
		} catch (URISyntaxException | UnsupportedEncodingException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public <T> T put ( URL url, T obj ) throws RestException, RuntimeRestException
	{
		return put(url, obj, null);
	}

	@SuppressWarnings ( "unchecked" )
	@Override
	public <T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws RestException, RuntimeRestException
	{
		return (T) put (url, obj, newHeaders,obj.getClass());
	}

	@SuppressWarnings ( "unchecked" )
	@Override
	public <T, R> R put ( URL url, T obj,Map<String, String> newHeaders, Class<R> responseClass)
			throws RestException, RuntimeRestException
	{
		try
		{
			HttpPut put = new HttpPut(url.toURI());
			setupMethod(put, newHeaders);
			if( obj!=null ) writeObject(obj, put);
			HttpResponse response = execute(put);
			if (response.getEntity() == null)
			{
				parseResponseHeaders( response );
				return null;
			}
			return (R) readObject(responseClass, response);
		}
		catch (URISyntaxException | ParseException e)
		{
			throw new RuntimeRestException(e);
		}
	}
	
	@Override
	public void setExceptionTranslator ( ExceptionTranslator exceptionTranslator )
	{
		this.exceptionTranslator = exceptionTranslator;
	}

	@Override
	public void setHeaders ( Map<String, String> headers )
	{
		this.headers = headers;
	}

	@Override
	public Map<String, String> getHeaders()
	{
		return this.headers;
	}

	@Override
	public void setUseXml ( boolean useXml )
	{
		this.useXml = useXml;
	}

	private void applyHeaders ( HttpRequest request, Map<String, String> newHeaders )
	{
		Map<String, String> mergedHeaders = new HashMap<>();
		if (headers != null) mergedHeaders.putAll(headers);
		if (newHeaders != null) mergedHeaders.putAll(newHeaders);

		for (String k : mergedHeaders.keySet())
			request.addHeader(k, mergedHeaders.get(k));
	}

	private void checkError ( HttpResponse response ) throws RestException
	{
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode < 200 || responseCode >= 300)
		{
			try (InputStream stream = response.getEntity().getContent())
			{
				ContentType type = ContentType.getOrDefault(response.getEntity());
				String charset = type.getCharset() != null ? type.getCharset().name() : "UTF-8";

				String errorStream = IOUtils.toString(stream, charset);

				ResponseMessage responseMessage = new ResponseMessage();
				RestException translatedException = exceptionTranslator.translateException(responseCode, response
					.getStatusLine().getReasonPhrase(), errorStream, responseMessage, response.getAllHeaders());

				if (translatedException != null) throw translatedException;

				throw getDefaultException(responseCode, response.getStatusLine().getReasonPhrase(), responseMessage.getResponse()!=null ? responseMessage.getResponse() : errorStream );

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
		}
		catch (KeyManagementException |  NoSuchAlgorithmException | IOException e)
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
			if (this.skipValidation || this.ctx != null)
			{
				SSLSocketFactory sf;
				if (this.skipValidation)
				{
					log.warn("Configuring HTTPS with no validation!");
					sf = new SSLSocketFactory(getSSLContext(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				} else sf =   new SSLSocketFactory(getSSLContext());

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
		parseResponseHeaders( response );
		if (response.getEntity() == null)
			return null;
		Header contentType = response.getEntity().getContentType();
		if (contentType != null)
		{
			if (contentType.getValue().contains(ContentType.APPLICATION_JSON.getMimeType()))
				return parseJson(clazz, response);
			if (contentType.getValue().toLowerCase().contains("xml"))
				return parseXml(clazz, response);
		}
		throw new ParseException("Unsupported content type " + contentType.getValue());
	}

	private <T> T parseJson ( Class<T> clazz, HttpResponse response ) throws ParseException, RuntimeRestException
	{
		try (InputStream stream = response.getEntity().getContent())
		{
			T obj = mapper.reader(clazz).readValue(stream);
			return obj;
		}
		catch (JsonProcessingException e)
		{
			throw new ParseException(e);
		}
		catch (IllegalStateException | IOException e1)
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
			}
			catch (JAXBException | UnsupportedEncodingException e)
			{
				throw new ParseException(e);
			}
		}
	}

	@Override
	public InputStream getData ( URL url, Map<String, String> newHeaders ) throws RuntimeRestException, RestException
	{
		try
		{
			log.debug("Sending GET to " + url + " to retrieve CSV file");
			HttpGet get;
			get = new HttpGet(url.toURI());
			setupMethod(get, newHeaders);
			HttpResponse response = execute(get);
			return response.getEntity().getContent();
		}
		catch (URISyntaxException | IllegalStateException | IOException e)
		{
			throw new RuntimeRestException(e);
		}
	}

	@Override
	public Map<String, List<String>> getLastResponseHeaders()
	{
		return responseHeaders;
	}

}
