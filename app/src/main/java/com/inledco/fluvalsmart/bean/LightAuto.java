package com.inledco.fluvalsmart.bean;

import com.ble.api.DataUtil;

import java.io.Serializable;

/**
 * Created by liruya on 2016/11/23.
 */

public class LightAuto implements Serializable
{
    private static final long serialVersionUID = 7284666673318500458L;
    private RampTime mSunrise;
    private byte[] mDayBright;
    private RampTime mSunset;
    private byte[] mNightBright;
    private boolean mHasDynamic;
    private boolean mDynamicEnable;
    private boolean mSat;
    private boolean mFri;
    private boolean mThu;
    private boolean mWed;
    private boolean mTue;
    private boolean mMon;
    private boolean mSun;
    private RampTime mDynamicPeriod;
    private byte mDynamicMode;

    public LightAuto ( RampTime sunrise, byte[] dayBright, RampTime sunset, byte[] nightBright )
    {
        mSunrise = sunrise;
        mDayBright = dayBright;
        mSunset = sunset;
        mNightBright = nightBright;
    }

    public LightAuto ( RampTime sunrise, byte[] dayBright, RampTime sunset, byte[] nightBright, byte week, RampTime dynamicPeriod, byte dynamicMode )
    {
        mSunrise = sunrise;
        mDayBright = dayBright;
        mSunset = sunset;
        mNightBright = nightBright;
        mHasDynamic = true;
        mDynamicEnable = (week&0x80) == 0x80 ? true : false;
        mSat = (week&0x40) == 0x40 ? true : false;
        mFri = (week&0x20) == 0x20 ? true : false;
        mThu = (week&0x10) == 0x10 ? true : false;
        mWed = (week&0x08) == 0x08 ? true : false;
        mTue = (week&0x04) == 0x04 ? true : false;
        mMon = (week&0x02) == 0x02 ? true : false;
        mSun = (week&0x01) == 0x01 ? true : false;
        mDynamicPeriod = dynamicPeriod;
        mDynamicMode = dynamicMode;
    }

    public byte getWeek()
    {
        byte b = 0x00;
        if ( mDynamicEnable )
        {
            b |= 0x80;
        }
        if ( mSat )
        {
            b |= 0x40;
        }
        if ( mFri )
        {
            b |= 0x20;
        }
        if ( mThu )
        {
            b |= 0x10;
        }
        if ( mWed )
        {
            b |= 0x08;
        }
        if ( mTue )
        {
            b |= 0x04;
        }
        if ( mMon )
        {
            b |= 0x02;
        }
        if ( mSun )
        {
            b |= 0x01;
        }
        return b;
    }

    public RampTime getSunrise ()
    {
        return mSunrise;
    }

    public void setSunrise ( RampTime sunrise )
    {
        mSunrise = sunrise;
    }

    public byte[] getDayBright ()
    {
        return mDayBright;
    }

    public void setDayBright ( byte[] dayBright )
    {
        mDayBright = dayBright;
    }

    public RampTime getSunset ()
    {
        return mSunset;
    }

    public void setSunset ( RampTime sunset )
    {
        mSunset = sunset;
    }

    public byte[] getNightBright ()
    {
        return mNightBright;
    }

    public void setNightBright ( byte[] nightBright )
    {
        mNightBright = nightBright;
    }

    public void setHasDynamic ( boolean hasDynamic )
    {
        mHasDynamic = hasDynamic;
    }

    public boolean isHasDynamic ()
    {
        return mHasDynamic;
    }

    public boolean isDynamicEnable ()
    {
        return mDynamicEnable;
    }

    public void setDynamicEnable ( boolean dynamicEnable )
    {
        mDynamicEnable = dynamicEnable;
    }

    public boolean isSat ()
    {
        return mSat;
    }

    public void setSat ( boolean sat )
    {
        mSat = sat;
    }

    public boolean isFri ()
    {
        return mFri;
    }

    public void setFri ( boolean fri )
    {
        mFri = fri;
    }

    public boolean isThu ()
    {
        return mThu;
    }

    public void setThu ( boolean thu )
    {
        mThu = thu;
    }

    public boolean isWed ()
    {
        return mWed;
    }

    public void setWed ( boolean wed )
    {
        mWed = wed;
    }

    public boolean isTue ()
    {
        return mTue;
    }

    public void setTue ( boolean tue )
    {
        mTue = tue;
    }

    public boolean isMon ()
    {
        return mMon;
    }

    public void setMon ( boolean mon )
    {
        mMon = mon;
    }

    public boolean isSun ()
    {
        return mSun;
    }

    public void setSun ( boolean sun )
    {
        mSun = sun;
    }

    public RampTime getDynamicPeriod ()
    {
        return mDynamicPeriod;
    }

    public void setDynamicPeriod ( RampTime dynamicPeriod )
    {
        mDynamicPeriod = dynamicPeriod;
    }

    public byte getDynamicMode ()
    {
        return mDynamicMode;
    }

    public void setDynamicMode ( byte dynamicMode )
    {
        mDynamicMode = dynamicMode;
    }

    @Override
    public String toString ()
    {
        String str = "Sunrise: " + mSunrise.getStartHour() + ":" + mSunrise.getStartMinute() + " - " + mSunrise.getEndHour() + ":" + mSunrise.getEndMinute()
                     + "\r\nDayLight: " + DataUtil.byteArrayToHex( mDayBright )
                     + "\r\nSunset: " + mSunset.getStartHour() + ":" + mSunset.getStartMinute() + " - " + mSunset.getEndHour() + ":" + mSunset.getEndMinute()
                     + "\r\nNightLight: " + DataUtil.byteArrayToHex( mNightBright );
        return str;
    }
}
