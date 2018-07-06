package com.inledco.fluvalsmart.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.SharedPreferencesCompat;
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

//    private boolean mModifyPswFlag = false;
//    private int mRemotePassword = -1;

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
        if ( mCountDownTimer != null )
        {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_device, menu );
        MenuItem menu_device_edit = menu.findItem( R.id.menu_device_edit );
        MenuItem menu_device_find = menu.findItem( R.id.menu_device_find );
        MenuItem menu_device_modify_psw = menu.findItem( R.id.menu_device_modify_psw );
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
        menu_device_modify_psw.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick ( MenuItem item )
            {
                showModifyPasswordDialog();
                return false;
            }
        } );
        return true;
    }

    @Override
    protected void initView ()
    {
        light_sw_auto = findViewById( R.id.light_sw_auto );
        light_toolbar = findViewById( R.id.light_toolbar );
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
                runOnUiThread( new Runnable() {
                    @Override
                    public void run ()
                    {
                        if ( mProgressDialog.isShowing() )
                        {
                            mProgressDialog.dismiss();
                        }
                        light_sw_auto.setVisibility( View.GONE );
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace( R.id.light_fl_show, DataInvalidFragment.newInstance( mPrefer.getDeviceMac() ) )
                          .commitAllowingStateLoss();
                    }
                } );

            }
        };
        mCommunicateListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( final String mac )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
//                    mModifyPswFlag = false;
//                    BleManager.getInstance().readPassword( mac );
//                    runOnUiThread( new Runnable() {
//                        @Override
//                        public void run ()
//                        {
//                            mProgressDialog.setMessage( getString( R.string.logging ) );
//                            mCountDownTimer.start();
//                        }
//                    } );
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            CommUtil.syncDeviceTime( mac );
                            mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
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
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace( R.id.light_fl_show, DataInvalidFragment.newInstance( mPrefer.getDeviceMac() ) )
                              .commitAllowingStateLoss();
                        }
                    } );
                }
            }

            @Override
            public void onReadMfr ( String mac, String s )
            {

            }

            @Override
            public void onReadPassword ( final String mac, final int psw )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
