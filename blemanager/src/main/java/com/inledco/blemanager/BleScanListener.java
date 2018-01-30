package com.inledco.blemanager;

/**
 * Created by liruya on 2017/4/25.
 */

public interface BleScanListener
{
    void onStartScan ();

    void onStopScan ();

    void onDeviceScanned ( String mac, String name, byte[] bytes );
}
