package com.inledco.fluvalsmart.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.bean.RampTime;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by liruya on 2016/11/28.
 */

public class LightProfileUtil
{
    private static final String TAG = "LightProfileUtil";

    private static final String LIGHT_AUTO_PROFILE_FILENAME = "profile_";
    private static final String LIGHT_PRO_PROFILE_FILENAME = "pro_profile_";
    private static final RampTime DEFAULT_SUNRISE = new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 );
    private static final RampTime DEFAULT_SUNSET = new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 );

//    public static LightAuto getDefaultProfile( short devid )
//    {
//        LightAuto lightAuto = new LightAuto( DEFAULT_SUNRISE,
//                                             DeviceUtil.getDayBright( devid ),
//                                             DEFAULT_SUNSET,
//                                             DeviceUtil.getNightBright( devid ));
//        return lightAuto;
//    }

    public static void saveAutoProfile( Context context, LightAuto lightAuto, short devid, String name )
    {
        if ( context == null || lightAuto == null || TextUtils.isEmpty( name ) )
        {
            return;
        }
        PreferenceUtil.setObjectToPrefer( context, LIGHT_AUTO_PROFILE_FILENAME + devid, lightAuto, name );
    }

    public static void deleteAutoProfile( Context context, short devid, String name )
    {
        if ( context == null || TextUtils.isEmpty( name ) )
        {
            return;
        }
        PreferenceUtil.deleteObjectFromPrefer( context, LIGHT_AUTO_PROFILE_FILENAME + devid, name );
    }

    public static Map<String, LightAuto> getLocalAutoProfiles( Context context, short devid, boolean hasAutoDynamic, boolean hasTurnoff )
    {
        if ( context == null )
        {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences( LIGHT_AUTO_PROFILE_FILENAME + devid,
                                                             Context.MODE_PRIVATE );
        Map<String, LightAuto> map = DeviceUtil.getAutoPresetProfiles( context, devid, hasAutoDynamic, hasTurnoff );
//        if ( map == null )
//        {
//            map = new HashMap<>();
//        }
//        map.put( context.getResources().getString( R.string.custom_default ), getDefaultProfile( devid ) );
        for ( String key : sp.getAll().keySet() )
        {
            map.put( key, (LightAuto) PreferenceUtil.getObjectFromPrefer( context, LIGHT_AUTO_PROFILE_FILENAME + devid, key ) );
        }
        return map;
    }

    public static String getAutoProfileName( Context context, short devid, LightAuto a )
    {
        if ( a == null )
        {
            return "";
        }
        Map< String, LightAuto > profiles = getLocalAutoProfiles( context, devid, a.isHasDynamic(), a.isHasTurnoff() );
        if ( profiles == null || profiles.size() == 0 )
        {
            return "";
        }
        for ( String key : profiles.keySet() )
        {
            if ( a.equal( profiles.get( key ) ) )
            {
                return key;
            }
        }
        return "";
    }

    public static void saveProProfile( Context context, LightPro lightPro, short devid, String name)
    {
        if ( context == null || lightPro == null || TextUtils.isEmpty( name ) )
        {
            return;
        }
        PreferenceUtil.saveByteArray( context, LIGHT_PRO_PROFILE_FILENAME + devid, lightPro.toArray(), name );
    }

    public static void deleteProProfile(Context context, short devid, String name)
    {
        if ( context == null || TextUtils.isEmpty( name ) )
        {
            return;
        }
        PreferenceUtil.deleteObjectFromPrefer( context, LIGHT_PRO_PROFILE_FILENAME + devid, name );
    }

    public static Map<String, LightPro> getLocalProProfiles(Context context, short devid, boolean hasDynamic )
    {
        if ( context == null )
        {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences( LIGHT_PRO_PROFILE_FILENAME + devid,
                                                             Context.MODE_PRIVATE );
        Map<String, LightPro> map = DeviceUtil.getProPresetProfiles( context, devid, hasDynamic );
        LightPro.Builder builder = new LightPro.Builder();
        for ( String key : sp.getAll().keySet() )
        {
            byte[] array = PreferenceUtil.readByteArray( context, LIGHT_PRO_PROFILE_FILENAME + devid, key );
            LightPro lightPro = builder.creatFromArray( array, DeviceUtil.getChannelCount( devid ) );
            map.put( key, lightPro );
        }
        return map;
    }

    public static String getProProfileName( Context context, short devid, LightPro p )
    {
        if ( p == null )
        {
            return "";
        }
        Map< String, LightPro > profiles = getLocalProProfiles( context, devid, p.isHasDynamic() );
        if ( profiles == null || profiles.size() == 0 )
        {
            return "";
        }
        byte[] array = p.toArray();
        for ( String key : profiles.keySet() )
        {
            LightPro lp = profiles.get( key );
            if ( lp == null )
            {
                continue;
            }
            if ( Arrays.equals( array, lp.toArray() ) )
            {
                return key;
            }
        }
        return "";
    }
}
