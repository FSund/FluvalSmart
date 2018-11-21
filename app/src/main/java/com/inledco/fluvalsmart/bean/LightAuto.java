package com.inledco.fluvalsmart.bean;

import android.util.Log;

import com.ble.api.DataUtil;

import java.io.Serializable;
import java.util.Arrays;

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

    private boolean mHasTurnoff;
    private boolean mTurnoffEnable;
    private byte mTurnoffHour;
    private byte mTurnoffMinute;

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

    public LightAuto( RampTime sunrise, byte[] dayBright, RampTime sunset, byte[] nightBright, boolean turnoffEnable, byte turnoffHour, byte turnoffMinute )
    {
        mSunrise = sunrise;
        mDayBright = dayBright;
        mSunset = sunset;
        mNightBright = nightBright;
        mHasTurnoff = true;
        mTurnoffEnable = turnoffEnable;
        mTurnoffHour = turnoffHour;
        mTurnoffMinute = turnoffMinute;
    }

    public LightAuto( RampTime sunrise, byte[] dayBright, RampTime sunset, byte[] nightBright, byte week, RampTime dynamicPeriod, byte dynamicMode )
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

    public LightAuto( RampTime sunrise, byte[] dayBright, RampTime sunset, byte[] nightBright, boolean turnoffEnable, byte turnoffHour, byte turnoffMinute, byte week, RampTime dynamicPeriod, byte dynamicMode )
    {
        mSunrise = sunrise;
        mDayBright = dayBright;
        mSunset = sunset;
        mNightBright = nightBright;
        mHasTurnoff = true;
        mTurnoffEnable = turnoffEnable;
        mTurnoffHour = turnoffHour;
        mTurnoffMinute = turnoffMinute;
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

    public boolean isHasTurnoff()
    {
        return mHasTurnoff;
    }

    public void setHasTurnoff( boolean hasTurnoff )
    {
        mHasTurnoff = hasTurnoff;
    }

    public boolean isTurnoffEnable()
    {
        return mTurnoffEnable;
    }

    public void setTurnoffEnable( boolean turnoffEnable )
    {
        mTurnoffEnable = turnoffEnable;
    }

    public byte getTurnoffHour()
    {
        return mTurnoffHour;
    }

    public void setTurnoffHour( byte turnoffHour )
    {
        mTurnoffHour = turnoffHour;
    }

    public byte getTurnoffMinute()
    {
        return mTurnoffMinute;
    }

    public void setTurnoffMinute( byte turnoffMinute )
    {
        mTurnoffMinute = turnoffMinute;
    }

    public void setHasDynamic( boolean hasDynamic )
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

    public int[] getTimeArray()
    {
        int[] array;
        if ( mHasTurnoff && mTurnoffEnable )
        {
            array = new int[6];
            array[0] = mSunrise.getStart();
            array[1] = mSunrise.getEnd();
            array[2] = mSunset.getStart();
            array[3] = mSunset.getEnd();
            array[4] = mTurnoffHour*60 + mTurnoffMinute;
            array[5] = array[4];
        }
        else
        {
            array = new int[4];
            array[0] = mSunrise.getStart();
            array[1] = mSunrise.getEnd();
            array[2] = mSunset.getStart();
            array[3] = mSunset.getEnd();
        }
        return array;
    }

    public boolean isTimeValid()
    {
        int[] array = getTimeArray();
        /* sort time && check time is valid or not */
        int[] index = new int[array.length];
        for ( int i = 0; i < index.length; i++ )
        {
            index[i] = i;
        }
        for ( int i = index.length - 1; i > 0; i-- )
        {
            for ( int j = 0; j < i; j++ )
            {
                if ( array[index[j]] > array[index[j+1]] )
                {
                    int tmp = index[j];
                    index[j] = index[j+1];
                    index[j+1] = tmp;
                }
            }
        }
        for ( int i = 0; i < index.length; i++ )
        {
            int j = (i+1)%index.length;
            if ( (index[i]+1)%index.length != index[j]%index.length )
            {
                return false;
            }
        }
        return true;
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

    public boolean equal( LightAuto a )
    {
        if ( a == null )
        {
            return false;
        }
        if ( mSunrise.equal( a.getSunrise() ) == false)
        {
            return false;
        }
        if ( mSunset.equal( a.getSunset() ) == false)
        {
            return false;
        }
        if ( Arrays.equals( mDayBright, a.getDayBright() ) == false )
        {
            return false;
        }
        if ( Arrays.equals( mNightBright, a.getNightBright() ) == false )
        {
            return false;
        }
        if ( mHasTurnoff != a.isHasTurnoff() || mTurnoffEnable != a.isTurnoffEnable()
            || mTurnoffHour != a.getTurnoffHour() || mTurnoffMinute != a.getTurnoffMinute())
        {
            return false;
        }
        Log.e( "TAGG", "equal: " + (mDynamicPeriod == null) + "  " + (a.getDynamicPeriod() == null) );
        if ( mHasDynamic != a.isHasDynamic() || mDynamicEnable != a.isDynamicEnable()
            || mSun != a.isSun() || mMon != a.isMon() || mTue != a.isTue() || mWed != a.isWed()
            || mThu != a.isThu() || mFri != a.isFri() || mSat != a.isSat() || mDynamicMode != a.getDynamicMode() )
        {
            return false;
        }
        if ( mDynamicPeriod != null && mDynamicPeriod.equal( a.getDynamicPeriod() ) == false )
        {
            return false;
        }
        if ( mDynamicPeriod == null && a.getDynamicPeriod() != null )
        {
            return false;
        }
        return true;
    }
}
