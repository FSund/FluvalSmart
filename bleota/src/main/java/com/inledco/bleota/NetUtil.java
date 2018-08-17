package com.inledco.bleota;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil
{
    public static boolean isNetworkAvailable( Context context)
    {
        if ( context == null )
        {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        if ( manager == null )
        {
            return false;
        }
        NetworkInfo info = manager.getActiveNetworkInfo();
        if ( info == null || info.isAvailable() == false )
        {
            return false;
        }
        return true;
    }
}
