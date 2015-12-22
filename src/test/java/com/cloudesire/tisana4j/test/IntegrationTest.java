package com.cloudesire.tisana4j.test;

import com.cloudesire.tisana4j.Pair;
import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.cloudesire.tisana4j.exceptions.ResourceNotFoundException;
import com.cloudesire.tisana4j.exceptions.RestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTest
{
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void test() throws Exception
    {
        RestClient client = RestClientFactory.getDefaultClient();

        NetworkAddress testClass = client.get( new URL( "https://httpbin.org/ip" ), NetworkAddress.class );

        assertNotNull( testClass );
        assertNotNull( testClass.getOrigin() );
    }

    @Test
    public void testXML() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultXmlClient();

        final SlideShow response = client.get( new URL( "https://httpbin.org/xml" ), SlideShow.class );

        assertNotNull(response.getAuthor());
        assertNotNull(response.getTitle());
        assertNotNull(response.getDate());
        assertNotNull(response.getSlide());
        for ( Slide slide : response.getSlide() )
        {
            assertNotNull( slide.getType() );
            assertNotNull( slide.getTitle() );
        }
    }

    @Test
    public void testNoCompression() throws Exception
    {
        RestClient defaultClient = RestClientFactory.getDefaultClient();
        RestClient noCompressionClient = RestClientFactory.getNoCompressionClient();

        InputStream gzippedResponse = defaultClient.get( new URL( "https://httpbin.org/gzip" ), InputStream.class );
        final JsonNode gzippedJsonNode = jsonMapper.readTree( gzippedResponse );
        assertTrue( "value", gzippedJsonNode.findValue( "gzipped" ).asBoolean() );


        InputStream plainTextResponse = noCompressionClient.get( new URL( "https://httpbin.org/get" ), InputStream.class );
        final JsonNode plainTextJsonNode = jsonMapper.readTree( plainTextResponse );
        assertNotNull( "value", plainTextJsonNode.findValue( "headers" ) );
        assertNull( "value", plainTextJsonNode.findValue( "headers" ).findValue( "Accept-Encoding" ) );
    }

    @Test
    public void testPostForm() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        List<Pair> values = new ArrayList<>();
        values.add( new Pair( "key", "value" ) );

        InputStream response = client.postFormData( new URL( "https://httpbin.org/post" ), values, InputStream.class );

        final JsonNode jsonNode = jsonMapper.readTree( response );
        assertEquals( "value", jsonNode.findValue( "form" ).findValue( "key" ).asText() );
    }

    @Test ( expected = ResourceNotFoundException.class )
    public void sniCustomCert() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://ines-gtt-test.liberologico.com/asd" ) );
    }

    @Test ( expected = ResourceNotFoundException.class )
    public void testNoSni() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getNoValidationClient();
        client.get( new URL( "https://web001.liberologico.com/asd" ) );
    }

    @Test
    public void testCloudFlareSNI() throws IOException, RestException
    {
        RestClient client = RestClientFactory.getDefaultClient();
        client.get( new URL( "https://cloudesire.cloud/" ) );
    }

    private static class NetworkAddress
    {
        private String origin;

        public String getOrigin()
        {
            return origin;
        }
    }

    @XmlRootElement (name="slideshow")
    @XmlAccessorType ( XmlAccessType.FIELD )
    private static class SlideShow
    {
        @XmlAttribute
        private String title;
        @XmlAttribute
        private String date;
        @XmlAttribute
        private String author;
        @XmlElement
        private List<Slide> slide;

        public String getTitle()
        {
            return title;
        }

        public void setTitle( String title )
        {
            this.title = title;
        }

        public String getDate()
        {
            return date;
        }

        public void setDate( String date )
        {
            this.date = date;
        }

        public String getAuthor()
        {
            return author;
        }

        public void setAuthor( String author )
        {
            this.author = author;
        }

        public List<Slide> getSlide()
        {
            return slide;
        }

        public void setSlide( List<Slide> slide )
        {
            this.slide = slide;
        }
    }

    @XmlAccessorType ( XmlAccessType.FIELD )
    private static class Slide
    {
        @XmlAttribute
        private String type;
        @XmlElement
        private String title;
        @XmlElement
        private String item;

        public Slide(){}

        public Slide( String title )
        {
            this.title = title;
        }

        public String getType()
        {
            return type;
        }

        public void setType( String type )
        {
            this.type = type;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle( String title )
        {
            this.title = title;
        }

        public String getItem()
        {
            return item;
        }

        public void setItem( String item )
        {
            this.item = item;
        }
    }
}
