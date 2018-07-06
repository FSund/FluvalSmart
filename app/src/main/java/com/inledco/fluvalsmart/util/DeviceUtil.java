package com.inledco.fluvalsmart.util;

import android.content.Context;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.RampTime;
import com.inledco.fluvalsmart.constant.CustomColor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/21.
 */

public class DeviceUtil
{
    //灯具类别--0x**** 前两位表示设备类型,如灯,插座,第三位表示设备子类型,如marine,fresh,第四位表示该设备的不同规格 如长度500,800,1100

    /* Test */
    public static final short LIGHT_ID_RGBW = 0x0105;
    public static final short LIGHT_ID_STRIP_III = 0x0111;
    public static final short LIGHT_ID_EGG = 0x0115;

    /* Marine & Ref */
    public static final short LIGHT_ID_MARINE_500 = 0x0121;
    public static final short LIGHT_ID_MARINE_800 = 0x0122;
    public static final short LIGHT_ID_MARINE_1100 = 0x0123;
    public static final short LIGHT_ID_MARINE_1000 = 0x0124;

    /* Plant & Fresh */
    public static final short LIGHT_ID_FRESH_500 = 0x0131;
    public static final short LIGHT_ID_FRESH_800 = 0x0132;
    public static final short LIGHT_ID_FRESH_1100 = 0x0133;
    public static final short LIGHT_ID_FRESH_1000 = 0x0134;

    /* Aquasky */
    public static final short LIGHT_ID_AQUASKY_600 = 0x0141;
    public static final short LIGHT_ID_AQUASKY_900 = 0x0142;
    public static final short LIGHT_ID_AQUASKY_1200 = 0x0143;
    public static final short LIGHT_ID_AQUASKY_380 = 0x0144;
    public static final short LIGHT_ID_AQUASKY_530 = 0x0145;
    public static final short LIGHT_ID_AQUASKY_835 = 0x0146;
    public static final short LIGHT_ID_AQUASKY_990 = 0x0147;
    public static final short LIGHT_ID_AQUASKY_750 = 0x0148;
    public static final short LIGHT_ID_AQUASKY_1150 = 0x0149;
    public static final short LIGHT_ID_AQUASKY_910 = 0x0150;

    /* Nano */
    public static final short LIGHT_ID_NANO_MARINE = 0x0151;
    public static final short LIGHT_ID_NANO_FRESH = 0x0152;

    public static final String LIGHT_TYPE_RGBW = "RGBW Strip II";
    public static final String LIGHT_TYPE_STRIP_III = "Hagen Strip III";
    public static final String LIGHT_TYPE_EGG = "Egg Light";
    public static final String LIGHT_TYPE_MARINE = "Marine & Reef";
    public static final String LIGHT_TYPE_FRESH = "Fresh & Plant";
    public static final String LIGHT_TYPE_AQUASKY = "Aquasky";
    public static final String LIGHT_TYPE_NANO_MARINE = "Wing Nano Marine";
    public static final String LIGHT_TYPE_NANO_FRESH = "Wing Nano Fresh";

    private static Map< Short, String > mDeviceMap;
    private static Map< Short, Integer > mIconMap;

