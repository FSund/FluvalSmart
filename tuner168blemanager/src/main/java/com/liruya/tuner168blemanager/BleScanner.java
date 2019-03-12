package com.liruya.tuner168blemanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import com.ble.ble.LeScanRecord;
import com.ble.ble.constants.BleUUIDS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleScanner {
    private static final String TAG = "BleScanner";

    private final UUID[] TARGET_UUIDS = new UUID[] {BleUUIDS.PRIMARY_SERVICE};

    /**
     * BluetoothLeScanner callback (Android Version >= 5.0)
     */
    private ScanCallback mScanCallback;

    /**
     * LeScanCallback (Android Version < 5.0)
     */
    BluetoothAdapter.LeScanCallback mLeScanCallback;

    private int mScanPeriod = 12000;

    private boolean mScanning;

    private Handler mHandler;

    private Runnable mScanRunnable;

    private BleScanListener mListener;

    public static BleScanner getInstance() {
        return LazyHolder.INSTANCE;
    }

    private BleScanner() {
        mHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (result != null) {
                        BluetoothDevice device = result.getDevice();
                        if (device != null) {
                            onScan(device.getAddress(),
                                   device.getName(),
                                   result.getRssi(),
                                   result.getScanRecord()
                                         .getBytes());
                        }
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    Log.e(TAG, "onBatchScanResults: " + results.size());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.e(TAG, "onScanFailed: " + errorCode);
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null) {
                        onScan(device.getAddress(),
                               device.getName(),
                               rssi,
                               scanRecord);
                    }
                }
            };
        }
        mScanRunnable = new Runnable() {
            @Override
            public void run() {
                stopScan();
                if (mListener != null) {
                    mListener.onScanTimeout();
                }
            }
        };
    }

    public int getScanPeriod() {
        return mScanPeriod;
    }

    public void setScanPeriod(int scanPeriod) {
        mScanPeriod = scanPeriod;
    }

    public void setBleScanListener(BleScanListener listener) {
        mListener = listener;
    }

    private void onScan(String mac, String name, int rssi, byte[] scanRecord) {
        LeScanRecord record = LeScanRecord.parseFromBytes(scanRecord);
        if (record == null)
        {
            return;
        }
        SparseArray<byte[]> bytes = record.getManufacturerSpecificData();
        Log.e(TAG, "onLeScan: Mac: " + mac + " rssi - " + rssi + "  \t" + record.toString() + "\r\n" + bytes.size());
        byte[] rawData = null;
        if (bytes != null && bytes.size() > 0) {
            int id = bytes.keyAt(0);
            byte[] mfr = bytes.get(id);
            rawData = new byte[2 + mfr.length];
            rawData[0] = (byte) (id & 0xFF);
            rawData[1] = (byte) ((id >> 8) & 0xFF);
            System.arraycopy(mfr, 0, rawData, 2, mfr.length);
        }
        if (mListener != null) {
            mListener.onDeviceScanned(mac, name, rssi, rawData);
        }
    }

    public void startScan() {
        if (mScanning) {
            return;
        }
        mScanning = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(BleUUIDS.PRIMARY_SERVICE))
                                                        .build();
            filters.add(filter);
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                                                  .build();
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(filters, scanSettings, mScanCallback);
        } else {
            BluetoothAdapter.getDefaultAdapter()
                            .startLeScan(TARGET_UUIDS, mLeScanCallback);
        }
        mHandler.postDelayed(mScanRunnable, mScanPeriod);
    }

    public void stopScan()
    {
        if (mScanning) {
            mScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothAdapter.getDefaultAdapter()
                                .getBluetoothLeScanner()
                                .stopScan(mScanCallback);
            } else {
                BluetoothAdapter.getDefaultAdapter()
                                .stopLeScan(mLeScanCallback);
            }
            mHandler.removeCallbacks(mScanRunnable);
        }
    }

    private static class LazyHolder {
        private static final BleScanner INSTANCE = new BleScanner();
    }
}
