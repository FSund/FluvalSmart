package com.inledco.fluvalsmart.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.scan.ScanActivity;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.liruya.tuner168blemanager.BleHelper;
import com.liruya.tuner168blemanager.BleManager;

public class MainActivity extends BaseActivity {
    private BottomNavigationView main_bottom_navigation;

    private BleHelper mBleHelper;

    //双击back退出标志位
    private boolean mExiting;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().unbindService(MainActivity.this);
        BleManager.getInstance().disConnectAll();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + "  " + resultCode);
        switch (requestCode) {
            case ConstVal.BLUETOOTH_REQUEST_ENABLE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                }
                else {
                    Toast.makeText(MainActivity.this, R.string.snackbar_bluetooth_denied, Toast.LENGTH_LONG)
                         .show();
                }
                break;
            case ConstVal.SCAN_CODE:
                if (resultCode == ConstVal.SCAN_CODE) {
                    replaceFragment(R.id.main_fl_show, new DeviceFragment());
                }
                break;
            case ConstVal.RENAME_CODE:
                if (resultCode == ConstVal.RENAME_CODE) {
                    replaceFragment(R.id.main_fl_show, new DeviceFragment());
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: ");
        if (permissions == null || grantResults == null || permissions.length < 1 || grantResults.length < 1) {
            return;
        }
        if (requestCode == ConstVal.PERMISSON_REQUEST_COARSE_CODE && Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0])) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanActivity();
            }
            else {
                if (mBleHelper.shouldShowRequestPermissionRationale()) {
                    Toast.makeText(MainActivity.this, R.string.snackbar_coarselocation_denied, Toast.LENGTH_SHORT)
                         .show();
                } else {
                    showPermissionDialog();
                }
            }
        }
    }

    @Override
    protected void initView() {
        main_bottom_navigation = findViewById(R.id.main_bottom_navigation);
    }

    @Override
    protected void initData() {
        BleManager.getInstance().bindService(MainActivity.this);
        mBleHelper = new BleHelper(this);
        if (mBleHelper.checkBleSupported()) {
            if (mBleHelper.isBluetoothEnabled()
                || (Setting.isAutoTurnonBle(MainActivity.this) && mBleHelper.autoOpenBluetooth())) {

            }
            else {
                mBleHelper.requestBluetoothEnable(ConstVal.BLUETOOTH_REQUEST_ENABLE_CODE);
            }
        } else {
            Toast.makeText(this, R.string.ble_no_support, Toast.LENGTH_SHORT)
                 .show();
            finish();
            return;
        }
        replaceFragment(R.id.main_fl_show, new DeviceFragment());
    }

    @Override
    protected void initEvent() {
        main_bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_btm_device:
                        replaceFragment(R.id.main_fl_show, new DeviceFragment());
                        break;
                    case R.id.menu_btm_news:
                        replaceFragment(R.id.main_fl_show, new NewsFragment());
                        break;
                    case R.id.menu_btm_setting:
                        replaceFragment(R.id.main_fl_show, new UserFragment());
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        if (!mExiting) {
            mExiting = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mExiting = false;
                }
            }, 1500);
            Toast.makeText(MainActivity.this, R.string.exit_app_tips, Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        else {
            //如果退出时不提示 且设置为退出关闭BLE
            if (Setting.isExitTurnoffBle(MainActivity.this)) {
                mBleHelper.closeBluetooth();
            }
            finish();
        }
        super.onBackPressed();
    }

    private void startScanActivity() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, ConstVal.SCAN_CODE);
    }

    private void showPermissionDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(MainActivity.this, R.style.DialogTheme);
        builder.setTitle(R.string.turnon_location_permission);
        builder.setMessage(R.string.msg_turnon_location_permission);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBleHelper.startAppDetailActivity();
            }
        });
        builder.show();
    }
}