    static
    {
        mDeviceMap = new HashMap<>();
        mIconMap = new HashMap<>();
        /* Test */
        mDeviceMap.put( LIGHT_ID_RGBW, LIGHT_TYPE_RGBW );
        mDeviceMap.put( LIGHT_ID_STRIP_III, LIGHT_TYPE_STRIP_III );
        mDeviceMap.put( LIGHT_ID_EGG, LIGHT_TYPE_EGG );
        /* Marine & Ref */
        mDeviceMap.put( LIGHT_ID_MARINE_500, LIGHT_TYPE_MARINE + " 500mm" );
        mDeviceMap.put( LIGHT_ID_MARINE_800, LIGHT_TYPE_MARINE + " 800mm" );
        mDeviceMap.put( LIGHT_ID_MARINE_1100, LIGHT_TYPE_MARINE + " 1100mm" );
        mDeviceMap.put( LIGHT_ID_MARINE_1000, LIGHT_TYPE_MARINE + " 1000mm" );
        /* Plant & Fresh */
        mDeviceMap.put( LIGHT_ID_FRESH_500, LIGHT_TYPE_FRESH + " 500mm" );
        mDeviceMap.put( LIGHT_ID_FRESH_800, LIGHT_TYPE_FRESH + " 800mm" );
        mDeviceMap.put( LIGHT_ID_FRESH_1100, LIGHT_TYPE_FRESH + " 1100mm" );
        mDeviceMap.put( LIGHT_ID_FRESH_1000, LIGHT_TYPE_FRESH + " 1000mm" );
        /* Aquasky */
        mDeviceMap.put( LIGHT_ID_AQUASKY_600, LIGHT_TYPE_AQUASKY + " 600mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_900, LIGHT_TYPE_AQUASKY + " 900mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_1200, LIGHT_TYPE_AQUASKY + " 1200mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_380, LIGHT_TYPE_AQUASKY + " 380mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_530, LIGHT_TYPE_AQUASKY + " 530mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_835, LIGHT_TYPE_AQUASKY + " 835mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_990, LIGHT_TYPE_AQUASKY + " 990mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_750, LIGHT_TYPE_AQUASKY + " 750mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_1150, LIGHT_TYPE_AQUASKY + " 1150mm" );
        mDeviceMap.put( LIGHT_ID_AQUASKY_910, LIGHT_TYPE_AQUASKY + " 910mm" );
        /* Nano */
        mDeviceMap.put( LIGHT_ID_NANO_MARINE, LIGHT_TYPE_NANO_MARINE );
        mDeviceMap.put( LIGHT_ID_NANO_FRESH, LIGHT_TYPE_NANO_FRESH );

        /* Test */
        mIconMap.put( LIGHT_ID_RGBW, R.mipmap.ic_light_rgbw_ii );
        mIconMap.put( LIGHT_ID_STRIP_III, R.mipmap.ic_light_strip_iii );
        mIconMap.put( LIGHT_ID_EGG, R.mipmap.ic_light_egg );
        /* Marine & Ref */
        mIconMap.put( LIGHT_ID_MARINE_500, R.mipmap.ic_light_marine );
        mIconMap.put( LIGHT_ID_MARINE_800, R.mipmap.ic_light_marine );
        mIconMap.put( LIGHT_ID_MARINE_1100, R.mipmap.ic_light_marine );
        mIconMap.put( LIGHT_ID_MARINE_1000, R.mipmap.ic_light_marine );
        /* Plant & Fresh */
        mIconMap.put( LIGHT_ID_FRESH_500, R.mipmap.ic_light_fresh );
        mIconMap.put( LIGHT_ID_FRESH_800, R.mipmap.ic_light_fresh );
        mIconMap.put( LIGHT_ID_FRESH_1100, R.mipmap.ic_light_fresh );
        mIconMap.put( LIGHT_ID_FRESH_1000, R.mipmap.ic_light_fresh );
        /* Aquasky */
        mIconMap.put( LIGHT_ID_AQUASKY_600, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_900, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_1200, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_380, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_530, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_835, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_990, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_750, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_1150, R.mipmap.ic_light_aquasky );
        mIconMap.put( LIGHT_ID_AQUASKY_910, R.mipmap.ic_light_aquasky );
        /* Nano */
        mIconMap.put( LIGHT_ID_NANO_MARINE, R.mipmap.ic_light_nano_marine );
        mIconMap.put( LIGHT_ID_NANO_FRESH, R.mipmap.ic_light_nano_fresh );
    }

    public static boolean isCorrectDevType( short id )
    {
        return mDeviceMap.containsKey( id );
    }

    /**
     * 由设备id获取设备类型
     *
     * @param devid
     * @return
     */
    public static String getDeviceType( short devid )
    {
        if ( mDeviceMap.containsKey( devid ) )
        {
            return mDeviceMap.get( devid );
        }
        return "Unkown device";
    }

    public static int getDeviceIcon( short devid )
    {
        int resid = R.drawable.ic_bluetooth_white_48dp;
        if ( mIconMap == null )
        {
            return resid;
        }
        if ( mIconMap.containsKey( devid ) )
        {
            return mIconMap.get( devid );
        }
        return resid;
    }

