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
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
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

public class RestClient
{
	private final String username;
	private final String password;
	private SSLContext ctx;
	private final boolean authenticated;
	private boolean useXml = false;
	private ExceptionTranslator exceptionTranslator;
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
	 * 
	 * @param skipValidation
	 *            if true skips server certificate validation for Https
	 *            connections
	 */
	public RestClient(boolean skipValidation)
	{
		this(null, null, skipValidation, null);
	}

	/**
	 * 
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
	 * 
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
		super();
		this.username = username;
		this.password = password;
		this.skipValidation = skipValidation;
		authenticated = username != null;
		this.headers = headers;
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
		if (responseCode < 200 || responseCode >= 300)

			if (exceptionTranslator != null)

				throw exceptionTranslator.translateError(responseCode, response.getStatusLine().getReasonPhrase(), response
						.getEntity().getContent());
			else throw new RestException(responseCode, response.getStatusLine().getReasonPhrase());

	}

	public void delete ( URL url ) throws Exception
	{
		delete(url, null);
		return;
	}

	public void delete ( URL url, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending DELETE to " + url);
		HttpDelete delete = new HttpDelete(url.toURI());

		setupMethod(delete, newHeaders);
		HttpResponse response = getHttpClient().execute(delete);
		checkError(response);
		return;
	}

	public <T> T get(URL url, Class<T> clazz) throws Exception {
		return get(url, clazz, null);
	}

	public <T> T get ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending GET to " + url);
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		HttpResponse response = getHttpClient().execute(get);
		checkError(response);
		return readObject(clazz, response);
	}

	public <T> List<T> getCollection ( URL url, Class<T> clazz ) throws Exception
	{
		return getCollection(url, clazz, null);

	}

	public <T> List<T> getCollection ( URL url, Class<T> clazz, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending GET to " + url);
		HttpGet get = new HttpGet(url.toURI());
		setupMethod(get, newHeaders);
		HttpResponse response = getHttpClient().execute(get);
		checkError(response);
		try
		{
			List<T> objList = mapper.reader(mapper.getTypeFactory().constructCollectionType(List.class, clazz))
					.readValue(response.getEntity().getContent());
			return objList;
		} catch (JsonParseException e)
		{
			throw new ParseException("Bad object Class");
		}

	}

	private synchronized HttpClient getHttpClient () throws Exception
	{
		if (httpClient == null)
		{
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
			httpClient = new DefaultHttpClient(cm);
			if (skipValidation)
			{
				log.debug("Configuring HTTPS with no validation");
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

	public Map<String, String> head(URL url) throws Exception {
		return head(url, null);
	}

	public Map<String, String> head(URL url, Map<String, String> newHeaders)
			throws Exception {
		log.debug("Sending HEAD to " + url);
		HttpHead head= new HttpHead(url.toURI());
		setupMethod(head, newHeaders);
		HttpResponse response = getHttpClient().execute(head);
		checkError(response);
		Map<String, String> headers = new HashMap<>();
		Header[] allHeaders = response.getAllHeaders();
		if (allHeaders == null)
			return headers;
		for (int i = 0; i < allHeaders.length; i++)
			headers.put(allHeaders[i].getName(), allHeaders[i].getValue());
		return headers;
	}

	public String[] options(URL url) throws Exception {
		return options(url, null);
	}
	public String[] options(URL url,Map<String, String> newHeaders)
			throws Exception {
		log.debug("Sending OPTIONS to " + url);
		HttpOptions options= new HttpOptions(url.toURI());
		setupMethod(options, newHeaders);
		HttpResponse response = getHttpClient().execute(options);
		checkError(response);
		String allow = null;
		Header[] allHeaders = response.getAllHeaders();
		for (int i = 0; i < allHeaders.length; i++)
			if(allHeaders[i].getName()=="Allow")
				allow=allHeaders[i].getValue();
		if (allow == null)
			throw new Exception("Method options not supported.");
		return allow.split(",");
	}

	public void patch ( URL url, Map<String, String> paramMap ) throws Exception
	{
		patch(url, paramMap, null);
		return;

	}

	public void patch ( URL url, Map<String, String> paramMap, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending PATCH to " + url);
		HttpPatch patch = new HttpPatch(url.toURI());
		setupMethod(patch, newHeaders);
		writeObject(paramMap, patch);
		HttpResponse response = getHttpClient().execute(patch);
		checkError(response);
		return;

	}

	public <T> T post ( URL url, T obj ) throws Exception
	{

		return post(url, obj, null);
	}

	@SuppressWarnings ( "unchecked" )
	public <T> T post ( URL url, T obj, Map<String, String> newHeaders ) throws Exception
	{
		return (T) post(url, obj, newHeaders, obj.getClass());
	}

	public <T, R> R post ( URL url, T obj, Map<String, String> newHeaders, Class<R> responseClass ) throws Exception
	{
		log.debug("Sending POST to " + url);
		HttpPost post = new HttpPost(url.toURI());
		setupMethod(post, newHeaders);
		if (obj != null) writeObject(obj, post);
		HttpResponse response = getHttpClient().execute(post);
		checkError(response);
		if (responseClass == null)
		{
			EntityUtils.consumeQuietly(response.getEntity());
			return null;
		}
		return readObject(responseClass, response);
	}

	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass ) throws Exception
	{
		return postData(url, filename, content, responseClass, null);
	}

	public <T> T postData ( URL url, String filename, InputStream content, Class<T> responseClass,
			Map<String, String> newHeaders ) throws Exception
			{
		log.debug("Sending binary data with POST to " + url);

		HttpPost post = new HttpPost(url.toURI());

		setupMethod(post, newHeaders);
		MultipartEntity entity = new MultipartEntity();

		InputStreamBody body = new InputStreamBody(content, filename);

		entity.addPart("file", body);
		post.setEntity(entity);
		HttpResponse response = getHttpClient().execute(post);
		checkError(response);
		if (responseClass == null) return null;
		return readObject(responseClass, response);
			}

	public <T> T put ( URL url, T obj ) throws Exception
	{
		return put(url, obj, null);
	}

	@SuppressWarnings ( "unchecked" )
	public <T> T put ( URL url, T obj, Map<String, String> newHeaders ) throws Exception
	{
		log.debug("Sending PUT to " + url);
		HttpPut put = new HttpPut(url.toURI());
		setupMethod(put, newHeaders);
		writeObject(obj, put);
		HttpResponse response = getHttpClient().execute(put);
		checkError(response);
		return (T) readObject(obj.getClass(), response);
	}

	private <T> T readObject ( Class<T> clazz, HttpResponse response ) throws IOException, JsonProcessingException,
	ParseException
	{
		if (!useXml)
			try {
				T obj = mapper.reader(clazz).readValue(
						response.getEntity().getContent());
				return obj;
			} catch (JsonParseException e) {
				throw new ParseException("Bad object Class");
			}
		else
			try
		{
				JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
				Unmarshaller jaxbUnmarshaller = jaxbContext
						.createUnmarshaller();
				@SuppressWarnings("unchecked")
				T obj = (T) jaxbUnmarshaller.unmarshal(response.getEntity()
						.getContent());
				return obj;
		} catch (JAXBException e)
		{
			throw new ParseException("Bad object Class");
		}

	}

	/**
	 * Set an exception translator for server errors
	 * 
	 * @param exceptionTranslator
	 * 
	 */
	public void setExceptionTranslator ( ExceptionTranslator exceptionTranslator )
	{
		this.exceptionTranslator = exceptionTranslator;
	}

