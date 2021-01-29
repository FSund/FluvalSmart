package com.inledco.fluvalsmart.bean;

import java.io.Serializable;

/**
 * Created by liruya on 2016/10/28.
 */

public class Light extends BaseDevice implements Serializable
{
    private static final long serialVersionUID = -4162709866411397526L;

    public static final int MODE_MANUAL = 0;
    public static final int MODE_AUTO = 1;
    public static final int MODE_PRO = 2;

    private int mMode;
    private LightManual mLightManual;
    private LightAuto mLightAuto;
    private LightPro mLightPro;

    public Light(DevicePrefer devicePrefer) {
        super(devicePrefer);
    }

    public Light(DevicePrefer devicePrefer, int mode, LightManual lightManual, LightAuto lightAuto)
    {
        super(devicePrefer);
        mMode = mode;
        mLightManual = lightManual;
        mLightAuto = lightAuto;
    }

    public Light (DevicePrefer devicePrefer, int mode, LightManual lightManual, LightAuto lightAuto, LightPro lightPro)
    {
        super(devicePrefer);
        mMode = mode;
        mLightManual = lightManual;
        mLightAuto = lightAuto;
        mLightPro = lightPro;
    }

    public Light (byte majorVersion, byte minorVersion, DevicePrefer devicePrefer, DeviceTime deviceTime, int mode,
                   LightManual lightManual, LightAuto lightAuto)
    {
        super(majorVersion, minorVersion, devicePrefer, deviceTime);
        mMode = mode;
        mLightManual = lightManual;
        mLightAuto = lightAuto;
    }

    public int getMode()
    {
        return mMode;
    }

    public void setMode(int mode)
    {
        mMode = mode;
    }

    public LightPro getLightPro()
    {
        return mLightPro;
    }

    public void setLightPro(LightPro lightPro)
    {
        mLightPro = lightPro;
    }

    public LightManual getLightManual()
    {
        return mLightManual;
    }

    public void setLightManual (LightManual lightManual)
    {
        mLightManual = lightManual;
    }

    public LightAuto getLightAuto ()
    {
        return mLightAuto;
    }

    public void setLightAuto (LightAuto lightAuto)
    {
        mLightAuto = lightAuto;
    }
}
