package com.inledco.blemanager;

import java.util.ArrayList;

/**
 * Created by liruya on 2017/4/25.
 */

public interface BleCommunicateListener
{
    void onDataValid ( String mac );

    void onDataInvalid ( String mac );

    void onReadMfr ( String mac, String s );

    void onDataReceived ( String mac, ArrayList< Byte > list );
}
