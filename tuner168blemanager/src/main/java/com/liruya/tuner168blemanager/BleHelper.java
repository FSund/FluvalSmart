package com.liruya.tuner168blemanager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class BleHelper {
    private final String TAG = "BleHelper";

    /** Anything worse than or equal to this will show 0 bars. */
    private final int MIN_RSSI = -100;

    /** Anything better than or equal to this will show the max bars. */
    private final int MAX_RSSI = -55;

    private WeakReference<AppCompatActivity> mActivity;

    public BleHelper(@NonNull AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public boolean checkBleSupported() {
        if (mActivity.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return BluetoothAdapter.getDefaultAdapter() != null;
        }
        return false;
    }

    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public boolean autoOpenBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().enable();
    }

    public void closeBluetooth() {
        BluetoothAdapter.getDefaultAdapter().disable();
    }


    /**
     * Check if Location is enabled, some phones need to enable location to scan bluetooth devices
     * @return
     */
    public boolean isLocationEnabled() {
        LocationManager manager = (LocationManager) mActivity.get().getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    /**
     * Check if Location permission is allowed, for phones which use Android 6.0 and above
     * need location permission to scan bluetooth device
     * @return
     */
    public boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return ContextCompat.checkSelfPermission(mActivity.get(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * request location permission
     * @param requestCode
     */
    public void requestLocationPermission(int requestCode) {
        ActivityCompat.requestPermissions(mActivity.get(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
    }

    /**
     * request open bluetooth
     * @param requestCode
     */
    public void requestBluetoothEnable(int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mActivity.get().startActivityForResult(intent, requestCode);
    }

    public boolean shouldShowRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(mActivity.get(), Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * start Application Detail Activity to change permission
     * used when user disallow location permission forever
     */
    public void startAppDetailActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                                mActivity.get().getPackageName(),
                                null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.get().startActivity(intent);
    }

    /**
     * start location activity
     */
    public void startLocationActivity() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.get().startActivity(intent);
    }

    /**
     * Calculate Bluetooth signal level
     * @param rssi
     * @param numLevels
     * @return
     */
    public int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            int inputRange = (MAX_RSSI - MIN_RSSI);
            int outputRange = (numLevels - 1);
            return (rssi - MIN_RSSI) * outputRange / inputRange;
        }
    }
}
