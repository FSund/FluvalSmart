package com.liruya.tuner168blemanager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ble.api.DataUtil;
import com.ble.ble.BleCallBack;
import com.ble.ble.BleService;
import com.ble.ble.constants.BleRegConstants;
import com.ble.ble.constants.BleUUIDS;
import com.ble.ble.util.GattUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BleManager implements ServiceConnection {
    private final String TAG = "BleManager";

    /**
     * max lenght of data the bluetooth transfer
     */
    private final int DATA_MAX_LENGTH = 17;

    /**
     * min interval between two receive data frames
     */
    private final int DATA_FRAME_INTERVAL = 64;

    /**
     * min interval between two send data frames
     */
    private final int DATA_SEND_INTERVAL = 32;

    private BleService mBleService;

    private final Set<String> mValidDevices;

    /**
     * receive data list
     */
    private final ArrayList<Byte> mRcvBytes;
    private final Handler mHandler;
    private long msc;
    private final List<BleListener> mBleListeners;

    private final BleCallBack mBleCallBack = new BleCallBack() {
        @Override
        public void onConnected(String s) {
            Log.e(TAG, "onConnected: " + s);
            mValidDevices.remove(s);
            for (BleListener listener : mBleListeners) {
                listener.onConnected(s);
            }
        }

        @Override
        public void onConnectTimeout(String s) {
            Log.e(TAG, "onConnectTimeout: " + s);
            mValidDevices.remove(s);
            if (mBleService != null) {
                mBleService.refresh(s);
            }
            for (BleListener listener : mBleListeners) {
                listener.onConnectTimeout(s);
            }
        }

        @Override
        public void onConnectionError(String s, int i, int i1) {
            Log.e(TAG, "onConnectionError: " + s + " " + i + " " + i1);
            mValidDevices.remove(s);
            for (BleListener listener : mBleListeners) {
                listener.onConnectionError(s, i, i1);
            }
        }

        @Override
        public void onDisconnected(String s) {
            Log.e(TAG, "onDisconnected: " + s);
            mValidDevices.remove(s);
            for (BleListener listener : mBleListeners) {
                listener.onDisconnected(s);
            }
        }

        @Override
        public void onServicesDiscovered(final String s) {
            Log.e(TAG, "onServicesDiscovered: " + s);
            if (mBleService != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothGatt gatt = mBleService.getBluetoothGatt(s);
                        BluetoothGattCharacteristic characteristic = GattUtil.getGattCharacteristic(gatt, BleUUIDS.PRIMARY_SERVICE, BleUUIDS.CHARACTERS[1]);
                        boolean result = mBleService.setCharacteristicNotification(gatt, characteristic, true);
                        if (result) {
                            mValidDevices.add(s);
                        }
                        Log.e(TAG, "Enable Notification: " + result);
                    }
                }, 300);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mValidDevices.contains(s)) {
                            for (BleListener listener : mBleListeners) {
                                listener.onDataValid(s);
                            }
                        }
                    }
                }, 400);
            }
        }

        @Override
        public void onServicesUndiscovered(String s, int i) {
            Log.e(TAG, "onServicesUndiscovered: " + s + "  " + i);
            mValidDevices.remove(s);
        }

        @Override
        public void onRegRead(String s, String s1, int i, int i1) {
            if (i == BleRegConstants.REG_ADV_MFR_SPC) {
                Log.e(TAG, "ReadMfr: " + s);
                for (BleListener listener : mBleListeners) {
                    listener.onReadMfr(s, s1);
                }
            } else if (i == BleRegConstants.REG_PASSWORD) {
                Log.e(TAG, "onReadPassword: " + s);
                byte[] bytes = DataUtil.hexToByteArray(s1);
                if (bytes != null && bytes.length == 4) {
                    int psw = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
                    for (BleListener listener : mBleListeners) {
                        listener.onReadPassword(s, psw);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(String s, byte[] bytes) {
            Log.e(TAG, "onCharacteristicChanged: " + s);
            long t = System.currentTimeMillis();
            if (t - msc > DATA_FRAME_INTERVAL) {
                mRcvBytes.clear();
            }
            for (byte b : bytes) {
                mRcvBytes.add(b);
            }
            msc = t;
            for (BleListener listener : mBleListeners) {
                listener.onDataReceived(s, mRcvBytes);
            }
        }

        @Override
        public void onReadRemoteRssi(String s, int i, int i1) {
            for (BleListener listener : mBleListeners) {
                listener.onReadRssi(s, i);
            }
        }
    };

    private BleManager() {
        mValidDevices = new HashSet<>();
        mRcvBytes = new ArrayList<>();
        mHandler = new Handler();
        mBleListeners = new ArrayList<>();
        msc = System.currentTimeMillis();
    }

    public static BleManager getInstance() {
        return BleHolder.INSTANCE;
    }

    /**
     * bind service to activity
     *
     * @param context
     */
    public void bindService(@NonNull Context context) {
        Intent intent = new Intent(context, BleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind service from activity
     *
     * @param context
     */
    public void unbindService(@NonNull Context context) {
        context.unbindService(this);
    }

    public void refresh(String mac) {
        if (mBleService != null) {
            mBleService.refresh(mac);
        }
    }

    public void startReadRssi(@NonNull String mac) {
        if (mBleService == null) {
            return;
        }
        mBleService.startReadRssi(mac, 1000);
    }

    public void startReadRssi(@NonNull String mac, int interval) {
        if (mBleService == null) {
            return;
        }
        mBleService.startReadRssi(mac, interval);
    }

    public void stopReadRssi(@NonNull String mac) {
        if (mBleService == null) {
            return;
        }
        mBleService.stopReadRssi(mac);
    }

//    public void requestHightPriority(@NonNull String mac) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (mBleService != null) {
//                BluetoothGatt gatt = mBleService.getBluetoothGatt(mac);
//                if (gatt != null) {
//                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
//                }
//            }
//        }
//    }
//
//    public void requestBalancePriority(@NonNull String mac) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (mBleService != null) {
//                BluetoothGatt gatt = mBleService.getBluetoothGatt(mac);
//                if (gatt != null) {
//                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
//                }
//            }
//        }
//    }

    public void enableNotification(@NonNull String mac) {
        if (mBleService != null) {
            BluetoothGatt gatt = mBleService.getBluetoothGatt(mac);
            BluetoothGattCharacteristic characteristic = GattUtil.getGattCharacteristic(gatt, BleUUIDS.PRIMARY_SERVICE, BleUUIDS.CHARACTERS[1]);
            boolean result = mBleService.setCharacteristicNotification(gatt, characteristic, true);
            Log.e(TAG, "enableNotification: " + result);
        }
    }

    public void setAutoConnect(@NonNull String mac, boolean b) {
        if (mBleService == null) {
            return;
        }
        mBleService.setAutoConnect(mac, b);
    }

    /**
     * connect device
     *
     * @param mac device mac address
     * @return true:success false:failure
     */
    public boolean connectDevice(@NonNull final String mac, boolean autoConnect) {
        if (mBleService == null) {
            return false;
        }
        return mBleService.connect(mac, autoConnect);
    }

    /**
     * connect device
     *
     * @param mac device mac address
     * @return true:success false:failure
     */
    public boolean connectDevice(@NonNull final String mac) {
        if (mBleService == null) {
            return false;
        }
        return mBleService.connect(mac, false);
    }

    /**
     * disconnect device
     *
     * @param mac device mac address
     */
    public void disconnectDevice(@NonNull String mac) {
        if (mBleService != null) {
            mBleService.setAutoConnect(mac, false);
            mBleService.disconnect(mac);
        }
    }

    /**
     * disconnect all device
     */
    public void disConnectAll() {
        if (mBleService != null) {
            mBleService.disconnectAll();
        }
    }

    /**
     * read manufactory data of broadcast data
     *
     * @param mac device mac address
     */
    public void readMfr(@NonNull String mac) {
        if (mBleService != null) {
            mBleService.readReg(mac, BleRegConstants.REG_ADV_MFR_SPC);
        }
    }

    public void readPassword(@NonNull String mac) {
        if (mBleService != null) {
            mBleService.readReg(mac, BleRegConstants.REG_PASSWORD);
        }
    }

    public void setPassword(@NonNull String mac, int psw) {
        if (mBleService != null) {
            mBleService.setReg(mac, BleRegConstants.REG_PASSWORD, psw);
        }
    }

    /**
     * set remote device name
     *
     * @param mac device mac address
     * @param name device name
     */
    public void setSlaveName(@NonNull String mac, @NonNull String name) {
        if (mBleService != null) {
            mBleService.setSlaverName(mac, name);
        }
    }

    /**
     * send data to device
     *
     * @param mac device mac address
     * @param bytes data
     */
    public void sendBytes(@NonNull final String mac, @NonNull final byte[] bytes) {
        if (mac == null || bytes == null || mBleService == null) {
            return;
        }
        if (bytes.length <= DATA_MAX_LENGTH) {
            mBleService.send(mac, bytes, true);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx < bytes.length) {
                    int size = Math.min(bytes.length - idx, DATA_MAX_LENGTH);
                    byte[] bts = new byte[size];
                    for (int i = 0; i < size; i++) {
                        bts[i] = bytes[idx];
                        idx++;
                    }
                    int count = 0;
                    while (!mBleService.send(mac, bts, true)) {
                        count++;
                        if (count > 8) {
                            return;
                        }
                        try {
                            Thread.sleep(1);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(8);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void clearReceiveBuffer() {
        if (mRcvBytes != null) {
            mRcvBytes.clear();
        }
    }

    public int getConnectState(@NonNull String mac) {
        if (mBleService == null) {
            return -1;
        }
        return mBleService.getConnectionState(mac);
    }

    public boolean isConnecting(@NonNull String mac) {
        if (mBleService == null) {
            return false;
        }
        return mBleService.getConnectionState(mac) == BluetoothProfile.STATE_CONNECTING;
    }

    /**
     * check if device is connected
     *
     * @param mac device mac address
     * @return
     */
    public boolean isConnected(@NonNull String mac) {
        if (mBleService == null) {
            return false;
        }
        return mBleService.getConnectionState(mac) == BluetoothProfile.STATE_CONNECTED;
    }

    /**
     * check is valid to transfer data
     *
     * @param mac device mac address
     * @return
     */
    public boolean isDataValid(@NonNull String mac) {
        return mValidDevices.contains(mac);
    }

    public void addBleListener(BleListener listener) {
        if (!mBleListeners.contains(listener)) {
            mBleListeners.add(listener);
        }
    }

    public void removeBleListener(BleListener listener) {
        mBleListeners.remove(listener);
    }

    public void removeAllBleListeners() {
        mBleListeners.clear();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBleService = ((BleService.LocalBinder) service).getService(mBleCallBack);
        mBleService.setDecode(true);
        mBleService.setConnectTimeout(4000);
        //必须调用初始化方法
        mBleService.initialize();
        Log.e(TAG, "onServiceConnected: ");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBleService = null;
        Log.e(TAG, "onServiceDisconnected: ");
    }

    /**
     * Single instance holder
     */
    private static class BleHolder {
        private static final BleManager INSTANCE = new BleManager();
    }
}
