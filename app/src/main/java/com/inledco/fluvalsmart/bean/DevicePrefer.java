package com.inledco.fluvalsmart.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/20.
 */

public class DevicePrefer implements Serializable
{
    private static final long serialVersionUID = -3359140079103510542L;
    private short mDevId;
    private String mDeviceMac;
    private String mDeviceName;
    private String mTank;

    public DevicePrefer ( String deviceMac, String deviceName )
    {
        mDeviceMac = deviceMac;
        mDeviceName = deviceName;
    }

    public DevicePrefer ( short devId, String deviceMac, String deviceName )
    {
        mDevId = devId;
        mDeviceMac = deviceMac;
        mDeviceName = deviceName;
    }

    public short getDevId ()
    {
        return mDevId;
    }

    public void setDevId ( short devId )
    {
        mDevId = devId;
    }

    public String getDeviceName ()
    {
        return mDeviceName;
    }

    public void setDeviceName ( String deviceName )
    {
        mDeviceName = deviceName;
    }

    public String getDeviceMac ()
    {
        return mDeviceMac;
    }

    public void setDeviceMac ( String deviceMac )
    {
        mDeviceMac = deviceMac;
    }

    public String getTank ()
    {
        return mTank;
    }

    public void setTank ( String tank )
    {
        mTank = tank;
    }
}
