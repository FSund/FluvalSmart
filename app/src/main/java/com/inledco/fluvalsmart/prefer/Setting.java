package com.inledco.fluvalsmart.prefer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by Administrator on 2016/10/19.
 * 保存在本地的参数数据类
 */

public class Setting
{
    public static final String KEY_AUTO_TURNON_BLE = "AUTO_TURNON_BLE";
    public static final String KEY_EXIT_TURNOFF_BLE = "EXIT_TURNOFF_BLE";
//    public static final String KEY_EXIT_TIP = "EXIT_TIP";
    public static final String KEY_COUNTRY_LANGUAGE_SELECTED = "IS_COUNTRY_LANGUAGE_SELECTED";
    public static final String KEY_COUNTRY = "COUNTRY";
    public static final String KEY_LANGUAGE = "LANGUAGE";
    public static final String KEY_LANGUAGE_AUTO = "auto";
    public static final String KEY_LANGUAGE_ENGLISH = "en";
    public static final String KEY_LANGUAGE_GERMANY = "de";
    public static final String KEY_LANGUAGE_FRENCH = "fr";
    public static final String KEY_LANGUAGE_SPANISH = "es";
    public static final String KEY_LANGUAGE_CHINESE = "zh";
    public static final String KEY_SCAN_TIP = "scan_tip";
    public static final String KEY_UPGRADE_TIP = "upgrade_tip";

    public static boolean showRssi()
    {
        return false;
    }

    public static boolean forceUpdate()
    {
        return true;
    }

    public static boolean isAutoTurnonBle(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getBoolean( KEY_AUTO_TURNON_BLE, false );
        }
        return false;
    }

    public static void setAutoTurnonBle( Context context, boolean b )
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putBoolean( KEY_AUTO_TURNON_BLE, b );
            editor.commit();
        }
    }

    public static boolean isExitTurnoffBle(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getBoolean( KEY_EXIT_TURNOFF_BLE, false );
        }
        return false;
    }

    public static void setExitTurnoffBle( Context context, boolean b )
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putBoolean( KEY_EXIT_TURNOFF_BLE, b );
            editor.commit();
        }
    }

    public static boolean hasSelectCountryLanguage(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getBoolean( KEY_COUNTRY_LANGUAGE_SELECTED, false );
        }
        return false;
    }

    public static void setSelectCountryLanguage( Context context )
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putBoolean( KEY_COUNTRY_LANGUAGE_SELECTED, true );
            editor.commit();
        }
    }

    public static boolean hasScanTip(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getBoolean( KEY_SCAN_TIP, false );
        }
        return false;
    }

    public static void setScanTip( Context context )
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putBoolean( KEY_SCAN_TIP, true );
            editor.commit();
        }
    }

    public static boolean hasUpgradeTip(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getBoolean( KEY_UPGRADE_TIP, false );
        }
        return false;
    }

    public static void setUpgradeTip(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putBoolean( KEY_UPGRADE_TIP, true );
            editor.commit();
        }
    }

    public static String getCountry(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getString( KEY_COUNTRY, "" );
        }
        return "";
    }

    public static void setCountry( Context context, String c )
    {
        if ( context != null && !TextUtils.isEmpty( c ) )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putString( KEY_COUNTRY, c );
            editor.commit();
        }
    }

    public static String getLanguage(Context context)
    {
        if ( context != null )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            return sp.getString( KEY_LANGUAGE, KEY_LANGUAGE_AUTO );
        }
        return KEY_LANGUAGE_AUTO;
    }

    public static void setLanguage( Context context, String c )
    {
        if ( context != null && !TextUtils.isEmpty( c ) )
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor editor = sp.edit().putString( KEY_LANGUAGE, c );
            editor.commit();
        }
    }

    public static void changeAppLanguage ( Context context )
    {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        String lang = getLanguage( context );
        if ( TextUtils.isEmpty( lang ) )
        {
            lang = KEY_LANGUAGE_AUTO;
        }
        switch ( lang )
        {
            case KEY_LANGUAGE_AUTO:
                config.setLocale( Resources.getSystem()
                                           .getConfiguration().locale );
                break;
            case KEY_LANGUAGE_ENGLISH:
                config.setLocale( Locale.ENGLISH );
                break;
            case KEY_LANGUAGE_GERMANY:
                config.setLocale( Locale.GERMANY );
                break;
            case KEY_LANGUAGE_FRENCH:
                config.setLocale( Locale.FRENCH );
                break;
            case KEY_LANGUAGE_SPANISH:
                config.setLocale( new Locale( "es", "ES" ) );
                break;
            case KEY_LANGUAGE_CHINESE:
                config.setLocale( Locale.SIMPLIFIED_CHINESE );
                break;
            default:
                config.setLocale( Resources.getSystem()
                                           .getConfiguration().locale );
                break;
        }
        res.updateConfiguration( config, dm );
    }
}
