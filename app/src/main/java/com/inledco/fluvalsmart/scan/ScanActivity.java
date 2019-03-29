package com.inledco.fluvalsmart.scan;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ble.api.DataUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.bean.SelectDevice;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.liruya.tuner168blemanager.BleHelper;
import com.liruya.tuner168blemanager.BleListener;
import com.liruya.tuner168blemanager.BleManager;
import com.liruya.tuner168blemanager.BleScanListener;
import com.liruya.tuner168blemanager.BleScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScanActivity extends BaseActivity {
    private final int BLUETOOTH_REQUEST_ENABLE_CODE = 1;
    private final int SCAN_CODE = 3;
    private Toolbar scan_toolbar;
    private ToggleButton scan_tb_scan;
    private ProgressBar scan_pb_scanning;
    private RecyclerView scan_rv_show;
    private TextView scan_tv_msg;
    private FloatingActionButton scan_fab_confirm;

    private Map<String, DevicePrefer> storedAddress;

    private final Set<String> mDeviceMacs = new HashSet<>();
    private final ArrayList<SelectDevice> mDevices = new ArrayList<>();
    private Comparator<SelectDevice> mComparator;
    private ScanAdapter mScanAdapter;
    private Handler mHandler;
    private final BleHelper mBleHelper = new BleHelper(this);

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_OFF) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scan_tb_scan.setChecked(false);
                            scan_pb_scanning.setVisibility(View.GONE);
                        }
                    });
                    stopScan();
                }
            }
        }
    };

    private final BleScanListener mScanListener = new BleScanListener() {
        @Override
        public void onScanTimeout() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scan_tb_scan.setChecked(false);
                    if (mDevices.size() == 0 && !mBleHelper.isLocationEnabled()) {
                        scan_tv_msg.setVisibility(View.VISIBLE);
                    } else {
                        scan_tv_msg.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onDeviceScanned(String mac, String name, int rssi, byte[] bytes) {
            decodeScanData(mac, name, rssi, bytes);
        }
    };

    private final BleListener mBleListener = new BleListener() {
        @Override
        protected void onDataValid(String mac) {
            BleManager.getInstance().readMfr(mac);
        }

        @Override
        protected void onReadMfr(String mac, String s) {
            BleManager.getInstance().disconnectDevice(mac);
            decodeMfrData(mac, s);
        }
    };

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        if (scan_tb_scan != null) {
            scan_tb_scan.setChecked(false);
        }
        stopScan();
        BleManager.getInstance().removeBleListener(mBleListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        MenuItem menuItem = menu.findItem(R.id.scan_menu_scan);
        scan_pb_scanning = menuItem.getActionView()
                                   .findViewById(R.id.menu_item_progress);
        scan_tb_scan = menuItem.getActionView()
                               .findViewById(R.id.menu_item_scan);
        scan_tb_scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBleHelper.isBluetoothEnabled()) {
                        startScan();
                    } else {
                        mBleHelper.requestBluetoothEnable(BLUETOOTH_REQUEST_ENABLE_CODE);
                    }
                }
                else {
                    stopScan();
                }
                scan_pb_scanning.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        scan_tb_scan.setChecked(true);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (requestCode == BLUETOOTH_REQUEST_ENABLE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                scan_tb_scan.setChecked(true);
            } else {
                scan_tb_scan.setChecked(false);
                Toast.makeText(ScanActivity.this, R.string.snackbar_bluetooth_denied, Toast.LENGTH_LONG)
                     .show();
            }
        }
    }

    @SuppressLint ("RestrictedApi")
    @Override
    protected void initView() {
        scan_toolbar = findViewById(R.id.scan_toolbar);
        scan_toolbar.setTitle("");
        setSupportActionBar(scan_toolbar);
        scan_rv_show = findViewById(R.id.scan_rv_show);
        scan_fab_confirm = findViewById(R.id.scan_fab_confirm);
        scan_tv_msg = findViewById(R.id.scan_tv_msg);

        scan_tv_msg.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_right_white_32dp, 0);
        scan_rv_show.addItemDecoration(new DividerItemDecoration(this, OrientationHelper.VERTICAL));
        scan_fab_confirm.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mHandler = new Handler() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                boolean show = false;
                switch (msg.what) {
                    case 0:
                        for (SelectDevice dev : mDevices) {
                            if (dev.isSelectable() && dev.isSelected()) {
                                show = true;
                                break;
                            }
                        }
                        if (show) {
                            if (scan_fab_confirm.getVisibility() != View.VISIBLE) {
                                scan_fab_confirm.setVisibility(View.VISIBLE);
                                ValueAnimator animator = ObjectAnimator.ofFloat(scan_fab_confirm, "alpha", 0.5f, 1.0f);
                                animator.setDuration(800);
                                animator.setRepeatCount(ValueAnimator.INFINITE);
                                animator.setRepeatMode(ValueAnimator.REVERSE);
                                animator.start();
                            }
                        }
                        else {
                            scan_fab_confirm.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };
        BleScanner.getInstance().setBleScanListener(mScanListener);
        BleManager.getInstance().addBleListener(mBleListener);

        mComparator = new Comparator<SelectDevice>() {
            @Override
            public int compare(SelectDevice o1, SelectDevice o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return o2.getRssi()-o1.getRssi();
            }
        };
        mScanAdapter = new ScanAdapter(ScanActivity.this, mHandler, mDevices);
        mScanAdapter.setShowRssi(Setting.showRssi());
        scan_rv_show.setAdapter(mScanAdapter);
    }

    @Override
    protected void initEvent() {
        scan_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
                finish();
            }
        });

        //确认按键点击事件
        scan_fab_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SelectDevice dev : mDevices) {
                    if (dev.isSelected()) {
                        PreferenceUtil.setObjectToPrefer(ScanActivity.this,
                                                         ConstVal.DEV_PREFER_FILENAME,
                                                         dev.getPrefer(),
                                                         dev.getPrefer()
                                                            .getDeviceMac());
                    }
                }
                setResult(SCAN_CODE);
                finish();
            }
        });

        scan_tv_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleHelper.startLocationActivity();
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    private void startScan() {
        storedAddress = PreferenceUtil.getAllObjectMapFromPrefer(ScanActivity.this, ConstVal.DEV_PREFER_FILENAME);
        mDeviceMacs.clear();
        mDevices.clear();
        mScanAdapter.notifyDataSetChanged();
        scan_fab_confirm.setVisibility(View.GONE);
        scan_tv_msg.setVisibility(View.GONE);
        BleScanner.getInstance().startScan();
    }

    private void stopScan() {
        BleScanner.getInstance().stopScan();
        BleManager.getInstance()
                  .disConnectAll();
    }

    private void decodeScanData(final String mac, String name, int rssi, byte[] bytes) {
        if (mDeviceMacs.contains(mac)) {
            for (int i = 0; i < mDevices.size(); i++) {
                if (mDevices.get(i)
                            .getPrefer()
                            .getDeviceMac()
                            .equals(mac))
                {
                    mDevices.get(i)
                            .setRssi(rssi);
                    break;
                }
            }
        }
        else {
            boolean flag = false;
            if (storedAddress != null && storedAddress.containsKey(mac)) {
                mDeviceMacs.add(mac);
                SelectDevice device = new SelectDevice(false, true, rssi, storedAddress.get(mac));
                mDevices.add(device);
                flag = true;
            }
            else {
                if (bytes == null || bytes.length == 0) {
                    mDeviceMacs.add(mac);
                    SelectDevice device = new SelectDevice(false, false, rssi, new DevicePrefer((short) 0, mac, name));
                    mDevices.add(device);
                    flag = true;
                    BleManager.getInstance()
                              .connectDevice(mac);
                }
                else {
                    short devid = 0;
                    for (int i = 0; i < 4 && i < bytes.length; i++) {
                        if (bytes[i] >= 0x30 && bytes[i] <= 0x39) {
                            devid = (short) ((devid << 4) | (bytes[i] - 0x30));
                        }
                        else {
                            break;
                        }
                    }
                    boolean selectable = DeviceUtil.isCorrectDevType(devid);
                    mDeviceMacs.add(mac);
                    SelectDevice device = new SelectDevice(selectable, false, rssi, new DevicePrefer(devid, mac, name));
                    mDevices.add(device);
                    flag = true;
                }
            }
            if (flag && mScanAdapter.isShowRssi() == false) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScanAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        if (mScanAdapter.isShowRssi()) {
            Collections.sort(mDevices, mComparator);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mScanAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void decodeMfrData(String mac, String s) {
        byte[] mfr = DataUtil.hexToByteArray(s.replace(" ", ""));
        //        LogUtil.d( TAG, "onReadMfr: " + mac + "\t" + s );
        short devid;
        if (mfr == null || mfr.length < 2) {
            devid = 0;
        }
        else {
            devid = (short) (((mfr[0] & 0xFF) << 8) | (mfr[1] & 0xFF));
        }
        if (!DeviceUtil.isCorrectDevType(devid)) {
            return;
        }
        for (int i = 0; i < mDevices.size(); i++) {
            if (mDevices.get(i)
                        .getPrefer()
                        .getDeviceMac()
                        .equals(mac))
            {
                mDevices.get(i)
                        .getPrefer()
                        .setDevId(devid);
                mDevices.get(i)
                        .setSelectable(true);
                final int position = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScanAdapter.notifyItemChanged(position);
                    }
                });
                break;
            }
        }
    }
}
