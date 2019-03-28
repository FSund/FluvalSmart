package com.inledco.fluvalsmart.util;

import android.text.TextUtils;
import android.util.Log;

import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.bean.PointComparator;
import com.inledco.fluvalsmart.bean.RampTime;
import com.inledco.fluvalsmart.bean.TimerBrightPoint;
import com.liruya.tuner168blemanager.BleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 与硬件设备通信工具类
 * Created by liruya on 2016/8/26.
 */
public class CommUtil
{
    private static final String TAG = "CommUtil";

    private static final int MAX_SEND_BYTES = 17;
    public static final byte CHNL_RED = 0x01;
    public static final byte CHNL_GREEN = 0x02;
    public static final byte CHNL_BLUE = 0x04;
    public static final byte CHNL_WHITE = 0x08;
    public static final byte CHNL_ALL = 0x0F;

    private static final byte FRM_HDR = 0x68;

    private static final byte CMD_MODE = 0x02;
    private static final byte CMD_SWITCH = 0x03;
    private static final byte CMD_CTRL = 0x04;
    private static final byte CMD_READ = 0x05;
    private static final byte CMD_CUSTOM = 0x06;
    private static final byte CMD_CYCLE= 0x07;
    private static final byte CMD_CHN_INC = 0x08;
    private static final byte CMD_CHN_DEC = 0x09;
    private static final byte CMD_DYN = 0x0A;
    private static final byte CMD_PREVIEW = 0x0B;
    private static final byte CMD_STOP_PREVIEW = 0x0C;
    private static final byte CMD_READTIME = 0x0D;
    private static final byte CMD_SYNCTIME = 0x0E;
    private static final byte CMD_FIND = 0x0F;
    private static final byte CMD_PRO = 0x10;
    private static final byte CMD_DYNAMIC_PERIOD = 0x11;

    private static final byte MODE_MANUAL = 0x00;
    private static final byte MODE_AUTO = 0x01;
    private static final byte MODE_PRO = 0x02;
    private static final byte LED_OFF = 0x00;
    private static final byte LED_ON = 0x01;

    public static ArrayList<Byte> mRcvBytes = new ArrayList<>();

    /**
     * 计算异或校验值
     * @param array   数组
     * @param len     长度
     * @return        返回校验值
     */
    public static byte getCRC(byte[] array, int len)
    {
        byte crc = 0x00;
        for ( int i = 0; i < len; i++ )
        {
            crc ^= array[i];
        }
        return crc;
    }

    /**
     *
     * @param bytes
     * @param len
     * @return
     */
    public static byte getCRC(List<Byte> bytes, int len)
    {
        byte crc = 0x00;
        for ( int i = 0; i < len; i++ )
        {
            crc ^= bytes.get( i );
        }
        return crc;
    }

    /**
     * 打开LED
     * @param mac 地址
     */
    public static void turnOnLed(String mac)
    {
        BleManager.getInstance().sendBytes(mac, new byte[]{FRM_HDR, CMD_SWITCH, LED_ON, FRM_HDR ^ CMD_SWITCH ^ LED_ON});
    }

