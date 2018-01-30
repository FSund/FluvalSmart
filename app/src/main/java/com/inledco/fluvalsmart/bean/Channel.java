package com.inledco.fluvalsmart.bean;

/**
 * Created by liruya on 2016/10/28.
 */

public class Channel
{
    private String name;
    private int color;
    private short value;

    public Channel ( String name, int color, short value )
    {
        this.name = name;
        this.color = color;
        this.value = value;
    }

    public String getName ()
    {
        return name;
    }

    public void setName ( String name )
    {
        this.name = name;
    }

    public int getColor ()
    {
        return color;
    }

    public void setColor ( int color )
    {
        this.color = color;
    }

    public short getValue ()
    {
        return value;
    }

    public void setValue ( short value )
    {
        this.value = value;
    }
}
