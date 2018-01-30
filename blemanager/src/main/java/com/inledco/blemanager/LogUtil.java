package com.inledco.blemanager;

import android.util.Log;

/**
 * Created by liruya on 2017/4/25.
 */

public class LogUtil
{
    private static final boolean ENABLE_LOGCAT = true;

    public static void v(String tag, String msg)
    {
        if ( ENABLE_LOGCAT )
        {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if ( ENABLE_LOGCAT )
        {
            Log.d( tag, msg );
        }
    }

    public static void i(String tag, String msg)
    {
        if ( ENABLE_LOGCAT )
        {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg)
    {
        if ( ENABLE_LOGCAT )
        {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg)
    {
        if ( ENABLE_LOGCAT )
        {
            Log.e(tag, msg);
        }
    }
}