    /**
     * 关闭LED
     * @param mac 地址
     */
    public static void turnOffLed(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{FRM_HDR, CMD_SWITCH, LED_OFF, FRM_HDR^CMD_SWITCH^LED_OFF} );
    }


    public static void setModeAuto(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{ FRM_HDR, CMD_MODE, MODE_AUTO, FRM_HDR ^ CMD_MODE ^ MODE_AUTO} );
    }

    public static void setModeManual(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{ FRM_HDR, CMD_MODE, MODE_MANUAL, FRM_HDR ^ CMD_MODE ^ MODE_MANUAL} );
    }

    public static void setModePro(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{ FRM_HDR, CMD_MODE, MODE_PRO, FRM_HDR ^ CMD_MODE ^ MODE_PRO} );
    }

    /**
     * 设置LED运行参数
     * @param mac       地址
     * @param value     要设置的值
     */
    public static void setLed(String mac, short[] value)
    {
        //FRM_HDR CMD_CTRL value xor
        byte[] txs = new byte[3+value.length*2];
        txs[0] = FRM_HDR;
        txs[1] = CMD_CTRL;
        for ( int i = 0; i < value.length; i++ )
        {
            txs[2+2*i] = (byte) ( value[i] >> 8 );
            txs[3+2*i] = (byte) ( value[i] & 0xFF );
        }
        txs[txs.length-1] = getCRC( txs, txs.length-1);
        BleManager.getInstance().sendBytes( mac, txs );
    }

    /**
     * 快速预览自动模式
     * @param mac
     * @param value
     */
    public static void preview( String mac, short[] value )
    {
        //FRM_HDR CMD_CTRL chn max_8 xor
        byte[] txs = new byte[3+value.length*2];
        txs[0] = FRM_HDR;
        txs[1] = CMD_PREVIEW;
        for ( int i = 0; i < value.length; i++ )
        {
            txs[2+2*i] = (byte) ( value[i] >> 8 );
            txs[3+2*i] = (byte) ( value[i] & 0xFF );
        }
        txs[txs.length-1] = getCRC( txs, txs.length-1);
        BleManager.getInstance().sendBytes( mac, txs );
    }

    public static void stopPreview(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{FRM_HDR, CMD_STOP_PREVIEW, FRM_HDR^CMD_STOP_PREVIEW} );
    }

    /**
     * 设置led
     * @param mac
     * @param bytes
     */
    public static void setLed(String mac, byte[] bytes)
    {
        BleManager.getInstance().sendBytes( mac, bytes );
    }

    /**
     * 读取设备运行状态
     * @param mac 地址
     */
    public static void readDevice(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{FRM_HDR, CMD_READ, FRM_HDR^CMD_READ} );
    }

    public static void setLedCustom( String mac, byte idx )
    {
        byte[] txs = new byte[]{ FRM_HDR, CMD_CUSTOM, idx, (byte) ( FRM_HDR ^ CMD_CUSTOM ^ idx) };
        BleManager.getInstance().sendBytes( mac, txs );
    }

    public static void increaseBright( String mac, byte chn, byte delta )
    {
        byte[] txs = new byte[]{ FRM_HDR, CMD_CHN_INC, chn, delta, (byte) ( FRM_HDR ^ CMD_CHN_INC ^ chn ^ delta) };
        BleManager.getInstance().sendBytes( mac, txs );
    }

    public static void decreaseBright( String mac, byte chn, byte delta )
    {
        byte[] txs = new byte[]{ FRM_HDR, CMD_CHN_DEC, chn, delta, (byte) ( FRM_HDR ^ CMD_CHN_DEC ^ chn ^ delta) };
        BleManager.getInstance().sendBytes( mac, txs );
    }

    public static Object decodeLight ( List<Byte> bytes, short devid )
    {
        if ( bytes == null || bytes.size() == 0 )
        {
            return null;
        }
        LightManual lightManual;
        LightAuto lightAuto;
        LightPro lightPro;
        int chns = DeviceUtil.getChannelCount( devid );
        int len = bytes.size();
        if ( bytes.get( 0 ) == FRM_HDR && bytes.get( 1 ) == CMD_READ && getCRC( bytes, len ) == 0x00 )
        {
            byte mode = bytes.get( 2 );
            if ( mode == MODE_AUTO )
            {
                //frm_hdr cmd auto [sh sm eh sm] [chns] [sh sm eh em] [chns] xor
                if ( len == 2*chns+12 )                     //no night turnoff, no dynamic period
                {
                    RampTime sunrise = new RampTime( bytes.get( 3 ), bytes.get( 4 ),
                                                     bytes.get( 5 ), bytes.get( 6 ));
                    RampTime sunset = new RampTime( bytes.get( 7+chns ), bytes.get( 8+chns ),
                                                     bytes.get( 9+chns ), bytes.get( 10+chns ));
                    byte[] dbrt = new byte[chns];
                    byte[] nbrt = new byte[chns];
                    for ( int i = 0; i < chns; i++ )
                    {
                        dbrt[i] = bytes.get( 7+i );
                        nbrt[i] = bytes.get( 11+chns+i );
                    }
                    lightAuto = new LightAuto( sunrise, dbrt, sunset, nbrt );
                    return lightAuto;
                }
                else if(len == 2*chns+15)                   //night turnoff, no dynamic period
                {
                    RampTime sunrise = new RampTime( bytes.get( 3 ), bytes.get( 4 ),
                                                     bytes.get( 5 ), bytes.get( 6 ));
                    RampTime sunset = new RampTime( bytes.get( 7+chns ), bytes.get( 8+chns ),
                                                    bytes.get( 9+chns ), bytes.get( 10+chns ));
                    byte[] dbrt = new byte[chns];
                    byte[] nbrt = new byte[chns];
                    for ( int i = 0; i < chns; i++ )
                    {
                        dbrt[i] = bytes.get( 7+i );
                        nbrt[i] = bytes.get( 11+chns+i );
                    }
                    boolean turnoff_enable = (bytes.get(11 + 2 * chns) != 0);
                    byte hour = bytes.get( 12+2*chns );
                    byte minute = bytes.get( 13+2*chns );
                    lightAuto = new LightAuto( sunrise, dbrt, sunset, nbrt, turnoff_enable, hour, minute );
                    return lightAuto;
                }
                else if ( len == 2*chns+18 )                //dynamic period, no night turnoff
                {
                    RampTime sunrise = new RampTime( bytes.get( 3 ), bytes.get( 4 ),
                                                     bytes.get( 5 ), bytes.get( 6 ));
                    RampTime sunset = new RampTime( bytes.get( 7+chns ), bytes.get( 8+chns ),
                                                    bytes.get( 9+chns ), bytes.get( 10+chns ));
                    byte[] dbrt = new byte[chns];
                    byte[] nbrt = new byte[chns];
                    for ( int i = 0; i < chns; i++ )
                    {
                        dbrt[i] = bytes.get( 7+i );
                        nbrt[i] = bytes.get( 11+chns+i );
                    }
                    byte week = bytes.get( 11 + chns*2 );
                    RampTime dynamicPeriod = new RampTime( bytes.get( 12+chns*2 ), bytes.get( 13+chns*2 ),
                                                           bytes.get( 14+chns*2 ), bytes.get( 15+chns*2 ));
                    byte md = bytes.get( 16+chns*2 );
                    lightAuto = new LightAuto( sunrise, dbrt, sunset, nbrt, week, dynamicPeriod, md );
                    return lightAuto;
                }
                else if ( len == 2*chns+21 )                //night turnoff, dynamic period
                {
                    RampTime sunrise = new RampTime( bytes.get( 3 ), bytes.get( 4 ),
                                                     bytes.get( 5 ), bytes.get( 6 ));
                    RampTime sunset = new RampTime( bytes.get( 7+chns ), bytes.get( 8+chns ),
                                                    bytes.get( 9+chns ), bytes.get( 10+chns ));
                    byte[] dbrt = new byte[chns];
                    byte[] nbrt = new byte[chns];
                    for ( int i = 0; i < chns; i++ )
                    {
                        dbrt[i] = bytes.get( 7+i );
                        nbrt[i] = bytes.get( 11+chns+i );
                    }
                    boolean turnoff_enable = (bytes.get(11 + 2 * chns) != 0);
                    byte hour = bytes.get( 12+2*chns );
                    byte minute = bytes.get( 13+2*chns );
                    byte week = bytes.get( 14 + chns*2 );
                    RampTime dynamicPeriod = new RampTime( bytes.get( 15+chns*2 ), bytes.get( 16+chns*2 ),
                                                           bytes.get( 17+chns*2 ), bytes.get( 18+chns*2 ));
                    byte md = bytes.get( 19+chns*2 );
                    lightAuto = new LightAuto( sunrise, dbrt, sunset, nbrt, turnoff_enable, hour, minute, week, dynamicPeriod, md );
                    return lightAuto;
                }
            }
            else if ( mode == MODE_PRO )
            {
                int count = bytes.get( 3 );
                if ( len == count*(2+chns)+5 || len == count*(2+chns)+11 )
                {
                    byte[] array = new byte[len-4];
                    for ( int i = 0; i < array.length; i++ )
                    {
                        array[i] = bytes.get( i+3 );
                    }
                    lightPro = new LightPro.Builder().creatFromArray( array, chns );
                    PointComparator comparator = new PointComparator();
                    Arrays.sort( lightPro.getPoints(), 0, lightPro.getPointCount(), comparator );
                    return lightPro;
                }
            }
            else if ( mode == MODE_MANUAL )
            {
                if ( len == 6*chns+6 )
                {
                    boolean on = ( (bytes.get( 3 ) & 0x01) != 0x00 );
                    byte dyn = bytes.get( 4 );
                    short[] chnValues = new short[chns];
                    byte[] p1Values = new byte[chns];
                    byte[] p2Values = new byte[chns];
                    byte[] p3Values = new byte[chns];
                    byte[] p4Values = new byte[chns];
                    for ( int i = 0; i < chns; i++ )
                    {
                        chnValues[i] = (short) ( ( ( bytes.get( 6 + 2 * i ) & 0xFF) << 8) | ( bytes.get( 5 + 2 * i ) & 0xFF ) );
                        p1Values[i] = bytes.get( 5+2*chns+i );
                        p2Values[i] = bytes.get( 5+3*chns+i );
                        p3Values[i] = bytes.get( 5+4*chns+i );
                        p4Values[i] = bytes.get( 5+5*chns+i );
                    }
                    lightManual = new LightManual( on, dyn, chnValues, p1Values, p2Values, p3Values, p4Values );
                    return lightManual;
                }
            }
        }
        return null;
    }

    public static void sendKey( String mac, byte key )
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{ FRM_HDR, CMD_DYN, key, (byte) ( FRM_HDR ^ CMD_DYN ^ key) } );
    }

    public static void findDevice(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{FRM_HDR, CMD_FIND, FRM_HDR^CMD_FIND} );
    }

    public static void syncDeviceTime(String mac)
    {
        Calendar calendar = Calendar.getInstance();
        byte year = (byte) (calendar.get( Calendar.YEAR ) - 2000);
        byte month = (byte) calendar.get( Calendar.MONTH );
        byte day = (byte) calendar.get( Calendar.DAY_OF_MONTH );
        byte wk = (byte) (calendar.get( Calendar.DAY_OF_WEEK ) - 1);
        byte hour = (byte) calendar.get( Calendar.HOUR_OF_DAY );
        byte minute = (byte) calendar.get( Calendar.MINUTE );
        byte second = (byte) calendar.get( Calendar.SECOND );
        byte[] ct = new byte[]{ FRM_HDR,
                                CMD_SYNCTIME,
                                year,
                                month,
                                day,
                                wk,
                                hour,
                                minute,
                                second,
                                (byte) ( FRM_HDR ^ CMD_SYNCTIME ^ year ^ month ^ day ^ wk ^ hour ^ minute ^ second) };
        BleManager.getInstance().sendBytes( mac, ct );
    }

    public static void readDeviceTime(String mac)
    {
        BleManager.getInstance().sendBytes( mac, new byte[]{FRM_HDR, CMD_READTIME, FRM_HDR^CMD_READTIME} );
    }

    public static void setLedAuto (String mac, LightAuto lightAuto )
    {
        if ( TextUtils.isEmpty( mac ) || lightAuto == null )
        {
            return;
        }
        int dlen = lightAuto.getDayBright().length;
        int nlen = lightAuto.getNightBright().length;
        int len = 11 + dlen + nlen;
        if ( !lightAuto.isHasDynamic() )
        {
            if ( lightAuto.isHasTurnoff() )
            {
                len += 3;
            }
        }
        else
        {
            if ( !lightAuto.isHasTurnoff() )
            {
                len += 6;
            }
            else
            {
                len += 3;
            }
        }
        byte[] datas = new byte[len];
        RampTime sunrise = lightAuto.getSunrise();
        RampTime sunset = lightAuto.getSunset();
        datas[0] = FRM_HDR;
        datas[1] = CMD_CYCLE;
        datas[2] = sunrise.getStartHour();
        datas[3] = sunrise.getStartMinute();
        datas[4] = sunrise.getEndHour();
        datas[5] = sunrise.getEndMinute();
        datas[6+dlen] = sunset.getStartHour();
        datas[7+dlen] = sunset.getStartMinute();
        datas[8+dlen] = sunset.getEndHour();
        datas[9+dlen] = sunset.getEndMinute();
        for ( int i = 0; i < dlen; i++ )
        {
            datas[6+i] = lightAuto.getDayBright()[i];
        }
        for ( int i = 0; i < nlen; i++ )
        {
            datas[10+dlen+i] = lightAuto.getNightBright()[i];
        }
        Log.e(TAG, "setLedAuto: " + lightAuto.isHasDynamic() );
        if ( !lightAuto.isHasDynamic() )
        {
            if ( lightAuto.isHasTurnoff() )
            {
                datas[10+dlen+nlen] = (byte) ( lightAuto.isTurnoffEnable() ? 0x01 : 0x00);
                datas[11+dlen+nlen] = lightAuto.getTurnoffHour();
                datas[12+dlen+nlen] = lightAuto.getTurnoffMinute();
//                len += 3;
            }
        }
        else
        {
            if ( !lightAuto.isHasTurnoff() )
            {
                datas[10 + dlen + nlen] = lightAuto.getWeek();
                datas[11 + dlen + nlen] = lightAuto.getDynamicPeriod()
                                                   .getStartHour();
                datas[12 + dlen + nlen] = lightAuto.getDynamicPeriod()
                                                   .getStartMinute();
                datas[13 + dlen + nlen] = lightAuto.getDynamicPeriod()
                                                   .getEndHour();
                datas[14 + dlen + nlen] = lightAuto.getDynamicPeriod()
                                                   .getEndMinute();
                datas[15 + dlen + nlen] = lightAuto.getDynamicMode();
                Log.e(TAG, "setLedAuto: " + datas[15+dlen+nlen] );
//                len += 6;
            }
            else
            {
                datas[10+dlen+nlen] = (byte) ( lightAuto.isTurnoffEnable() ? 0x01 : 0x00);
                datas[11+dlen+nlen] = lightAuto.getTurnoffHour();
                datas[12+dlen+nlen] = lightAuto.getTurnoffMinute();
//                len += 3;
            }
        }
        datas[len-1] = getCRC( datas, len-1 );
        BleManager.getInstance().sendBytes( mac, datas );
    }

    public static void setLedPro( String mac, LightPro lightPro)
    {
        if ( TextUtils.isEmpty( mac ) || lightPro == null )
        {
            return;
        }
        byte[] array = lightPro.toArray();
        int len = lightPro.isHasDynamic() ? array.length-6 : array.length;
        byte[] datas = new byte[len+3];
        datas[0] = FRM_HDR;
        datas[1] = CMD_PRO;
        System.arraycopy( array, 0, datas, 2, len );
        datas[datas.length-1] = getCRC( datas, datas.length-1 );
        BleManager.getInstance().sendBytes( mac, datas );
    }

    public static void setLedPro( String mac, List<TimerBrightPoint> points )
    {
        if ( TextUtils.isEmpty( mac ) || points == null || points.size() < LightPro.POINT_COUNT_MIN || points.size() > LightPro.POINT_COUNT_MAX )
        {
            return;
        }
        int chns = points.get( 0 ).getBrights().length;
        int len = points.size()*(chns+2)+4;
        byte[] array = new byte[len];
        array[0] = FRM_HDR;
        array[1] = CMD_PRO;
        array[2] = (byte) points.size();
        for ( int i = 0; i < points.size(); i++ )
        {
            if ( points.get( i ).getBrights().length != chns )
            {
                return;
            }
            array[3+i*(chns+2)] = (byte) points.get( i ).getHour();
            array[4+i*(chns+2)] = (byte) points.get( i ).getMinute();
            for ( int j = 0; j < chns; j++ )
            {
                array[5+i*(chns+2)+j] = points.get( i ).getBrights()[j];
            }
        }
        array[len-1] = getCRC( array, len-1 );
        BleManager.getInstance().sendBytes( mac, array );
    }

    public static void setLedDynamicPeriod(String mac, byte week, RampTime period, byte mode)
    {
        byte[] datas = new byte[9];
        datas[0] = FRM_HDR;
        datas[1] = CMD_DYNAMIC_PERIOD;
        datas[2] = week;
        datas[3] = period.getStartHour();
        datas[4] = period.getStartMinute();
        datas[5] = period.getEndHour();
        datas[6] = period.getEndMinute();
        datas[7] = mode;
        datas[8] = getCRC( datas, 8 );
        BleManager.getInstance().sendBytes( mac, datas );
    }
}
