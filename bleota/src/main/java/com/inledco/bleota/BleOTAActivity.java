package com.inledco.bleota;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liruya.tuner168blemanager.BleManager;

public class BleOTAActivity extends AppCompatActivity implements IOTAView
{
    private static final String TAG = "BleOTAActivity";

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
            mTestMode = intent.getBooleanExtra("mode", false);
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
        if ( BleManager.getInstance().isConnected(mAddress) )
        {
            menu_connect_status.setIcon( R.drawable.ic_bluetooth_connected_white_36dp );
            menu_connect_status.setChecked( true );
        }
        else
        {
            menu_connect_status.setIcon( R.drawable.ic_bluetooth_disabled_grey_500_36dp );
            menu_connect_status.setChecked( false );
        }
        return true;
    }

    private void initView ()
    {
        ota_toolbar = findViewById( R.id.ota_toolbar );
        ota_tv_device_name = findViewById( R.id.ota_tv_device_name );
        ota_tv_device_version = findViewById( R.id.ota_tv_device_version );
        ota_tv_remote_version = findViewById( R.id.ota_tv_remote_version );
        ota_nsv = findViewById( R.id.ota_nsv );
        ota_tv_msg = findViewById( R.id.ota_tv_msg );
        ota_check_upgrade = findViewById( R.id.ota_check_upgrade );

        setSupportActionBar( ota_toolbar );
        ota_tv_msg.setKeepScreenOn( true );
    }

    private void initEvent()
    {
        ota_toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isProcessing() )
                {
                    finish();
                }
            }
        } );
        ota_check_upgrade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( !mPresenter.isProcessing() )
                {
                    if ( NetUtil.isNetworkAvailable( BleOTAActivity.this ) )
                    {
                        ota_tv_device_version.setText( R.string.device_frimware_version );
                        ota_tv_remote_version.setText( R.string.latest_firmware_version );
                        mMessage = new StringBuffer();
                        ota_tv_msg.setText( "" );
                        mPresenter.checkUpdate();
                    }
                    else
                    {
                        mMessage = new StringBuffer();
                        ota_tv_msg.setText( R.string.ota_network_unavailable );
                    }
                }
            }
        } );
    }

    private void initData()
    {
        ota_tv_device_name.setText( mName );
        mMessage = new StringBuffer();
        mPresenter = new OTAPresenter( this, this, mDevid, mAddress, "", mTestMode );
        mPresenter.start();
        if ( NetUtil.isNetworkAvailable( this ) )
        {
            mPresenter.checkUpdate();
        }
        else
        {
            ota_tv_msg.setText( R.string.ota_network_unavailable );
        }
    }

    public void showDeviceVersion(String version)
    {
        ota_tv_device_version.setText( getString(R.string.device_frimware_version) + version );
    }

    public void showRemoteVersion(String version)
    {
        ota_tv_remote_version.setText( getString(R.string.latest_firmware_version) + version );
    }

    public void showDeviceConnected ()
    {
        if ( menu_connect_status != null )
        {
            menu_connect_status.setIcon( R.drawable.ic_bluetooth_connected_white_36dp );
            menu_connect_status.setChecked( true );
        }
    }

    public void showDeviceDisconnected ()
    {
        if ( menu_connect_status != null )
        {
            menu_connect_status.setIcon( R.drawable.ic_bluetooth_disabled_grey_500_36dp );
            menu_connect_status.setChecked( false );
        }
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
                ota_nsv.post( new Runnable() {
                    @Override
                    public void run()
                    {
                        ota_nsv.fullScroll( NestedScrollView.FOCUS_DOWN );
                    }
                } );
            }
        } );
    }

    @Override
    public void showUpgradeConfirmDialog (String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( R.string.ota_upgradable );
        builder.setMessage( msg );
        builder.setNegativeButton( R.string.ota_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {
                mPresenter.stopProcess();
            }
        } );
        builder.setPositiveButton( R.string.ota_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick ( DialogInterface dialog, int which )
            {
                mPresenter.downloadFirmware();
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside( false );
        dialog.setCancelable( false );
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
                ota_nsv.post( new Runnable() {
                    @Override
                    public void run()
                    {
                        ota_nsv.fullScroll( NestedScrollView.FOCUS_DOWN );
                    }
                } );
            }
        } );
    }

    @Override
    public void showRepowerDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        final AlertDialog dialog = builder.create();
        final int[] count = new int[]{ 20 };
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_repower, null, false );
        final Button btn = view.findViewById( R.id.dialog_repower_next );
        btn.setText( getString( R.string.next ) + " ( 20 ) " );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                dialog.dismiss();
                mPresenter.checkUpdate();
            }
        } );
        CountDownTimer timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick( long millisUntilFinished )
            {
                count[0]--;
                if (count[0] > 0)
                {
                    btn.setText(getString(R.string.next) + " ( " + count[0] + " ) ");
                }
            }

            @Override
            public void onFinish()
            {
                btn.setText( R.string.next );
                btn.setEnabled(true);
            }
        };
        dialog.setTitle( R.string.ota_wait_title );
        dialog.setView( view );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setCancelable( false );
        dialog.show();
        timer.start();
    }

    @Override
    public void onBackPressed ()
    {
        if ( mPresenter.isProcessing() )
        {
            return;
        }
        super.onBackPressed();
    }

}
