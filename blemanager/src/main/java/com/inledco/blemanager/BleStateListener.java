package com.inledco.blemanager;

/**
 * Created by liruya on 2017/4/25.
 */

public interface BleStateListener
{
    void onBluetoothEnabled ();

    void onBluetoothDisabled ();

    void onBluetoothDenied ();

    void onCoarseLocationGranted ();

    void onCoarseLocationDenied ();

    void onBleInitialized ();
}
