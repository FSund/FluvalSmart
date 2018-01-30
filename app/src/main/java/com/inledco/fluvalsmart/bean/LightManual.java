package com.inledco.fluvalsmart.bean;

import java.io.Serializable;

/**
 * Created by liruya on 2016/11/23.
 */

public class LightManual implements Serializable
{
    private static final long serialVersionUID = -5609114933945473748L;
    private boolean mOn;
    private byte mDynamic;
    private short[] mChnValues;
    private byte[] mCustomP1Values;
    private byte[] mCustomP2Values;
    private byte[] mCustomP3Values;
    private byte[] mCustomP4Values;

    public LightManual ( boolean on, byte dynamic, short[] chnValues, byte[] customP1Values, byte[] customP2Values, byte[] customP3Values,
                         byte[] customP4Values )
    {
        mOn = on;
        mDynamic = dynamic;
        mChnValues = chnValues;
        mCustomP1Values = customP1Values;
        mCustomP2Values = customP2Values;
        mCustomP3Values = customP3Values;
        mCustomP4Values = customP4Values;
    }

    public boolean isOn ()
    {
        return mOn;
    }

    public void setOn ( boolean on )
    {
        mOn = on;
    }

    public byte getDynamic ()
    {
        return mDynamic;
    }

    public void setDynamic ( byte dynamic )
    {
        mDynamic = dynamic;
    }

    public short[] getChnValues ()
    {
        return mChnValues;
    }

    public void setChnValues ( short[] chnValues )
    {
        mChnValues = chnValues;
    }

    public byte[] getCustomP1Values ()
    {
        return mCustomP1Values;
    }

    public void setCustomP1Values ( byte[] customP1Values )
    {
        mCustomP1Values = customP1Values;
    }

    public byte[] getCustomP2Values ()
    {
        return mCustomP2Values;
    }

    public void setCustomP2Values ( byte[] customP2Values )
    {
        mCustomP2Values = customP2Values;
    }

    public byte[] getCustomP3Values ()
    {
        return mCustomP3Values;
    }

    public void setCustomP3Values ( byte[] customP3Values )
    {
        mCustomP3Values = customP3Values;
    }

    public byte[] getCustomP4Values ()
    {
        return mCustomP4Values;
    }

    public void setCustomP4Values ( byte[] customP4Values )
    {
        mCustomP4Values = customP4Values;
    }
}
