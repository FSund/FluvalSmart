package com.inledco.fluvalsmart.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.fragment.DeviceFragment;
import com.inledco.fluvalsmart.fragment.NewsFragment;
import com.inledco.fluvalsmart.fragment.UserFragment;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.liruya.tuner168blemanager.BleManager;

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
        BleManager.getInstance().unbindService(getApplicationContext());
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
        if (requestCode == PERMISSON_REQUEST_COARSE_CODE && Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0]))
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startScanActivity();
            }
            else
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == false) {
                    showPermissionDialog();
                } else {
                    Toast.makeText(MainActivity.this, R.string.snackbar_coarselocation_denied, Toast.LENGTH_SHORT)
                         .show();
                }
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
                if (BleManager.getInstance().checkLocationPermission(MainActivity.this))
                {
                    startScanActivity();
                }
                else
                {
                    BleManager.getInstance().requestLocationPermission(MainActivity.this, PERMISSON_REQUEST_COARSE_CODE);
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
        BleManager.getInstance().bindService( getApplicationContext() );
        if ( BleManager.getInstance().checkBleSupported( this ) )
        {
            if ( BleManager.getInstance().isBluetoothEnabled() || ( Setting.isAutoTurnonBle( MainActivity.this ) && BleManager.getInstance().autoOpenBluetooth()) )
            {
            }
            else
            {
//                Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
//                startActivityForResult( intent, BLUETOOTH_REQUEST_ENABLE_CODE );
                BleManager.getInstance().requestBluetoothEnable(MainActivity.this, BLUETOOTH_REQUEST_ENABLE_CODE);
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

    private void showPermissionDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(MainActivity.this, R.style.DialogTheme);
        builder.setTitle(R.string.turnon_location_permission);
        builder.setMessage(R.string.msg_turnon_location_permission);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.show();
    }
}