    public static Channel[] getLightChannel( Context context, short devid )
    {
        Channel[] channels = null;
        switch ( devid )
        {
            case LIGHT_ID_RGBW:
            case LIGHT_ID_STRIP_III:
                channels = new Channel[]{ new Channel( context.getString( R.string.chn_name_red ), CustomColor.COLOR_RED_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_green ), CustomColor.COLOR_GREEN_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_blue ), CustomColor.COLOR_BLUE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_white ), CustomColor.COLOR_WHITE_COLD, (short) 0 ) };
                break;

            case LIGHT_ID_EGG:
                channels = new Channel[]{ new Channel( context.getString( R.string.chn_name_red ), CustomColor.COLOR_RED_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_green ), CustomColor.COLOR_GREEN_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_blue ), CustomColor.COLOR_BLUE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_coldwhite ), CustomColor.COLOR_WHITE_COLD, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_Warmwhite ), CustomColor.COLOR_WHITE_WARM, (short) 0 ) };
                break;

            case LIGHT_ID_MARINE_500:
            case LIGHT_ID_MARINE_800:
            case LIGHT_ID_MARINE_1100:
            case LIGHT_ID_MARINE_1000:
            case LIGHT_ID_NANO_MARINE:
                channels = new Channel[]{ new Channel( context.getString( R.string.chn_name_pink ), CustomColor.COLOR_PINK_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_cyan ), CustomColor.COLOR_CYAN_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_blue ), CustomColor.COLOR_BLUE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_purple ), CustomColor.COLOR_PURPLE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_coldwhite ), CustomColor.COLOR_WHITE_COLD, (short) 0 ), };
                break;

            case LIGHT_ID_FRESH_500:
            case LIGHT_ID_FRESH_800:
            case LIGHT_ID_FRESH_1100:
            case LIGHT_ID_FRESH_1000:
            case LIGHT_ID_NANO_FRESH:
                channels = new Channel[]{ new Channel( context.getString( R.string.chn_name_pink ), CustomColor.COLOR_PINK_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_blue ), CustomColor.COLOR_BLUE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_coldwhite ), CustomColor.COLOR_WHITE_COLD, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_purewhite ), CustomColor.COLOR_WHITE_PURE, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_Warmwhite ), CustomColor.COLOR_WHITE_WARM, (short) 0 ) };
                break;

            case LIGHT_ID_AQUASKY_600:
            case LIGHT_ID_AQUASKY_900:
            case LIGHT_ID_AQUASKY_1200:
            case LIGHT_ID_AQUASKY_380:
            case LIGHT_ID_AQUASKY_530:
            case LIGHT_ID_AQUASKY_835:
            case LIGHT_ID_AQUASKY_990:
            case LIGHT_ID_AQUASKY_750:
            case LIGHT_ID_AQUASKY_1150:
            case LIGHT_ID_AQUASKY_910:
                channels = new Channel[]{ new Channel( context.getString( R.string.chn_name_red ), CustomColor.COLOR_RED_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_green ), CustomColor.COLOR_GREEN_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_blue ), CustomColor.COLOR_BLUE_A700, (short) 0 ),
                                          new Channel( context.getString( R.string.chn_name_white ), CustomColor.COLOR_WHITE_PURE, (short) 0 ) };
                break;
        }
        return channels;
    }

    public static int[] getThumb( short devid )
    {
        int[] thumbs = null;
        switch ( devid )
        {
            case LIGHT_ID_RGBW:
            case LIGHT_ID_STRIP_III:
                thumbs = new int[]{ R.drawable.shape_thumb_red, R.drawable.shape_thumb_green, R.drawable.shape_thumb_blue, R.drawable.shape_thumb_coldwhite };
                break;
            case LIGHT_ID_EGG:
                thumbs = new int[]{ R.drawable.shape_thumb_red,
                                    R.drawable.shape_thumb_green,
                                    R.drawable.shape_thumb_blue,
                                    R.drawable.shape_thumb_coldwhite,
                                    R.drawable.shape_thumb_warmwhite };
                break;
            case LIGHT_ID_MARINE_500:
            case LIGHT_ID_MARINE_800:
            case LIGHT_ID_MARINE_1100:
            case LIGHT_ID_MARINE_1000:
            case LIGHT_ID_NANO_MARINE:
                thumbs = new int[]{ R.drawable.shape_thumb_pink,
                                    R.drawable.shape_thumb_cyan,
                                    R.drawable.shape_thumb_blue,
                                    R.drawable.shape_thumb_purple,
                                    R.drawable.shape_thumb_coldwhite };
                break;
            case LIGHT_ID_FRESH_500:
            case LIGHT_ID_FRESH_800:
            case LIGHT_ID_FRESH_1100:
            case LIGHT_ID_FRESH_1000:
            case LIGHT_ID_NANO_FRESH:
                thumbs = new int[]{ R.drawable.shape_thumb_pink,
                                    R.drawable.shape_thumb_blue,
                                    R.drawable.shape_thumb_coldwhite,
                                    R.drawable.shape_thumb_purewhite,
                                    R.drawable.shape_thumb_warmwhite, };
                break;
            case LIGHT_ID_AQUASKY_600:
            case LIGHT_ID_AQUASKY_900:
            case LIGHT_ID_AQUASKY_1200:
            case LIGHT_ID_AQUASKY_380:
            case LIGHT_ID_AQUASKY_530:
            case LIGHT_ID_AQUASKY_835:
            case LIGHT_ID_AQUASKY_990:
            case LIGHT_ID_AQUASKY_750:
            case LIGHT_ID_AQUASKY_1150:
            case LIGHT_ID_AQUASKY_910:
                thumbs = new int[]{ R.drawable.shape_thumb_red, R.drawable.shape_thumb_green, R.drawable.shape_thumb_blue, R.drawable.shape_thumb_purewhite };
                break;
        }
        return thumbs;
    }

    public static int[] getSeekbar( short devid )
    {
        int[] seekBars = null;
        switch ( devid )
        {
            case LIGHT_ID_RGBW:
            case LIGHT_ID_STRIP_III:
                seekBars = new int[]{ R.drawable.custom_seekbar_red,
                                      R.drawable.custom_seekbar_green,
                                      R.drawable.custom_seekbar_blue,
                                      R.drawable.custom_seekbar_coldwhite };
                break;
            case LIGHT_ID_EGG:
                seekBars = new int[]{ R.drawable.custom_seekbar_red,
                                      R.drawable.custom_seekbar_green,
                                      R.drawable.custom_seekbar_blue,
                                      R.drawable.custom_seekbar_coldwhite,
                                      R.drawable.custom_seekbar_warmwhite };
                break;
            case LIGHT_ID_MARINE_500:
            case LIGHT_ID_MARINE_800:
            case LIGHT_ID_MARINE_1100:
            case LIGHT_ID_MARINE_1000:
            case LIGHT_ID_NANO_MARINE:
                seekBars = new int[]{ R.drawable.custom_seekbar_pink,
                                      R.drawable.custom_seekbar_cyan,
                                      R.drawable.custom_seekbar_blue,
                                      R.drawable.custom_seekbar_purple,
                                      R.drawable.custom_seekbar_coldwhite };
                break;
            case LIGHT_ID_FRESH_500:
            case LIGHT_ID_FRESH_800:
            case LIGHT_ID_FRESH_1100:
            case LIGHT_ID_FRESH_1000:
            case LIGHT_ID_NANO_FRESH:
                seekBars = new int[]{ R.drawable.custom_seekbar_pink,
                                      R.drawable.custom_seekbar_blue,
                                      R.drawable.custom_seekbar_coldwhite,
                                      R.drawable.custom_seekbar_purewhite,
                                      R.drawable.custom_seekbar_warmwhite };
                break;
            case LIGHT_ID_AQUASKY_600:
            case LIGHT_ID_AQUASKY_900:
            case LIGHT_ID_AQUASKY_1200:
            case LIGHT_ID_AQUASKY_380:
            case LIGHT_ID_AQUASKY_530:
            case LIGHT_ID_AQUASKY_835:
            case LIGHT_ID_AQUASKY_990:
            case LIGHT_ID_AQUASKY_750:
            case LIGHT_ID_AQUASKY_1150:
            case LIGHT_ID_AQUASKY_910:
                seekBars = new int[]{ R.drawable.custom_seekbar_red,
                                      R.drawable.custom_seekbar_green,
                                      R.drawable.custom_seekbar_blue,
                                      R.drawable.custom_seekbar_purewhite };
                break;
        }
        return seekBars;
    }

    public static Map< String, LightAuto > getPresetProfiles( Context context, short devid, boolean hasAutoDynamic )
    {
        Map< String, LightAuto > profiles = new LinkedHashMap<>();
        switch ( devid )
        {
            case LIGHT_ID_MARINE_500:
            case LIGHT_ID_MARINE_800:
            case LIGHT_ID_MARINE_1100:
            case LIGHT_ID_MARINE_1000:
            case LIGHT_ID_NANO_MARINE:
                profiles.put( context.getString( R.string.preset_deep_sea_glo ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 16, 100, 100, 100, 0 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 0, 5, 0, 0 } ) );
                profiles.put( context.getString( R.string.preset_sunny_reef ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 100, 100, 100, 100, 100 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 0, 5, 0, 0 } ) );
                profiles.put( context.getString( R.string.preset_color_boost ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 68, 100, 100, 85, 90 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 0, 5, 0, 0 } ) );
                break;
            case LIGHT_ID_FRESH_500:
            case LIGHT_ID_FRESH_800:
            case LIGHT_ID_FRESH_1100:
            case LIGHT_ID_FRESH_1000:
            case LIGHT_ID_NANO_FRESH:
                profiles.put( context.getString( R.string.preset_tropic_river ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 80, 0, 37, 100, 100 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 5, 0, 0, 0 } ) );
                profiles.put( context.getString( R.string.preset_lake_malawi ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 50, 0, 37, 100, 0 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 5, 0, 0, 0 } ) );
                profiles.put( context.getString( R.string.preset_planted ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 84, 20, 73, 100, 80 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 5, 0, 0, 0 } ) );
                break;
            case LIGHT_ID_RGBW:
            case LIGHT_ID_STRIP_III:
            case LIGHT_ID_AQUASKY_600:
            case LIGHT_ID_AQUASKY_900:
            case LIGHT_ID_AQUASKY_1200:
            case LIGHT_ID_AQUASKY_380:
            case LIGHT_ID_AQUASKY_530:
            case LIGHT_ID_AQUASKY_835:
            case LIGHT_ID_AQUASKY_990:
            case LIGHT_ID_AQUASKY_750:
            case LIGHT_ID_AQUASKY_1150:
            case LIGHT_ID_AQUASKY_910:
                profiles.put( context.getString( R.string.preset_color_boost ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 68, 100, 100, 90 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 0, 5, 0 } ) );
                profiles.put( context.getString( R.string.preset_plant_boost ),
                              new LightAuto( new RampTime( (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00 ),
                                             new byte[]{ 100, 100, 100, 100 },
                                             new RampTime( (byte) 0x11, (byte) 0x00, (byte) 0x12, (byte) 0x00 ),
                                             new byte[]{ 0, 0, 5, 0 } ) );
                break;
        }
        if ( hasAutoDynamic )
        {
            for ( String key : profiles.keySet() )
            {
                profiles.get( key )
                        .setHasDynamic( true );
                profiles.get( key )
                        .setSun( false );
                profiles.get( key )
                        .setMon( false );
                profiles.get( key )
                        .setTue( false );
                profiles.get( key )
                        .setWed( false );
                profiles.get( key )
                        .setThu( false );
                profiles.get( key )
                        .setFri( false );
                profiles.get( key )
                        .setSat( false );
                profiles.get( key )
                        .setDynamicEnable( false );
                profiles.get( key )
                        .setDynamicPeriod( new RampTime( (byte) 0, (byte) 0, (byte) 0, (byte) 0 ) );
                profiles.get( key )
                        .setDynamicMode( (byte) 0 );
            }
        }
        return profiles;
    }

    public static int getChannelCount( short devid )
    {
        int chns = 0;
        switch ( devid )
        {
            case LIGHT_ID_RGBW:
            case LIGHT_ID_STRIP_III:
            case LIGHT_ID_AQUASKY_600:
            case LIGHT_ID_AQUASKY_900:
            case LIGHT_ID_AQUASKY_1200:
            case LIGHT_ID_AQUASKY_380:
            case LIGHT_ID_AQUASKY_530:
            case LIGHT_ID_AQUASKY_835:
            case LIGHT_ID_AQUASKY_990:
            case LIGHT_ID_AQUASKY_750:
            case LIGHT_ID_AQUASKY_1150:
            case LIGHT_ID_AQUASKY_910:
                chns = 4;
                break;
            case LIGHT_ID_EGG:
            case LIGHT_ID_MARINE_500:
            case LIGHT_ID_MARINE_800:
            case LIGHT_ID_MARINE_1100:
            case LIGHT_ID_MARINE_1000:
            case LIGHT_ID_FRESH_500:
            case LIGHT_ID_FRESH_800:
            case LIGHT_ID_FRESH_1100:
            case LIGHT_ID_FRESH_1000:
            case LIGHT_ID_NANO_MARINE:
            case LIGHT_ID_NANO_FRESH:
                chns = 5;
                break;
        }
        return chns;
    }
}