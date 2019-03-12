package com.liruya.tuner168blemanager;

import java.util.List;

public abstract class BleListener {
    protected void onConnected(String mac) {

    }

    protected void onConnectTimeout(String mac) {

    }

    protected void onConnectionError(String mac, int error, int newState) {

    }

    protected void onDisconnected(String mac) {

    }

    protected void onDataValid(String mac) {

    }

    protected void onReadRssi(String mac, int rssi) {

    }

    protected void onDataReceived(String mac, List<Byte> bytes) {

    }

    protected void onReadMfr(String mac, String s) {

    }

    protected void onReadPassword(String mac, int password) {

    }
}
