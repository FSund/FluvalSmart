package com.inledco.fluvalsmart.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inledco.blemanager.BleCommunicateListener;
import com.inledco.blemanager.BleManager;
import com.inledco.blemanager.LogUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.bean.Light;
import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.fragment.DataInvalidFragment;
import com.inledco.fluvalsmart.fragment.LightAutoFragment;
import com.inledco.fluvalsmart.fragment.LightManualFragment;
import com.inledco.fluvalsmart.fragment.RGBWManualFragment;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.PreferenceUtil;

import java.util.ArrayList;

public class LightActivity extends BaseActivity implements DataInvalidFragment.OnRetryClickListener
{
    private CheckableImageButton light_sw_auto;
    private Toolbar light_toolbar;
    private ProgressDialog mProgressDialog;

    private DevicePrefer mPrefer;
    private Light mLight;

    private CountDownTimer mCountDownTimer;

    private BleCommunicateListener mCommunicateListener;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_light );
        Intent intent = getIntent();
        mPrefer = (DevicePrefer) intent.getSerializableExtra( "DevicePrefer" );

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onStart ()
    {
        super.onStart();
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
    }

    @Override
    protected void onStop ()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        BleManager.getInstance().removeBleCommunicateListener( mCommunicateListener );
        mCommunicateListener = null;
        BleManager.getInstance().disConnectAll();
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_device, menu );
        MenuItem menu_device_edit = menu.findItem( R.id.menu_device_edit );
        MenuItem menu_device_find = menu.findItem( R.id.menu_device_find );
        menu_device_edit.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick ( MenuItem menuItem )
            {
                showRenameDialog( mPrefer );
                return false;
            }
        } );
        menu_device_find.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick ( MenuItem menuItem )
            {
                CommUtil.findDevice( mLight.getDevicePrefer().getDeviceMac() );
                return false;
            }
        } );
        return true;
    }

    @Override
    protected void initView ()
    {
        light_sw_auto = (CheckableImageButton) findViewById( R.id.light_sw_auto );
        light_toolbar = (Toolbar) findViewById( R.id.light_toolbar );
        light_toolbar.setTitle( mPrefer.getDeviceName() );
        setSupportActionBar( light_toolbar );

        mProgressDialog = new ProgressDialog( this );
        mProgressDialog.setCanceledOnTouchOutside( false );
        mProgressDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel ( DialogInterface dialog )
            {
                BleManager.getInstance().disconnectDevice( mPrefer.getDeviceMac() );
            }
        } );
    }

    @Override
    protected void initData ()
    {
        mCountDownTimer = new CountDownTimer(2048, 1024) {
            @Override
            public void onTick ( long millisUntilFinished )
            {

            }

            @Override
            public void onFinish ()
            {
                if ( mProgressDialog.isShowing() )
                {
                    mProgressDialog.dismiss();
                }
                light_sw_auto.setVisibility( View.GONE );
                getSupportFragmentManager().beginTransaction()
                                           .replace( R.id.light_fl_show, DataInvalidFragment.newInstance( mPrefer.getDeviceMac() ) )
                                           .commit();
            }
        };
        mCommunicateListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( final String mac )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
                            CommUtil.syncDeviceTime( mac );
                            mCountDownTimer.start();
                        }
                    } );
                }
            }

            @Override
            public void onDataInvalid ( String mac )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            if ( mProgressDialog.isShowing() )
                            {
                                mProgressDialog.dismiss();
                            }
                            light_sw_auto.setVisibility( View.GONE );
                            getSupportFragmentManager().beginTransaction()
                                                       .replace( R.id.light_fl_show, DataInvalidFragment.newInstance( mPrefer.getDeviceMac() ) )
                                                       .commit();
                        }
                    } );
                }
            }

            @Override
            public void onReadMfr ( String mac, String s )
            {

            }

            @Override
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
                    decodeReceiveData( mac, list );
                }
            }
        };
        BleManager.getInstance().addBleCommunicateListener( mCommunicateListener );
        mLight = new Light( mPrefer, false, false, null, null );
        light_toolbar.setTitle( mPrefer.getDeviceName() );
        light_sw_auto.setVisibility( View.GONE );
        getSupportFragmentManager().beginTransaction()
                                   .replace( R.id.light_fl_show, DataInvalidFragment.newInstance( mPrefer.getDeviceMac() ) )
                                   .commit();
        getDeviceData();
    }

    @Override
    protected void initEvent ()
    {
        light_toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View view )
            {
                finish();
            }
        } );

        light_sw_auto.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {
                if ( light_sw_auto.isChecked() )
                {
                    CommUtil.setManual( mLight.getDevicePrefer().getDeviceMac() );
                }
                else
                {
                    CommUtil.setAuto( mLight.getDevicePrefer().getDeviceMac() );
                }
            }
        } );
    }

    public void getDeviceData()
    {
        if ( !BleManager.getInstance().isConnected( mPrefer.getDeviceMac() ) )
        {
            mProgressDialog.setMessage( getString( R.string.msg_connecting_device ) );
            mProgressDialog.show();
            BleManager.getInstance().connectDevice( mPrefer.getDeviceMac() );
        }
        else if ( BleManager.getInstance().isDataValid( mPrefer.getDeviceMac() ) )
        {
            mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
            mProgressDialog.show();
            CommUtil.syncDeviceTime( mPrefer.getDeviceMac() );
            mCountDownTimer.start();
        }
    }

    private void decodeReceiveData ( final String mac, ArrayList< Byte > list )
    {
        Object object = CommUtil.decodeLight( list, mPrefer.getDevId() );
        if ( object != null )
        {
            final FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            if ( object instanceof LightAuto )
            {
                mCountDownTimer.cancel();
                mLight.setAuto( true );
                mLight.setLightAuto( (LightAuto) object );
                runOnUiThread( new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        mProgressDialog.dismiss();
                        if ( light_sw_auto.getVisibility() != View.VISIBLE || !light_sw_auto.isChecked() )
                        {
                            beginTransaction.replace( R.id.light_fl_show,
                                                      LightAutoFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightAuto() ) )
                                            .commit();
                        }
                        light_sw_auto.setVisibility( View.VISIBLE );
                        light_sw_auto.setChecked( true );
                    }
                } );
            }
            else if ( object instanceof LightManual )
            {
                mCountDownTimer.cancel();
                mLight.setAuto( false );
                mLight.setLightManual( (LightManual) object );
                runOnUiThread( new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        mProgressDialog.dismiss();
                        if ( light_sw_auto.getVisibility() != View.VISIBLE || light_sw_auto.isChecked() )
                        {
                            if ( mPrefer.getDevId() == DeviceUtil.LIGHT_ID_RGBW
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_600
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_900
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_1200
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_380
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_530
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_835
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_990
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_750
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_1150 )
                            {
                                beginTransaction.replace( R.id.light_fl_show,
                                                          RGBWManualFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightManual() ) )
                                                .commit();
                            }
                            else
                            {
                                beginTransaction.replace( R.id.light_fl_show,
                                                          LightManualFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightManual() ) )
                                                .commit();
                            }
                        }
                        light_sw_auto.setVisibility( View.VISIBLE );
                        light_sw_auto.setChecked( false );
                    }
                } );
            }
        }
    }

    private void showRenameDialog ( final DevicePrefer prefer )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        final AlertDialog dialog = builder.create();
        dialog.setTitle( R.string.rename_device );
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_rename, null );
        Button btn_cancel = (Button) view.findViewById( R.id.rename_cancel );
        Button btn_rename = (Button) view.findViewById( R.id.rename_confirm );
        final EditText newname = (EditText) view.findViewById( R.id.rename_newname );
        newname.setText( prefer.getDeviceName() );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View view )
            {
                dialog.dismiss();
            }
        } );
        btn_rename.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View view )
            {
                if ( TextUtils.isEmpty( newname.getText().toString() ) )
                {
                    newname.setError( getString( R.string.error_input_empty ) );
                }
                else if ( newname.getText().toString().equals( prefer.getDeviceName() ) )
                {
                    dialog.dismiss();
                }
                else
                {
                    prefer.setDeviceName( newname.getText().toString() );
                    BleManager.getInstance().setSlaverName( prefer.getDeviceMac(), prefer.getDeviceName() );
                    PreferenceUtil.setObjectToPrefer( LightActivity.this, ConstVal.DEV_PREFER_FILENAME, prefer, prefer.getDeviceMac() );
                    dialog.dismiss();
                }
            }
        } );
        dialog.setView( view );
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    @Override
    public void onRetryClick ()
    {
        LogUtil.d( TAG, "onRetryClick: " );
        getDeviceData();
    }
}
