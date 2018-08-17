package com.inledco.fluvalsmart.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class LightPro implements Serializable
{
    private static final long serialVersionUID = -9208889349220365301L;
    public static final int POINT_COUNT_MIN = 4;
    public static final int POINT_COUNT_MAX = 10;

    private int mPointCount;
    private TimerBrightPoint[] mPoints;

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

    private LightPro()
    {
        mPointCount = POINT_COUNT_MAX;
        mPoints = new TimerBrightPoint[POINT_COUNT_MAX];
    }

    private LightPro( int pointCount )
    {
        mPointCount = pointCount;
        mPoints = new TimerBrightPoint[POINT_COUNT_MAX];
    }

    public int getPointCount()
    {
        return mPointCount;
    }

    public void setPointCount( int pointCount )
    {
        if ( pointCount >= POINT_COUNT_MIN && pointCount <= POINT_COUNT_MAX )
        {
            mPointCount = pointCount;
        }
    }

    public TimerBrightPoint[] getPoints()
    {
        return mPoints;
    }

    public void setPoints( TimerBrightPoint[] points )
    {
        mPoints = points;
    }

    public boolean isHasDynamic()
    {
        return mHasDynamic;
    }

    public void setHasDynamic( boolean hasDynamic )
    {
        mHasDynamic = hasDynamic;
    }

    public boolean isDynamicEnable()
    {
        return mDynamicEnable;
    }

    public void setDynamicEnable( boolean dynamicEnable )
    {
        mDynamicEnable = dynamicEnable;
    }

    public boolean isSat()
    {
        return mSat;
    }

    public void setSat( boolean sat )
    {
        mSat = sat;
    }

    public boolean isFri()
    {
        return mFri;
    }

    public void setFri( boolean fri )
    {
        mFri = fri;
    }

    public boolean isThu()
    {
        return mThu;
    }

    public void setThu( boolean thu )
    {
        mThu = thu;
    }

    public boolean isWed()
    {
        return mWed;
    }

    public void setWed( boolean wed )
    {
        mWed = wed;
    }

    public boolean isTue()
    {
        return mTue;
    }

    public void setTue( boolean tue )
    {
        mTue = tue;
    }

    public boolean isMon()
    {
        return mMon;
    }

    public void setMon( boolean mon )
    {
        mMon = mon;
    }

    public boolean isSun()
    {
        return mSun;
    }

    public void setSun( boolean sun )
    {
        mSun = sun;
    }

    public RampTime getDynamicPeriod()
    {
        return mDynamicPeriod;
    }

    public void setDynamicPeriod( RampTime dynamicPeriod )
    {
        mDynamicPeriod = dynamicPeriod;
    }

    public byte getDynamicMode()
    {
        return mDynamicMode;
    }

    public void setDynamicMode( byte dynamicMode )
    {
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

    public byte[] toArray()
    {
        int chn = mPoints[0].getBrights().length;
        int len = mPointCount*(chn+2)+1;
        if ( mHasDynamic )
        {
            len += 6;
        }

        Arrays.sort( mPoints, 0, mPointCount, new Comparator< TimerBrightPoint >() {
            @Override
            public int compare( TimerBrightPoint o1, TimerBrightPoint o2 )
            {
                if ( o1 == null || o2 == null )
                {
                    return 0;
                }
                if ( o1.getTimer() < o2.getTimer() )
                {
                    return -1;
                }
                else if ( o1.getTimer() > o2.getTimer() )
                {
                    return 1;
                }
                return 0;
            }
        } );
        /* sort */
//        int[] index = new int[mPointCount];
//        int[] tmr = new int[mPointCount];
//        for ( int i = 0; i < mPointCount; i++ )
//        {
//            index[i] = i;
//            tmr[i] = mPoints[i].getHour()*60+mPoints[i].getMinute();
//        }
//        for ( int i = mPointCount-1; i > 0 ; i-- )
//        {
//            for ( int j = 0; j < i; j++ )
//            {
//                if ( tmr[index[j]] > tmr[index[j+1]] )
//                {
//                    int temp = index[j];
//                    index[j] = index[j+1];
//                    index[j+1] = temp;
//                }
//            }
//        }

        byte[] array = new byte[len];
        array[0] = (byte) mPointCount;
        int pos = 1;
        for ( int i = 0; i < mPointCount; i++ )
        {
            byte[] a = mPoints[i].toArray();
            System.arraycopy( a, 0, array, pos, a.length );
            pos += a.length;
        }
        if ( mHasDynamic )
        {
            array[len-6] = getWeek();
            array[len-5] = mDynamicPeriod.getStartHour();
            array[len-4] = mDynamicPeriod.getStartMinute();
            array[len-3] = mDynamicPeriod.getEndHour();
            array[len-2] = mDynamicPeriod.getEndMinute();
            array[len-1] = mDynamicMode;
        }
        return array;
    }

    public static class Builder
    {
        public LightPro creatFromArray( byte[] array, int chn )
        {
            if ( chn <= 0 || chn > 6 || array == null || array.length == 0 )
            {
                return null;
            }
            int count = array[0];
            if ( count < POINT_COUNT_MIN || count > POINT_COUNT_MAX )
            {
                return null;
            }
            int len = count*(2+chn)+1;
            if ( array.length != len && array.length != len + 6 )
            {
                return null;
            }
            LightPro lightPro = new LightPro( count );
            for ( int i = 0; i < count; i++ )
            {
                int hour = array[i * ( 2 + chn ) + 1];
                int minute = array[i * ( 2 + chn ) + 2];
                byte[] brights = new byte[chn];
                for ( int j = 0; j < chn; j++ )
                {
                    brights[j] = array[i * ( 2 + chn ) + 3 + j];
                }
                lightPro.mPoints[i] = new TimerBrightPoint( hour, minute, brights );
            }
            if ( array.length == len + 6 )
            {
                lightPro.mHasDynamic = true;
                lightPro.mDynamicEnable = (array[len]&0x80) == 0x80 ? true : false;
                lightPro.mSat = (array[len]&0x40) == 0x40 ? true : false;
                lightPro.mFri = (array[len]&0x20) == 0x20 ? true : false;
                lightPro.mThu = (array[len]&0x10) == 0x10 ? true : false;
                lightPro.mWed = (array[len]&0x08) == 0x08 ? true : false;
                lightPro.mTue = (array[len]&0x04) == 0x04 ? true : false;
                lightPro.mMon = (array[len]&0x02) == 0x02 ? true : false;
                lightPro.mSun = (array[len]&0x01) == 0x01 ? true : false;
                lightPro.mDynamicPeriod = new RampTime( array[len+1], array[len+2], array[len+3], array[len+4] );
                lightPro.mDynamicMode = array[len+5];
            }
            return lightPro;
        }
    }
}
