package com.inledco.fluvalsmart.bean;

/**
 * Created by Administrator on 2016/10/21.
 */

public class MessageEvent
{
    public static final int MSG_EVT_BLE_INIT = 0x01;

    public static final int MSG_EVT_BLE_CONNECTED = 0x11;
    public static final int MSG_EVT_BLE_DISCONNECTED = 0x12;
    public static final int MSG_EVT_BLE_CONNECT_TIMEOUT = 0x13;
    public static final int MSG_EVT_BLE_CONNECTION_ERROR = 0x14;
    public static final int MSG_EVT_BLE_SERVICES_DISCOVERED = 0x15;
    public static final int MSG_EVT_BLE_SERVICES_UNDISCOVERED = 0x16;
    public static final int MSG_EVT_BLE_REG_READ = 0x17;
    public static final int MSG_EVT_BLE_RECEIVE = 0x18;
    public static final int MSG_EVT_BLE_SEND = 0x19;

    private int msg;
    private String mac;

    public MessageEvent ( int msg )
    {
        this.msg = msg;
    }

    public MessageEvent ( int msg, String mac )
    {
        this.msg = msg;
        this.mac = mac;
    }

    public int getMsg ()
    {
        return msg;
    }

    public void setMsg ( int msg )
    {
        this.msg = msg;
    }

    public String getMac ()
    {
        return mac;
    }

    public void setMac ( String mac )
    {
        this.mac = mac;
    }
}