//                    if ( mModifyPswFlag )
//                    {
//                        mModifyPswFlag = false;
//                        mRemotePassword = psw;
//                        return;
//                    }
//                    final int password = getLocalPassword( mac );
//                    runOnUiThread( new Runnable() {
//                        @Override
//                        public void run ()
//                        {
//                            if ( psw == password )
//                            {
//                                mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
//                                CommUtil.syncDeviceTime( mac );
//                            }
//                            else
//                            {
//                                mCountDownTimer.cancel();
//                                showPasswordDialog( psw );
//                            }
//                        }
//                    } );
                }
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
            @SuppressLint ( "RestrictedApi" )
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
//            mProgressDialog.setMessage( getString( R.string.logging ) );
//            mProgressDialog.show();
//            mModifyPswFlag = false;
//            BleManager.getInstance().readPassword( mPrefer.getDeviceMac() );
//            mCountDownTimer.start();
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
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if ( object instanceof LightAuto )
            {
                mCountDownTimer.cancel();
                mLight.setAuto( true );
                mLight.setLightAuto( (LightAuto) object );
                runOnUiThread( new Runnable()
                {
                    @SuppressLint ( "RestrictedApi" )
                    @Override
                    public void run ()
                    {
                        mProgressDialog.dismiss();
                        if ( light_sw_auto.getVisibility() != View.VISIBLE || !light_sw_auto.isChecked() )
                        {
                            ft.replace( R.id.light_fl_show, LightAutoFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightAuto() ) )
                              .commitAllowingStateLoss();
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
                    @SuppressLint ( "RestrictedApi" )
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
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_1150
                                 || mPrefer.getDevId() == DeviceUtil.LIGHT_ID_AQUASKY_910 )
                            {
                                ft.replace( R.id.light_fl_show, RGBWManualFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightManual() ) )
                                  .commitAllowingStateLoss();
                            }
                            else
                            {
                                ft.replace( R.id.light_fl_show, LightManualFragment.newInstance( mac, mPrefer.getDevId(), mLight.getLightManual() ) )
                                  .commitAllowingStateLoss();
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

    private void showPasswordDialog ( final int password )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        final AlertDialog dialog = builder.create();
        dialog.setTitle( R.string.input_password );
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_password, null, false );
        final EditText psw_password = (EditText) view.findViewById( R.id.psw_password );
        Button btn_cancel = (Button) view.findViewById( R.id.psw_cancel );
        Button btn_login = (Button) view.findViewById( R.id.psw_login );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                dialog.dismiss();
                finish();
            }
        } );
        btn_login.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                String psw = psw_password.getText().toString();
                if ( psw.length() != 6 )
                {
                    psw_password.setError( getString( R.string.error_psw_6_num ) );
                }
                else
                {
                    int value = Integer.parseInt( psw );
                    if ( value == password )
                    {
                        setLocalPassword( mPrefer.getDeviceMac(), value );
                        mCountDownTimer.start();
                        mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
                        CommUtil.syncDeviceTime( mPrefer.getDeviceMac() );
                        dialog.dismiss();
                    }
                    else
                    {
                        psw_password.setError( getString( R.string.error_password_wrong ) );
                    }
                }
            }
        } );
        dialog.setView( view );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setCancelable( false );
        dialog.show();
    }

    private int getLocalPassword ( @NonNull String mac )
    {
        SharedPreferences defaultSet = PreferenceManager.getDefaultSharedPreferences( this );
        final int password = defaultSet.getInt( mac + "-" + ConstVal.KEY_PASSWORD, -1 );
        return password;
    }

    private void setLocalPassword ( @NonNull String mac, int password )
    {
        SharedPreferences defaultSet = PreferenceManager.getDefaultSharedPreferences( this );
        SharedPreferences.Editor editor = defaultSet.edit();
        editor.putInt( mac + "-" + ConstVal.KEY_PASSWORD, password );
        SharedPreferencesCompat.EditorCompat.getInstance().apply( editor );
    }

    private void modifyPassword ( final String mac, final int psw )
    {
//        mModifyPswFlag = true;
//        mRemotePassword = -1;
//        final CountDownTimer tmr = new CountDownTimer( 400, 400 ) {
//            @Override
//            public void onTick ( long millisUntilFinished )
//            {
//
//            }
//
//            @Override
//            public void onFinish ()
//            {
//                runOnUiThread( new Runnable() {
//                    @Override
//                    public void run ()
//                    {
//                        if ( mRemotePassword == psw )
//                        {
//                            Toast.makeText( LightActivity.this, R.string.modify_password_success, Toast.LENGTH_SHORT )
//                                 .show();
//                        }
//                        else
//                        {
//                            Toast.makeText( LightActivity.this, R.string.modify_password_fail, Toast.LENGTH_SHORT )
//                                 .show();
//                        }
//                    }
//                } );
//                mModifyPswFlag = false;
//                mRemotePassword = -1;
//            }
//        };
//        BleManager.getInstance().setPassword( mPrefer.getDeviceMac(), psw );
//        new Handler().postDelayed( new Runnable() {
//            @Override
//            public void run ()
//            {
//                tmr.start();
//                BleManager.getInstance().readPassword( mac );
//            }
//        }, 200 );
    }

    private void showModifyPasswordDialog ()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        final AlertDialog dialog = builder.create();
        dialog.setTitle( getString( R.string.modify_password ) );
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_modify_password, null, false );
        final EditText modify_new = (EditText) view.findViewById( R.id.modify_psw_new );
        final EditText modify_confirm = (EditText) view.findViewById( R.id.modify_psw_confirm );
        Button btn_cancel = (Button) view.findViewById( R.id.modify_psw_cancel );
        Button btn_modify = (Button) view.findViewById( R.id.modify_psw_modify );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                dialog.dismiss();
            }
        } );
        btn_modify.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                String psw1 = modify_new.getText().toString();
                String psw2 = modify_confirm.getText().toString();
                if ( psw1.length() != 6 )
                {
                    modify_new.setError( getString( R.string.error_psw_6_num ) );
                    return;
                }
                if ( psw2.length() != 6 )
                {
                    modify_confirm.setError( getString( R.string.error_psw_6_num ) );
                    return;
                }
                if ( psw1.equals( psw2 ) )
                {
                    int value = Integer.parseInt( psw1 );
                    modifyPassword( mPrefer.getDeviceMac(), value );
                    dialog.dismiss();
                }
                else
                {
                    modify_confirm.setError( getString( R.string.error_password_mismatch ) );
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
