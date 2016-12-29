package com.example.sengloke.InfoGo.pictureTaking;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyImage implements Serializable
{
    private String title, description, path;
    private Double latitude, longitude;
    private long datetimeLong;
    private SimpleDateFormat df = new SimpleDateFormat("MMMM d, yy  h:mm");

    public MyImage(String title, String description, String path, long datetimeLong, Double latitude, Double longitude)
    {
        this.title = title;
        this.description = description;
        this.path = path;
        this.datetimeLong = datetimeLong;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public MyImage()
    {
    }

    public String getTitle()
    {
        return title;
    }


    public Calendar getDatetime()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(datetimeLong);
        return cal;
    }

    public void setDatetime(long datetimeLong)
    {
        this.datetimeLong = datetimeLong;
    }


    public void setDatetime(Calendar datetime)
    {
        this.datetimeLong = datetime.getTimeInMillis();
    }


    public String getDescription()
    {
        return description;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }

    public long getDatetimeLong()
    {
        return datetimeLong;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Double getLatitude()
    {
        return latitude;
    }
    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }
    public Double getLongitude()
    {
        return longitude;
    }
    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
    public String details()
    {
        return "Image taken on : " + df.format(getDatetime().getTime()) +
                "\nAt : " + title +
                "\nDescription : " + description;
    }
    @Override public String toString()
    {
        return  title +"~"  +
                latitude + "~" +
                longitude ;
    }
}
