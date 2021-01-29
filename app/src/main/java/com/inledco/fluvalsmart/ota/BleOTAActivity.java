package com.inledco.fluvalsmart.ota;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.liruya.tuner168blemanager.BleManager;

public class BleOTAActivity extends BaseActivity implements IOTAView {

    private Toolbar ota_toolbar;
    private TextView ota_tv_device_name;
    private TextView ota_tv_device_version;
    private TextView ota_tv_remote_version;
    private NestedScrollView ota_nsv;
    private TextView ota_tv_msg;
    private Button ota_check_upgrade;
    private MenuItem menu_connect_status;

    private short mDevid;
    private String mName;
    private String mAddress;
    private boolean mTestMode;
    private OTAPresenter mPresenter;

    private StringBuffer mMessage;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleota);

        Intent intent = getIntent();
        if (intent != null) {
            mDevid = intent.getShortExtra("devid", (short) 0);
            mName = intent.getStringExtra("name");
            mAddress = intent.getStringExtra("address");
            mTestMode = intent.getBooleanExtra("mode", false);
        }
        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ota, menu);
        menu_connect_status = menu.findItem(R.id.menu_connect_status);
        if (BleManager.getInstance()
                      .isConnected(mAddress))
        {
            menu_connect_status.setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
            menu_connect_status.setChecked(true);
        }
        else {
            menu_connect_status.setIcon(R.drawable.ic_bluetooth_disabled_grey_500_24dp);
            menu_connect_status.setChecked(false);
        }
        return true;
    }

    @Override
    protected void initView() {
        ota_toolbar = findViewById(R.id.ota_toolbar);
        ota_tv_device_name = findViewById(R.id.ota_tv_device_name);
        ota_tv_device_version = findViewById(R.id.ota_tv_device_version);
        ota_tv_remote_version = findViewById(R.id.ota_tv_remote_version);
        ota_nsv = findViewById(R.id.ota_nsv);
        ota_tv_msg = findViewById(R.id.ota_tv_msg);
        ota_check_upgrade = findViewById(R.id.ota_check_upgrade);

//        ota_check_upgrade.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upgrade_white_32dp, 0, 0, 0);
        setSupportActionBar(ota_toolbar);
        ota_tv_msg.setKeepScreenOn(true);
    }

    @Override
    protected void initEvent() {
        ota_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPresenter.isProcessing()) {
                    finish();
                }
            }
        });
        ota_check_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPresenter.isProcessing()) {
                    if (NetUtil.isNetworkAvailable(BleOTAActivity.this)) {
                        ota_tv_device_version.setText(R.string.device_frimware_version);
                        ota_tv_remote_version.setText(R.string.latest_firmware_version);
                        mMessage = new StringBuffer();
                        ota_tv_msg.setText("");
                        mPresenter.checkUpdate(false);
                    }
                    else {
                        mMessage = new StringBuffer();
                        ota_tv_msg.setText(R.string.ota_network_unavailable);
                        showNonetDialog();
                    }
                }
            }
        });
    }

    @Override
    protected void initData() {
        ota_tv_device_name.setText(mName);
        mMessage = new StringBuffer();
        mPresenter = new OTAPresenter(this, this, mDevid, mAddress, "", mTestMode);
        mPresenter.start();
        ota_check_upgrade.performClick();
//        if (NetUtil.isNetworkAvailable(this)) {
//            //            new Handler().postDelayed(new Runnable() {
//            //                @Override
//            //                public void run() {
//            //                    mPresenter.checkUpdate(false);
//            //                }
//            //            }, 500);
//            //            mPresenter.checkUpdate(false);
//        }
//        else {
//            ota_tv_msg.setText(R.string.ota_network_unavailable);
//        }
    }

    public void showDeviceVersion(final String version) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ota_tv_device_version.setText(getString(R.string.device_frimware_version) + version);
            }
        });
    }

    public void showRemoteVersion(final String version) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ota_tv_remote_version.setText(getString(R.string.latest_firmware_version) + version);
            }
        });
    }

    public void showDeviceConnected() {
        if (menu_connect_status != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu_connect_status.setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
                    menu_connect_status.setChecked(true);
                }
            });
        }
    }

    public void showDeviceDisconnected() {
        if (menu_connect_status != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu_connect_status.setIcon(R.drawable.ic_bluetooth_disabled_grey_500_24dp);
                    menu_connect_status.setChecked(false);
                }
            });
        }
    }

    @Override
    public void showMessage(final String msg) {
        mMessage.append(msg)
                .append("\r\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ota_tv_msg.setText(mMessage);
                ota_nsv.post(new Runnable() {
                    @Override
                    public void run() {
                        ota_nsv.fullScroll(NestedScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    public void showUpgradeConfirmDialog(String msg) {
//        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.ota_upgradable);
        builder.setMessage(msg);
        builder.setNegativeButton(R.string.ota_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.stopProcess();
            }
        });
        builder.setPositiveButton(R.string.ota_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.downloadFirmware();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        lp.width = dm.widthPixels;
        window.setAttributes(lp);
    }

    @Override
    public void showUpgradeProgress(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ota_tv_msg.setText(mMessage);
                ota_tv_msg.append(msg + "\r\n");
                ota_nsv.post(new Runnable() {
                    @Override
                    public void run() {
                        ota_nsv.fullScroll(NestedScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    public void showRepowerDialog() {
//        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        final int[] count = new int[]{20};
        View view = LayoutInflater.from(this)
                                  .inflate(R.layout.dialog_repower, null, false);
//        builder.setTitle(R.string.ota_wait_title);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        lp.width = dm.widthPixels;
        window.setAttributes(lp);

        final Button btn = view.findViewById(R.id.dialog_repower_next);
        btn.setText(getString(R.string.ota_wait_msg) + " (20) ");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mPresenter.checkUpdate(true);
            }
        });
        CountDownTimer timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                count[0]--;
                if (count[0] > 0) {
                    btn.setText(getString(R.string.ota_wait_msg) + " (" + count[0] + ") ");
                }
            }

            @Override
            public void onFinish() {
                btn.setText(R.string.next);
                btn.setEnabled(true);
            }
        };

        //        dialog.setView(view);
        //        dialog.setCanceledOnTouchOutside(false);
        //        dialog.show();
        timer.start();
    }

    @Override
    public void showSuccessDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        builder.setTitle(R.string.title_upgrade_success);
        builder.setIcon(R.drawable.ic_check_green_32dp);
        builder.setMessage(R.string.upgrade_success_msg);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void showErrorDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        builder.setTitle(R.string.title_upgrade_failed);
        builder.setIcon(R.drawable.ic_error_red_32dp);
        builder.setMessage(R.string.upgrade_failed_msg);
        builder.setNegativeButton(R.string.close, null);
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ota_check_upgrade.performClick();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void showUptodateDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        builder.setTitle(R.string.uptodate);
        builder.setMessage(R.string.ota_firmware_newest);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showNonetDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
        builder.setTitle(R.string.title_network_unavailable);
        builder.setMessage(R.string.ota_network_unavailable);
        builder.setPositiveButton(R.string.close, null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (mPresenter.isProcessing()) {
            return;
        }
        super.onBackPressed();
    }
}
