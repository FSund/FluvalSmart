package com.inledco.fluvalsmart.light;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.api.DataUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.bean.Light;
import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.ota.BleOTAActivity;
import com.inledco.fluvalsmart.ota.RemoteFirmware;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LightPrefUtil;
import com.inledco.fluvalsmart.util.Md5Util;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.inledco.fluvalsmart.view.CustomProgressDialog;
import com.inledco.fluvalsmart.viewmodel.LightViewModel;
import com.liruya.okhttpmanager.HttpCallback;
import com.liruya.okhttpmanager.OKHttpManager;
import com.liruya.tuner168blemanager.BleListener;
import com.liruya.tuner168blemanager.BleManager;
import com.liruya.tuner168blemanager.BleSimpleListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.Call;

public class LightActivity extends BaseActivity implements DataInvalidFragment.OnRetryClickListener {
    private static final String OTA_UPGRADE_LINK = "http://47.88.12.183:8080/OTAInfoModels/GetOTAInfo?deviceid=";

    private Toolbar light_toolbar;
    private ProgressDialog mProgressDialog;
    private AlertDialog mPasswordDialog;
    private LinearLayout light_mode_show;
    private CheckedTextView light_ctv_manual;
    private CheckedTextView light_ctv_auto;
    private CheckedTextView light_ctv_pro;
    private MenuItem menu_device_update;
    private MenuItem menu_device_status;

    private final int STATE_NONE = 0;
    private final int STATE_LOGIN = 1;
    private final int STATE_GET_INFO = 2;
    private final int STATE_GET_DATA = 3;
    private int mState;

    private String mAddress;
    private DevicePrefer mPrefer;

    private CountDownTimer mCountDownTimer;

    private int mDeviceVersion;
    private int mRemoteVersion;

    private boolean mAutoConnect;

    private BleListener mBleListener;
    private final Handler mHandler = new Handler();