	public void setHeaders ( Map<String, String> headers )
	{
		this.headers = headers;
	}

	private void setupMethod ( HttpRequest request, Map<String, String> newHeaders )
	{
		applyHeaders(request, newHeaders);
		if (authenticated)
		{
			String authorization = "Basic";
			String encoded = Base64Variants.MIME.encode((username + ":" + password).getBytes());
			authorization = "Basic " + encoded;
			request.addHeader("Authorization", authorization);
		}

	}

	/**
	 * @param useXml
	 *            if true client uses xml instead of json.
	 */
	public void setUseXml(boolean useXml){
		this.useXml=useXml;
	}

	private <T> void writeObject ( T obj, HttpEntityEnclosingRequest request ) throws IOException,
	JsonGenerationException, MappingException, JsonProcessingException, ParseException
	{
		if (!useXml)
			try {
				request.addHeader("Content-type", "application/json");
				ObjectWriter writer = mapper.writer();
				String payload = writer.writeValueAsString(obj);
				StringEntity entity = new StringEntity(payload);
				log.debug("Payload:\n " + payload);
				request.setEntity(entity);
			} catch (JsonMappingException e) {
				throw new MappingException("Error while mapping Object to Json");
			}
		else
			try {
				request.addHeader("Content-type", "application/xml");
				JAXBContext jaxbContext = JAXBContext.newInstance(obj
						.getClass());
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				jaxbMarshaller.marshal(obj, baos);
				String payload = baos.toString();
				StringEntity entity = new StringEntity(payload);
				log.debug("Payload:\n " + payload);
				request.setEntity(entity);
			} catch (JAXBException e) {
				throw new MappingException("Error while mapping Object to xml");
			}

	}

}
