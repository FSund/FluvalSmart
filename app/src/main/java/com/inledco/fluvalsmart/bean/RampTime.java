package com.inledco.fluvalsmart.bean;

import java.io.Serializable;

/**
 * Created by liruya on 2016/11/23.
 */

public class RampTime implements Serializable
{
    private static final long serialVersionUID = -3985727232820727495L;
    private byte startHour;
    private byte startMinute;
    private byte endHour;
    private byte endMinute;

    public RampTime (byte startHour, byte startMinute, byte endHour, byte endMinute)
    {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public byte getStartHour ()
    {
        return startHour;
    }

    public void setStartHour (byte startHour)
    {
        this.startHour = startHour;
    }

    public byte getStartMinute ()
    {
        return startMinute;
    }

    public void setStartMinute (byte startMinute)
    {
        this.startMinute = startMinute;
    }

    public byte getEndHour ()
    {
        return endHour;
    }

    public void setEndHour (byte endHour)
    {
        this.endHour = endHour;
    }

    public byte getEndMinute ()
    {
        return endMinute;
    }

    public void setEndMinute (byte endMinute)
    {
        this.endMinute = endMinute;
    }

    public int getStart()
    {
        return (startHour*60+startMinute);
    }

    public int getEnd()
    {
        return (endHour*60+endMinute);
    }

    public boolean equal(RampTime rt)
    {
        if (rt == null)
        {
            return false;
        }
        if (startHour != rt.getStartHour())
        {
            return false;
        }
        if (startMinute != rt.getStartMinute())
        {
            return false;
        }
        if (endHour != rt.getEndHour())
        {
            return false;
        }
        return endMinute == rt.getEndMinute();
    }
}
