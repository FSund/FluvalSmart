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
    public static final String SET_BLE_ENABLED = "BLE_ENABLED";
    public static final String SET_EXIT_TURNOFF_BLE = "EXIT_TURNOFF_BLE";
    public static final String SET_EXIT_TIP = "EXIT_TIP";
    public static final String SET_COUNTRY_LANGUAGE_SELECTED = "IS_COUNTRY_LANGUAGE_SELECTED";
    public static final String SET_COUNTRY = "COUNTRY";
    public static final String SET_LANGUAGE = "LANGUAGE";
    public static final String LANGUAGE_AUTO = "auto";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_GERMANY = "de";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_CHINESE = "zh";

    public static boolean mBleEnabled;
    public static boolean mExitTurnOffBle;
    public static boolean mExitTip;
    public static boolean mHasSelect;
    public static String mCountry;
    public static String mLang;

    /**
     * 初始化ble设置
     *
     * @param context
     */
    public static void initSetting ( Context context )
    {
        SharedPreferences defaultSet = PreferenceManager.getDefaultSharedPreferences( context );
        mBleEnabled = defaultSet.getBoolean( SET_BLE_ENABLED, false );
        mExitTurnOffBle = defaultSet.getBoolean( SET_EXIT_TURNOFF_BLE, false );
        mExitTip = defaultSet.getBoolean( SET_EXIT_TIP, true );
        mHasSelect = defaultSet.getBoolean( SET_COUNTRY_LANGUAGE_SELECTED, false );
        mCountry = defaultSet.getString( SET_COUNTRY, "" );
        mLang = defaultSet.getString( SET_LANGUAGE, LANGUAGE_AUTO );
    }

    public static void changeAppLanguage ( Context context )
    {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        if ( TextUtils.isEmpty( mLang ) )
        {
            mLang = LANGUAGE_AUTO;
        }
        switch ( mLang )
        {
            case LANGUAGE_AUTO:
                config.setLocale( Resources.getSystem()
                                           .getConfiguration().locale );
                break;
            case LANGUAGE_ENGLISH:
                config.setLocale( Locale.ENGLISH );
                break;
            case LANGUAGE_GERMANY:
                config.setLocale( Locale.GERMANY );
                break;
            case LANGUAGE_FRENCH:
                config.setLocale( Locale.FRENCH );
                break;
            case LANGUAGE_SPANISH:
                config.setLocale( new Locale( "es", "ES" ) );
                break;
            case LANGUAGE_CHINESE:
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
