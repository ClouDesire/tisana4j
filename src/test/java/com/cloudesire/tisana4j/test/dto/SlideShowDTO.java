package com.cloudesire.tisana4j.test.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement ( name = "slideshow" )
@XmlAccessorType ( XmlAccessType.FIELD )
public class SlideShowDTO
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

    @XmlAccessorType ( XmlAccessType.FIELD )
    public static class Slide
    {
        @XmlAttribute
        private String type;
        @XmlElement
        private String title;
        @XmlElement
        private String item;

        public Slide()
        {
        }

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
