package com.inledco.fluvalsmart.bean;

import android.support.annotation.DrawableRes;

/**
 * Created by liruya on 2016/10/28.
 */

public class Channel
{
    private String name;
    private int color;
    private @DrawableRes int icon;
    private short value;

    public Channel(String name, int color, @DrawableRes int icon)
    {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.value = 0;
    }

    public Channel(String name, int color, short value)
    {
        this.name = name;
        this.color = color;
        this.value = value;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public int getColor ()
    {
        return color;
    }

    public void setColor (int color)
    {
        this.color = color;
    }

    public @DrawableRes int getIcon()
    {
        return icon;
    }

    public void setIcon(@DrawableRes int icon)
    {
        this.icon = icon;
    }

    public short getValue()
    {
        return value;
    }

    public void setValue (short value)
    {
        this.value = value;
    }
}
