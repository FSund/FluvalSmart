package com.inledco.bleota;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inledco.blemanager.BleManager;

import java.text.DecimalFormat;

public class BleOTAActivity extends AppCompatActivity implements OTAContract.View
{
    private static final String TAG = "BleOTAActivity";

    private Toolbar ota_toolbar;
    private TextView ota_tv_device_name;
    private TextView ota_tv_device_version;
    private TextView ota_tv_remote_version;
    private TextView ota_tv_msg;
    private Button ota_upgrade;
    private Button ota_download;
    private Button ota_check_upgrade;
    private MenuItem menu_connect_status;

    private short mDevid;
    private String mName;
    private String mAddress;
    private OTAPresenter mPresenter;

    private StringBuffer mMessage;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_bleota );

        Intent intent = getIntent();
        if ( intent != null )
        {
            mDevid = intent.getShortExtra( "devid", (short) 0 );
            mName = intent.getStringExtra( "name" );
            mAddress = intent.getStringExtra( "address" );
        }
        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        mPresenter.stop();
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_ota, menu );
        menu_connect_status = menu.findItem( R.id.menu_connect_status );
        menu_connect_status.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick ( MenuItem item )
            {
                if ( !item.isChecked() )
                {
                    BleManager.getInstance().connectDevice( mAddress );
                    showMessage( getString( R.string.ota_connecting ) );
                }
                return false;
            }
        } );
        return true;
    }

    private void initView ()
    {
        ota_toolbar = (Toolbar) findViewById( R.id.ota_toolbar );
        ota_tv_device_name = (TextView) findViewById( R.id.ota_tv_device_name );
        ota_tv_device_version = (TextView) findViewById( R.id.ota_tv_device_version );
        ota_tv_remote_version = (TextView) findViewById( R.id.ota_tv_remote_version );
        ota_tv_msg = (TextView) findViewById( R.id.ota_tv_msg );
        ota_upgrade = (Button) findViewById( R.id.ota_upgrade );
        ota_download = (Button) findViewById( R.id.ota_download );
        ota_check_upgrade = (Button) findViewById( R.id.ota_check_upgrade );

        setSupportActionBar( ota_toolbar );
        ota_tv_msg.setKeepScreenOn( true );
    }

    private void initEvent()
    {
        ota_toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isUpgrading() )
                {
                    finish();
                }
            }
        } );
        ota_check_upgrade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isUpgrading() )
                {
                    ota_tv_device_version.setText( "Device Firmware Version: " );
                    ota_tv_remote_version.setText( "Remote Firmware Version: " );
                    mPresenter.checkRemoteVersion();
                    mPresenter.checkDeviceVersion();
                }
            }
        } );

        ota_download.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isUpgrading() )
                {
                    mPresenter.downloadFirmware();
                }
            }
        } );

        ota_upgrade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isUpgrading() )
                {
                    showUpgradeWarning();
                }
            }
        } );
    }

    private void initData()
    {
        ota_tv_device_name.setText( mName );
        mMessage = new StringBuffer();
        setPresenter( new OTAPresenter( this, mDevid, mAddress, "" ) );
        mPresenter.start();
    }

    @Override
    public void setPresenter ( OTAContract.Presenter presenter )
    {
        mPresenter = (OTAPresenter) presenter;
    }

    @Override
    public Context getMvpContext ()
    {
        return this;
    }

    @Override
    public void onDataValid ()
    {
        menu_connect_status.setIcon( R.drawable.ic_bluetooth_connected_white_36dp );
        menu_connect_status.setChecked( true );
        showMessage( getString( R.string.ota_connect_success ) );
    }

    @Override
    public void onDataInvalid ()
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                menu_connect_status.setIcon( R.drawable.ic_bluetooth_disabled_grey_500_36dp );
                menu_connect_status.setChecked( false );
                showMessage( getString( R.string.ota_disconnect ) );
            }
        } );
    }

    @Override
    public void onCheckRemoteSuccess ( final int major_version, final int minor_version )
    {
        final DecimalFormat df = new DecimalFormat( "00" );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_remote_version.setText( "Remote Firmware Version: " + major_version + "." + df.format( minor_version ) );
            }
        } );
    }

    @Override
    public void onCheckRemoteFailure ()
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_remote_version.setText( "Remote Firmware Version: Failed" );
            }
        } );
    }

    @Override
    public void onCheckDeviceSuccess ( final int major_version, final int minor_version )
    {
        final DecimalFormat df = new DecimalFormat( "00" );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_device_version.setText( "Device Firmware Version: " + major_version + "." + df.format( minor_version ) );
            }
        } );
    }

    @Override
    public void onCheckDeviceFailure ()
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_device_version.setText( "Device Firmware Version: Failed" );
            }
        } );
    }

    @Override
    public void onFirmwareExists ( final boolean exist )
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                if ( exist )
                {
                    ota_download.setText( getString( R.string.ota_redownload )  );
                }
                else
                {
                    ota_download.setText( getString( R.string.ota_download )  );
                }
                ota_download.setClickable( true );
            }
        } );
    }

    @Override
    public void onDownloadError ()
    {
        showMessage( getString( R.string.ota_download_failed ) );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_download.setEnabled( true );
            }
        } );
    }

    @Override
    public void onDownloadProgress ( long total, long current )
    {
        float percent = (float) current/total;
        DecimalFormat df = new DecimalFormat( "0.0%" );
        showUpgradeProgress( df.format( percent ) + "\r\n" );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_download.setEnabled( false );
            }
        } );

    }

    @Override
    public void onDownloadSuccess ()
    {
        showMessage( getString( R.string.ota_download_success ) );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_download.setEnabled( true );
                ota_download.setText( getString( R.string.ota_redownload ) );
            }
        } );
    }

    @Override
    public void onConvertFirmwareSuccess ()
    {

    }

    @Override
    public void onConvertFirmwareError ( String msg )
    {

    }

    @Override
    public void onEnterBootloader ()
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_msg.setText( getString( R.string.ota_enter_bootloader ) );
            }
        } );
    }

    @Override
    public void onResetToBootloader ()
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_msg.setText( getString( R.string.ota_reset_tobootloader )  );
            }
        } );
    }

    @Override
    public void showMessage ( final String msg )
    {
        mMessage.append( msg ).append( "\r\n" );
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_msg.setText( mMessage );
            }
        } );
    }

    @Override
    public void showUpgradeWarning ()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( R.string.ota_warning_title );
        builder.setIcon( R.drawable.ic_warning_yellow_500_48dp );
        builder.setMessage( R.string.ota_warning_msg );
        builder.setNegativeButton( R.string.ota_cancel, null );
        builder.setPositiveButton( R.string.ota_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick ( DialogInterface dialog, int which )
            {
                mMessage = new StringBuffer();
                ota_tv_msg.setText( "" );
                mPresenter.isUpgradable();
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    @Override
    public void showUpgradeProgress ( final String msg )
    {
        runOnUiThread( new Runnable() {
            @Override
            public void run ()
            {
                ota_tv_msg.setText( mMessage  );
                ota_tv_msg.append( msg + "\r\n" );
            }
        } );
    }

    @Override
    public void onBackPressed ()
    {
        if ( mPresenter.isUpgrading() )
        {
            return;
        }
        super.onBackPressed();
    }
}
