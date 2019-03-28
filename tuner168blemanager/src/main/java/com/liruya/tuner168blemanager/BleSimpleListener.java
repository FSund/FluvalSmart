package com.liruya.tuner168blemanager;

import android.text.TextUtils;

import java.util.List;

public abstract class BleSimpleListener extends BleListener {
    private String mAddress;

    public BleSimpleListener(String address) {
        mAddress = address;
    }

    @Override
    protected void onConnected(String mac) {
        if (TextUtils.equals(mAddress, mac)) {
            onConnected();
        }
    }

    @Override
    protected void onConnectTimeout(String mac) {
        if (TextUtils.equals(mAddress, mac)) {
            onConnectTimeout();
        }
    }

    @Override
    protected void onConnectionError(String mac, int error, int newState) {
        if (TextUtils.equals(mAddress, mac)) {
            onConnectionError(error, newState);
        }
    }

    @Override
    protected void onDisconnected(String mac) {
        if (TextUtils.equals(mAddress, mac)) {
            onDisconnected();
        }
    }

    @Override
    protected void onDataValid(String mac) {
        if (TextUtils.equals(mAddress, mac)) {
            onDataValid();
        }
    }

    @Override
    protected void onReadRssi(String mac, int rssi) {
        if (TextUtils.equals(mAddress, mac)) {
            onReadRssi(rssi);
        }
    }

    @Override
    protected void onDataReceived(String mac, List<Byte> bytes) {
        if (TextUtils.equals(mAddress, mac)) {
            onDataReceived(bytes);
        }
    }

    @Override
    protected void onReadMfr(String mac, String s) {
        if (TextUtils.equals(mAddress, mac)) {
            onReadMfr(s);
        }
    }

    @Override
    protected void onReadPassword(String mac, int password) {
        if (TextUtils.equals(mAddress, mac)) {
            onReadPassword(password);
        }
    }

    public void onConnected() {}

    public void onConnectTimeout() {}

    public void onConnectionError(int error, int newState) {}

    public void onDisconnected() {}

    public void onDataValid() {}

    public void onReadRssi(int rssi) {}

    public void onDataReceived(List<Byte> bytes) {}

    public void onReadMfr(String s) {}

    public void onReadPassword(int password) {}
}