    private LightViewModel mLightViewModel;
    private Light mLight;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        Intent intent = getIntent();
        mPrefer = (DevicePrefer) intent.getSerializableExtra("DevicePrefer");
        mAddress = mPrefer.getDeviceMac();
        mLight = new Light(mPrefer);
        mLightViewModel = ViewModelProviders.of(LightActivity.this).get(LightViewModel.class);
        mLightViewModel.setData(mLight);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (BleManager.getInstance().isConnected(mAddress)) {
            BleManager.getInstance().setAutoConnect(mAddress, mAutoConnect);
        } else {
            BleManager.getInstance().connectDevice(mAddress, mAutoConnect);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().setAutoConnect(mAddress, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().removeBleListener(mBleListener);
        BleManager.getInstance().disconnectDevice(mAddress);
        BleManager.getInstance().refresh(mAddress);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        menu_device_status = menu.findItem(R.id.menu_device_status);
        MenuItem menu_device_edit = menu.findItem(R.id.menu_device_rename);
        MenuItem menu_device_find = menu.findItem(R.id.menu_device_find);
        menu_device_update = menu.findItem(R.id.menu_device_update);
        MenuItem menu_device_modpsw = menu.findItem(R.id.menu_device_modpsw);
        menu_device_status.setIcon(BleManager.getInstance().isDataValid(mAddress) ? R.drawable.ic_bluetooth_connected_white_24dp : R.drawable.ic_bluetooth_disabled_grey_500_24dp);
        menu_device_edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showRenameDialog(mPrefer);
                return false;
            }
        });
        menu_device_find.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                CommUtil.findDevice(mPrefer.getDeviceMac());
                return false;
            }
        });
        menu_device_update.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mPrefer != null) {
                    startOtaActivity();
                    return true;
                }
                return false;
            }
        });
        menu_device_modpsw.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showModifyPasswordDialog();
                return false;
            }
        });
        return true;
    }

    @Override
    protected void initView() {
        light_mode_show = findViewById(R.id.light_mode_show);
        light_ctv_manual = findViewById(R.id.light_ctv_manual);
        light_ctv_auto = findViewById(R.id.light_ctv_auto);
        light_ctv_pro = findViewById(R.id.light_ctv_pro);
        light_toolbar = findViewById(R.id.light_toolbar);
        light_toolbar.setTitle(mPrefer.getDeviceName());
        setSupportActionBar(light_toolbar);

        mProgressDialog = new CustomProgressDialog(this, R.style.DialogTheme);
    }

    @Override
    protected void initData() {
        OKHttpManager.getInstance()
                     .get(OTA_UPGRADE_LINK + mPrefer.getDevId(), null, new HttpCallback<RemoteFirmware>() {
                         @Override
                         public void onFailure(Call call, IOException e) {

                         }

                         @Override
                         public void onError(int code, final String msg) {
                         }

                         @Override
                         public void onSuccess(final RemoteFirmware result) {
                             mRemoteVersion = (result.getMajor_version() << 8) | result.getMinor_version();
                             showUpgradeStatus();
                         }
                     });
        mCountDownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        showDataInvalidFragment();
                    }
                });
            }
        };
        mBleListener = new BleSimpleListener(mAddress) {
            @Override
            public void onConnected() {
                mAutoConnect = true;
                BleManager.getInstance().setAutoConnect(mAddress, true);
            }

            @Override
            public void onConnectTimeout() {
                showDisconnectStatus();
                if (mProgressDialog.isShowing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                        }
                    });
                }
                showDataInvalidFragment();
            }

            @Override
            public void onConnectionError(int error, int newState) {
                showDisconnectStatus();
            }

            @Override
            public void onDisconnected() {
                showDisconnectStatus();
            }

            @Override
            public void onDataValid() {
                showConnectStatus();
                mCountDownTimer.cancel();
                if (mState < STATE_LOGIN) {
                    readPassword();
                } else if (mState < STATE_GET_INFO) {
                    readMfr();
                } else if (mState < STATE_GET_DATA) {
                    syncDeviceDatetime();
                }
            }

            @Override
            public void onReadRssi(int rssi) {
            }

            @Override
            public void onReadMfr(String s) {
//                mState = STATE_GET_INFO;
//                syncDeviceDatetime();
                byte[] mfr = DataUtil.hexToByteArray(s.replace(" ", ""));
                if (mfr == null || mfr.length < 4) {
                    mDeviceVersion = 0;
                } else {
                    mDeviceVersion = ((mfr[2] & 0xFF) << 8) | (mfr[3] & 0xFF);
                    mState = STATE_GET_INFO;
                    syncDeviceDatetime();
                }
                showUpgradeStatus();
            }

            @Override
            public void onReadPassword(final int password) {
                if (mState < STATE_LOGIN) {
                    final int psw = LightPrefUtil.getLocalPassword(LightActivity.this, mAddress);
                    if (psw == password) {
                        mState = STATE_LOGIN;
                        readMfr();
                    } else {
                        mCountDownTimer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showPasswordDialog(password);
                                mProgressDialog.dismiss();
                            }
                        });
                    }
                }
            }

            @Override
            public void onDataReceived(List<Byte> bytes) {
                decodeReceiveData(bytes);
            }
        };
        BleManager.getInstance().addBleListener(mBleListener);
        light_toolbar.setTitle(mPrefer.getDeviceName());
        light_mode_show.setVisibility(View.GONE);
        getDeviceData();
    }

    @Override
    protected void initEvent() {
        light_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                BleManager.getInstance().disconnectDevice(mAddress);
            }
        });

        light_ctv_manual.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!light_ctv_manual.isChecked()) {
                    CommUtil.setModeManual(mAddress);
                }
            }
        });

        light_ctv_auto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!light_ctv_auto.isChecked()) {
                    CommUtil.setModeAuto(mAddress);
                }
            }
        });

        light_ctv_pro.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!light_ctv_pro.isChecked()) {
                    CommUtil.setModePro(mAddress);
                }
            }
        });
    }

    private void showConnectStatus() {
        if (menu_device_status != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu_device_status.setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
                }
            });
        }
    }

    private void showDisconnectStatus() {
        if (menu_device_status != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu_device_status.setIcon(R.drawable.ic_bluetooth_disabled_grey_500_24dp);
                }
            });
        }
    }

    private void showUpgradeStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDeviceVersion > 0 && mRemoteVersion > 0 && mDeviceVersion < mRemoteVersion) {
                    menu_device_update.setVisible(true);
                }
                else {
                    menu_device_update.setVisible(false);
                }
            }
        });
    }

    private void showDataInvalidFragment() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                light_mode_show.setVisibility(View.GONE);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.light_fl_show, DataInvalidFragment.newInstance(mPrefer.getDeviceMac()))
                  .commitAllowingStateLoss();
            }
        });
    }

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(msg);
                mProgressDialog.show();
            }
        });
    }

    private void showMessage(@StringRes int resid) {
        showMessage(getString(resid));
    }

    private void readPassword() {
        Log.e(TAG, "readPassword: ");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().readPassword(mAddress);
                showMessage(R.string.logging);
            }
        }, 50);
        mCountDownTimer.start();
    }

    private void readMfr() {
        Log.e(TAG, "readMfr: " );
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().readMfr(mAddress);
                showMessage(R.string.msg_get_device_data);
            }
        }, 50);
    }

    private void syncDeviceDatetime() {
        Log.e(TAG, "syncDeviceDatetime: " );
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CommUtil.syncDeviceTime(mAddress);
            }
        }, 100);
    }

    public void getDeviceData() {
        if (BleManager.getInstance().isConnected(mAddress)) {
            if (BleManager.getInstance().isDataValid(mAddress)) {
                if (mState < STATE_LOGIN) {
                    readPassword();
                } else if (mState < STATE_GET_INFO) {
                    readMfr();
                } else if (mState < STATE_GET_DATA) {
                    showMessage(R.string.msg_get_device_data);
                    syncDeviceDatetime();
                }
                mCountDownTimer.start();
            } else {
                BleManager.getInstance().disconnectDevice(mAddress);
                showMessage(R.string.msg_connecting_device);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (BleManager.getInstance().isConnected(mAddress));
                        BleManager.getInstance().connectDevice(mAddress);
                        mCountDownTimer.start();
                    }
                }).start();
            }
        } else {
            BleManager.getInstance().connectDevice(mAddress);
            showMessage(R.string.msg_connecting_device);
            mCountDownTimer.start();
        }
    }

    private boolean isNewVersion() {
        return mDeviceVersion > 258;
    }

    private void updateLightManual() {
        runOnUiThread(new Runnable() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void run() {
                mProgressDialog.dismiss();
                if (light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_manual.isChecked()) {
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if (mPrefer.getDevId() == DeviceUtil.LIGHT_ID_RGBW ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_600 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_900 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_1200 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_380 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_530 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_835 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_990 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_750 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_1150 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_910 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_ROMA90 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_ROMA125 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_ROMA200 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_ROMA240 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_VI180 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_VI260 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_VE190 ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_VE350A ||
                        mPrefer.getDevId() == DeviceUtil.LIGHT_ID_VE350B )
                    {
                        ft.replace(R.id.light_fl_show, new RGBWManualFragment())
                          .commitAllowingStateLoss();
                    }
                    else {
                        ft.replace(R.id.light_fl_show, new LightManualFragment())
                          .commitAllowingStateLoss();
                    }
                }
                light_mode_show.setVisibility(View.VISIBLE);
                light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                light_ctv_manual.setChecked(true);
                light_ctv_auto.setChecked(false);
                light_ctv_pro.setChecked(false);
                light_toolbar.setSubtitle("");
            }
        });
    }

    private void updateLightAuto() {
        runOnUiThread(new Runnable() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void run() {
                mProgressDialog.dismiss();
                if (light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_auto.isChecked()) {
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.light_fl_show, new LightAutoFragment())
                      .commitAllowingStateLoss();
                }
                light_mode_show.setVisibility(View.VISIBLE);
                light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                light_ctv_manual.setChecked(false);
                light_ctv_auto.setChecked(true);
                light_ctv_pro.setChecked(false);
                String prof = LightPrefUtil.getAutoProfileName(LightActivity.this, mPrefer.getDevId(), mLight.getLightAuto());
                light_toolbar.setSubtitle(prof);
            }
        });
    }

    private void updateLightPro() {
        runOnUiThread(new Runnable() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void run() {
                mProgressDialog.dismiss();
                if (light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_pro.isChecked()) {
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.light_fl_show, new LightProFragment())
                      .commitAllowingStateLoss();
                }
                light_mode_show.setVisibility(View.VISIBLE);
                light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                light_ctv_manual.setChecked(false);
                light_ctv_auto.setChecked(false);
                light_ctv_pro.setChecked(true);
                String prof = LightPrefUtil.getProProfileName(LightActivity.this, mPrefer.getDevId(), mLight.getLightPro());
                light_toolbar.setSubtitle(prof);
            }
        });
    }

    private void decodeReceiveData(List<Byte> list) {
        final Object object = CommUtil.decodeLight(list, mPrefer.getDevId());
        if (object != null) {
            if (object instanceof LightAuto) {
                mCountDownTimer.cancel();
                mLight.setMode(Light.MODE_AUTO);
                mLight.setLightAuto((LightAuto) object);
                mLightViewModel.postValue();
                updateLightAuto();
            } else if (object instanceof LightPro) {
                mCountDownTimer.cancel();
                mLight.setMode(Light.MODE_PRO);
                mLight.setLightPro((LightPro) object);
                mLightViewModel.postValue();
                updateLightPro();
            } else if (object instanceof LightManual) {
                mCountDownTimer.cancel();
                mLight.setMode(Light.MODE_MANUAL);
                mLight.setLightManual((LightManual) object);
                mLightViewModel.postValue();
                updateLightManual();
            }
        }
    }

    private void showRenameDialog(final DevicePrefer prefer) {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        //        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View view = LayoutInflater.from(this)
                                  .inflate(R.layout.dialog_rename, null);
        builder.setTitle(R.string.rename_device);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final Button btn_cancel = view.findViewById(R.id.rename_cancel);
        final Button btn_rename = view.findViewById(R.id.rename_confirm);
        final TextInputLayout til = view.findViewById(R.id.rename_til);
        final TextInputEditText newname = view.findViewById(R.id.rename_newname);
        final boolean[] flag = new boolean[]{false};
        newname.setText(prefer.getDeviceName());
        newname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && start == 0 && flag[0] == false) {
                    flag[0] = true;
                    String str = new StringBuilder().append(s.subSequence(0, 1))
                                                    .toString()
                                                    .toUpperCase();
                    str = new StringBuilder(str).append(s.subSequence(1, s.length()))
                                                .toString();
                    newname.setText(str);
                    newname.setSelection(start + count);
                }
                else {
                    flag[0] = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = newname.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    til.setError(getString(R.string.error_input_empty));
                } else if (s.equals(prefer.getDeviceName())) {
                    dialog.dismiss();
                }else {
                    prefer.setDeviceName(s);
                    BleManager.getInstance().setSlaverName(prefer.getDeviceMac(), s);
                    PreferenceUtil.setObjectToPrefer(LightActivity.this, ConstVal.DEV_PREFER_FILENAME, prefer, prefer.getDeviceMac());
                    dialog.dismiss();
                }
            }
        });
        //        dialog.setView( view );
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.show();
    }

    private void showRetrievePasswordDialog(final int password) {
        //        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(LightActivity.this, R.style.DialogTheme);
        View view = LayoutInflater.from(LightActivity.this).inflate(R.layout.dialog_retrieve_password, null, false);
        builder.setTitle(R.string.retrieve_password);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final Button btn_copy = view.findViewById(R.id.dialog_retrieve_copy);
        final EditText retrieve_key = view.findViewById(R.id.dialog_retrieve_key);
        final TextView retrieve_msg = view.findViewById(R.id.dialog_retrieve_msg);
        Button btn_cancel = view.findViewById(R.id.dialog_retrieve_cancel);
        Button btn_retrieve = view.findViewById(R.id.dialog_retrieve_retrieve);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd = ClipData.newPlainText("device_info", DeviceUtil.getDeviceInfo(mPrefer));
                cm.setPrimaryClip(cd);
                Toast.makeText(LightActivity.this, R.string.retrieve_copy_msg, Toast.LENGTH_LONG)
                     .show();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        btn_retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = retrieve_key.getText().toString().toLowerCase();
                if (TextUtils.isEmpty(key)) {
                    retrieve_msg.setText("");
                } else if (key.equals(Md5Util.encrypt(mAddress).toLowerCase())) {
                    DecimalFormat df = new DecimalFormat("000000");
                    retrieve_msg.setText(getString(R.string.retrieve_psw_is) + df.format(password));
                } else {
                    retrieve_msg.setText(R.string.retrieve_wrong_key);
                }
            }
        });
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.setView( view );
        //        dialog.show();
    }

    private void showPasswordDialog(final int password) {
        if (mPasswordDialog != null && mPasswordDialog.isShowing()) {
            return;
        }
        //        AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_password, null, false);
        builder.setTitle(R.string.input_password);
        builder.setView(view);
        builder.setCancelable(false);
        mPasswordDialog = builder.show();
        final TextInputLayout psw_til = view.findViewById(R.id.psw_til);
        final TextInputEditText psw_password = view.findViewById(R.id.psw_password);
        final Button btn_retrieve = view.findViewById(R.id.psw_retrieve);
        final Button btn_cancel = view.findViewById(R.id.psw_cancel);
        final Button btn_login = view.findViewById(R.id.psw_login);
        btn_retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordDialog.dismiss();
                showRetrievePasswordDialog(password);
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordDialog.dismiss();
                finish();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psw = psw_password.getText().toString();
                if (psw.length() != 6) {
                    psw_til.setError(getString(R.string.error_psw_6_num));
                }
                else {
                    int value = Integer.parseInt(psw);
                    if (value == password) {
                        LightPrefUtil.setLocalPassword(LightActivity.this, mAddress, value);
                        readMfr();
                        mPasswordDialog.dismiss();
                        mCountDownTimer.start();
                    }
                    else {
                        psw_til.setError(getString(R.string.error_password_wrong));
                    }
                }
            }
        });
        //        dialog.setView( view );
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.setCancelable( false );
        //        dialog.show();
    }

    private void modifyPassword(final int psw) {
        final BleListener listener = new BleSimpleListener(mAddress) {
            @Override
            public void onReadPassword(int password) {
                final String msg;
                if (psw == password) {
                    msg = getString(R.string.modify_password_success);
                } else {
                    msg = getString(R.string.modify_password_fail);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LightActivity.this, msg, Toast.LENGTH_SHORT)
                             .show();
                    }
                });
            }
        };
        final CountDownTimer tmr = new CountDownTimer(400, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished < 200) {
                    BleManager.getInstance().readPassword(mAddress);
                }
            }

            @Override
            public void onFinish() {
                BleManager.getInstance().removeBleListener(listener);
            }
        };
        BleManager.getInstance().addBleListener(listener);
        BleManager.getInstance().setPassword(mAddress, psw);
        tmr.start();
    }

    private void showModifyPasswordDialog() {
        //        AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        View view = LayoutInflater.from(this)
                                  .inflate(R.layout.dialog_modify_password, null, false);
        builder.setTitle(getString(R.string.modify_password));
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final TextInputLayout modify_til1 = view.findViewById(R.id.modify_psw_til1);
        final TextInputLayout modify_til2 = view.findViewById(R.id.modify_psw_til2);
        final TextInputEditText modify_new = view.findViewById(R.id.modify_psw_new);
        final TextInputEditText modify_confirm = view.findViewById(R.id.modify_psw_confirm);
        Button btn_cancel = view.findViewById(R.id.modify_psw_cancel);
        Button btn_modify = view.findViewById(R.id.modify_psw_modify);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psw1 = modify_new.getText()
                                        .toString();
                String psw2 = modify_confirm.getText()
                                            .toString();
                if (psw1.length() != 6) {
                    modify_til1.setError(getString(R.string.error_psw_6_num));
                    return;
                }
                if (psw2.length() != 6) {
                    modify_til2.setError(getString(R.string.error_psw_6_num));
                    return;
                }
                if (psw1.equals(psw2)) {
                    int value = Integer.parseInt(psw1);
                    modifyPassword(value);
                    dialog.dismiss();
                }
                else {
                    modify_til2.setError(getString(R.string.error_password_mismatch));
                }
            }
        });
        modify_new.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    modify_til1.setError(null);
                } else {
                    String text = modify_new.getText().toString();
                    if (TextUtils.isEmpty(text) || text.length() < 6) {
                        modify_til1.setError(getString(R.string.error_psw_6_num));
                    }
                }
            }
        });
        //        dialog.setView( view );
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.show();
    }

    private void startOtaActivity() {
        BleManager.getInstance().disconnectDevice(mAddress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (BleManager.getInstance().isConnected(mAddress));
                final Intent intent = new Intent(LightActivity.this, BleOTAActivity.class);
                intent.putExtra("devid", mPrefer.getDevId());
                intent.putExtra("name", mPrefer.getDeviceName());
                intent.putExtra("address", mPrefer.getDeviceMac());
                intent.putExtra("mode", false);
                startActivity(intent);
                finish();
            }
        }).start();
    }

    @Override
    public void onRetryClick() {
        getDeviceData();
    }
}
