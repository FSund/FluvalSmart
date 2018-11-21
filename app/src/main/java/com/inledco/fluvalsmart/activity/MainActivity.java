package com.inledco.fluvalsmart.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.inledco.blemanager.BleManager;
import com.inledco.blemanager.LogUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.fragment.DeviceFragment;
import com.inledco.fluvalsmart.fragment.NewsFragment;
import com.inledco.fluvalsmart.fragment.UserFragment;
import com.inledco.fluvalsmart.prefer.Setting;

public class MainActivity extends BaseActivity
{
    private final int BLUETOOTH_REQUEST_ENABLE_CODE = 1;
    private final int PERMISSON_REQUEST_COARSE_CODE = 2;
    private final int SCAN_CODE = 3;

    private Toolbar toolbar;
    private MenuItem menuItemBleSearch;
    private BottomNavigationView main_bottom_navigation;

    //双击back退出标志位
    private boolean mExiting;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onDestroy ()
    {
        BleManager.getInstance().unbindService( this );
        BleManager.getInstance().disConnectAll();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode )
        {
            case BLUETOOTH_REQUEST_ENABLE_CODE:
                if ( resultCode == Activity.RESULT_OK )
                {
//                    showUpgradeTip();
                }
                else
                {
                    Toast.makeText( MainActivity.this, R.string.snackbar_bluetooth_denied, Toast.LENGTH_LONG )
                         .show();
                }
                break;
            case SCAN_CODE:
                if ( resultCode == SCAN_CODE )
                {
                    Setting.setScanTip( MainActivity.this );
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if ( requestCode == PERMISSON_REQUEST_COARSE_CODE
             && permissions[0].equals( Manifest.permission.ACCESS_COARSE_LOCATION) )
        {
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED )
            {
                startScanActivity();
            }
            else
            {
                Toast.makeText( MainActivity.this, R.string.snackbar_coarselocation_denied, Toast.LENGTH_SHORT )
                     .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        menuItemBleSearch = menu.findItem( R.id.menu_search_ble );
        menuItemBleSearch.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick ( MenuItem item )
            {
                LogUtil.e( TAG, "onMenuItemClick: " + Build.VERSION.SDK_INT );
                if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                     ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
                {
                    startScanActivity();
                }
                else
                {
                    ActivityCompat.requestPermissions( MainActivity.this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION },
                                                       PERMISSON_REQUEST_COARSE_CODE );
                }
                return false;
            }
        } );
        return true;
    }

    @Override
    protected void initView ()
    {
        main_bottom_navigation = findViewById( R.id.main_bottom_navigation );
        toolbar = findViewById( R.id.toolbar );
        toolbar.setTitle( "" );
        setSupportActionBar( toolbar );
    }

    @Override
    protected void initData()
    {
        BleManager.getInstance().bindService( this );
        if ( BleManager.getInstance().checkBleSupported( this ) )
        {
            if ( BleManager.getInstance().isBluetoothEnabled() || ( Setting.isAutoTurnonBle( MainActivity.this ) && BleManager.getInstance().autoOpenBluetooth()) )
            {
//                showUpgradeTip();
            }
            else
            {
                Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                startActivityForResult( intent, BLUETOOTH_REQUEST_ENABLE_CODE );
            }
        }
        else
        {
            Toast.makeText( this, R.string.ble_no_support, Toast.LENGTH_SHORT )
                 .show();
            finish();
            return;
        }
        getSupportFragmentManager().beginTransaction().replace( R.id.main_fl_show, new DeviceFragment() ).commit();
    }

    @Override
    protected void initEvent()
    {
        main_bottom_navigation.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected ( @NonNull MenuItem item )
            {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch ( item.getItemId() )
                {
                    case R.id.menu_btm_device:
                        transaction.replace( R.id.main_fl_show, new DeviceFragment() ).commit();
                        menuItemBleSearch.setVisible( true );
                        break;
                    case R.id.menu_btm_news:
                        transaction.replace( R.id.main_fl_show, new NewsFragment() ).commit();
                        menuItemBleSearch.setVisible( false );
                        break;
                    case R.id.menu_btm_setting:
                        transaction.replace( R.id.main_fl_show, new UserFragment() ).commit();
                        menuItemBleSearch.setVisible( false );
                        break;
                }
                return true;
            }
        } );
    }

    /**
     * back按键关闭app时 弹出确认关闭蓝牙dialog
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown ( int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if ( !mExiting )
            {
                mExiting = true;
                new Handler().postDelayed( new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        mExiting = false;
                    }
                }, 1500 );
                Toast.makeText( MainActivity.this, R.string.exit_app_tips, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                //如果退出时不提示 且设置为退出关闭BLE
                if ( Setting.isExitTurnoffBle( MainActivity.this ) )
                {
                    BleManager.getInstance().closeBluetooth();
                }
                finish();
            }
        }
        return true;
    }

    private void startScanActivity()
    {
        Intent intent = new Intent( this, ScanActivity.class );
        startActivityForResult( intent, SCAN_CODE );
    }

    private void showUpgradeTip()
    {
        if (Setting.hasUpgradeTip(this))
        {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tip");
        builder.setView(R.layout.dialog_tip_upgrade);
        builder.setPositiveButton(R.string.dialog_ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Setting.setUpgradeTip(MainActivity.this);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
