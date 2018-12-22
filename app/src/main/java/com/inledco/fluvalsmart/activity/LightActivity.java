package com.inledco.fluvalsmart.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.api.DataUtil;
import com.inledco.bleota.BleOTAActivity;
import com.inledco.bleota.RemoteFirmware;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.fragment.DataInvalidFragment;
import com.inledco.fluvalsmart.fragment.LightAutoFragment;
import com.inledco.fluvalsmart.fragment.LightManualFragment;
import com.inledco.fluvalsmart.fragment.LightProFragment;
import com.inledco.fluvalsmart.fragment.RGBWManualFragment;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LightProfileUtil;
import com.inledco.fluvalsmart.util.Md5Util;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.inledco.fluvalsmart.view.CustomProgressDialog;
import com.liruya.okhttpmanager.HttpCallback;
import com.liruya.okhttpmanager.OKHttpManager;
import com.liruya.tuner168blemanager.BleCommunicateListener;
import com.liruya.tuner168blemanager.BleManager;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.Call;

public class LightActivity extends BaseActivity implements DataInvalidFragment.OnRetryClickListener
{
    private static final String OTA_UPGRADE_LINK = "http://47.88.12.183:8080/OTAInfoModels/GetOTAInfo?deviceid=";

    private Toolbar light_toolbar;
    private ProgressDialog mProgressDialog;
    private LinearLayout light_mode_show;
    private CheckedTextView light_ctv_manual;
    private CheckedTextView light_ctv_auto;
    private CheckedTextView light_ctv_pro;
    private TextView light_prof_name;
    private MenuItem menu_device_update;

    private DevicePrefer mPrefer;

    private boolean mModifyPswFlag = false;
    private int mRemotePassword = -1;

    private CountDownTimer mCountDownTimer;

    private int mDeviceVersion;
    private int mRemoteVersion;

    private BleCommunicateListener mCommunicateListener;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled( true );
    }

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
        BleManager.getInstance().removeBleCommunicateListener(mCommunicateListener);
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
        MenuItem menu_device_edit = menu.findItem( R.id.menu_device_rename);
        MenuItem menu_device_find = menu.findItem( R.id.menu_device_find );
        menu_device_update = menu.findItem( R.id.menu_device_update );
        MenuItem menu_device_modpsw = menu.findItem( R.id.menu_device_modpsw );
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
                CommUtil.findDevice( mPrefer.getDeviceMac() );
                return false;
            }
        } );
        menu_device_update.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick ( MenuItem item )
            {
                if (mPrefer != null)
                {
                    final Intent intent = new Intent(LightActivity.this, BleOTAActivity.class);
                    intent.putExtra("devid", mPrefer.getDevId());
                    intent.putExtra("name", mPrefer.getDeviceName());
                    intent.putExtra("address", mPrefer.getDeviceMac());
                    intent.putExtra("mode", Setting.forceUpdate());
                    startActivity(intent);
//                    finish();
                }
                return false;
            }
        } );
        menu_device_modpsw.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick( MenuItem item )
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
        light_mode_show = findViewById( R.id.light_mode_show );
        light_ctv_manual = findViewById( R.id.light_ctv_manual );
        light_ctv_auto = findViewById( R.id.light_ctv_auto );
        light_ctv_pro = findViewById( R.id.light_ctv_pro );
        light_prof_name = findViewById( R.id.light_prof_name );
        light_toolbar = findViewById( R.id.light_toolbar );
        light_toolbar.setTitle( mPrefer.getDeviceName() );
        setSupportActionBar( light_toolbar );

        mProgressDialog = new CustomProgressDialog(this, R.style.DialogTheme );
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
        OKHttpManager.getInstance().get( OTA_UPGRADE_LINK + mPrefer.getDevId(), null, new HttpCallback<RemoteFirmware>()
        {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onError( int code, final String msg )
            {
            }

            @Override
            public void onSuccess( final RemoteFirmware result )
            {
                mRemoteVersion = (result.getMajor_version()<<8)|result.getMinor_version();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mDeviceVersion > 0 && mRemoteVersion > 0 && mDeviceVersion < mRemoteVersion)
                        {
                            menu_device_update.setVisible(true);
                        }
                        else
                        {
                            menu_device_update.setVisible(false);
                        }
                    }
                });
            }
        } );
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
                        light_mode_show.setVisibility( View.GONE );
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
                    mModifyPswFlag = false;
                    BleManager.getInstance().readPassword( mac );
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            mProgressDialog.setMessage( getString( R.string.logging ) );
                            mCountDownTimer.start();
                        }
                    } );
//                    runOnUiThread( new Runnable() {
//                        @Override
//                        public void run ()
//                        {
//                            mCountDownTimer.start();
//                            mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
//                            BleManager.getInstance().readMfr(mac);
//                        }
//                    } );
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
                            light_mode_show.setVisibility( View.GONE );
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
                if (mac.equals(mPrefer.getDeviceMac()))
                {
                    byte[] mfr = DataUtil.hexToByteArray( s.replace( " ", "" ) );
                    if ( mfr == null || mfr.length < 4 )
                    {
                        mDeviceVersion = 0;
                    }
                    else
                    {
                        mDeviceVersion = ((mfr[2]&0xFF)<<8)|(mfr[3]&0xFF);
                        CommUtil.syncDeviceTime( mac );
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (mDeviceVersion > 0 && mRemoteVersion > 0 && mDeviceVersion < mRemoteVersion)
                            {
                                menu_device_update.setVisible(true);
                            }
                            else
                            {
                                menu_device_update.setVisible(false);
                            }
                        }
                    });

                }
            }

            @Override
            public void onReadPassword ( final String mac, final int psw )
            {
                if ( mac.equals( mPrefer.getDeviceMac() ) )
                {
                    if ( mModifyPswFlag )
                    {
                        mModifyPswFlag = false;
                        mRemotePassword = psw;
                        return;
                    }
                    final int password = getLocalPassword( mac );
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            if ( psw == password )
                            {
//                                mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
//                                CommUtil.syncDeviceTime( mac );
                                mCountDownTimer.start();
                                mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
                                BleManager.getInstance().readMfr(mac);
                            }
                            else
                            {
                                mCountDownTimer.cancel();
                                showPasswordDialog( psw );
                            }
                        }
                    } );
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
        light_toolbar.setTitle( mPrefer.getDeviceName() );
        light_mode_show.setVisibility( View.GONE );
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

        light_ctv_manual.setOnClickListener( new View.OnClickListener() {
            @SuppressLint ( "RestrictedApi" )
            @Override
            public void onClick( View v )
            {
                if ( !light_ctv_manual.isChecked() )
                {
                    CommUtil.setModeManual( mPrefer.getDeviceMac() );
                }
            }
        } );

        light_ctv_auto.setOnClickListener( new View.OnClickListener() {
            @SuppressLint ( "RestrictedApi" )
            @Override
            public void onClick( View v )
            {
                if ( !light_ctv_auto.isChecked() )
                {
                    CommUtil.setModeAuto( mPrefer.getDeviceMac() );
                }
            }
        } );

        light_ctv_pro.setOnClickListener( new View.OnClickListener() {
            @SuppressLint ( "RestrictedApi" )
            @Override
            public void onClick( View v )
            {
                if ( !light_ctv_pro.isChecked() )
                {
                    CommUtil.setModePro( mPrefer.getDeviceMac() );
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
            mProgressDialog.setMessage( getString( R.string.logging ) );
            mProgressDialog.show();
            mModifyPswFlag = false;
            BleManager.getInstance().readPassword( mPrefer.getDeviceMac() );
            mCountDownTimer.start();
//            mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
//            mProgressDialog.show();
//            CommUtil.syncDeviceTime( mPrefer.getDeviceMac() );
//            mCountDownTimer.start();
        }
    }

    private boolean isNewVersion()
    {
        if (mDeviceVersion <= 258)        //0x0102 V1.02 old version
        {
            return false;
        }
        return true;
    }

    private void decodeReceiveData ( final String mac, ArrayList< Byte > list )
    {
        final Object object = CommUtil.decodeLight( list, mPrefer.getDevId() );
        if ( object != null )
        {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if ( object instanceof LightAuto )
            {
                mCountDownTimer.cancel();
                runOnUiThread( new Runnable()
                {
                    @SuppressLint ( "RestrictedApi" )
                    @Override
                    public void run ()
                    {
                        mProgressDialog.dismiss();
                        if ( light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_auto.isChecked() )
                        {
                            ft.replace( R.id.light_fl_show, LightAutoFragment.newInstance( mac, mPrefer.getDevId(), (LightAuto) object ) )
                              .commitAllowingStateLoss();
                        }
                        light_mode_show.setVisibility( View.VISIBLE );
                        light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                        light_ctv_manual.setChecked( false );
                        light_ctv_auto.setChecked( true );
                        light_ctv_pro.setChecked( false );
                        String prof = LightProfileUtil.getAutoProfileName( LightActivity.this,
                                                                           mPrefer.getDevId(),
                                                                           (LightAuto) object );
                        light_toolbar.setSubtitle(prof);
//                        light_prof_name.setText( prof );
                    }
                } );
            }
            else if ( object instanceof LightPro )
            {
                mCountDownTimer.cancel();
                runOnUiThread( new Runnable() {
                    @SuppressLint ( "RestrictedApi" )
                    @Override
                    public void run()
                    {
                        mProgressDialog.dismiss();
                        if ( light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_pro.isChecked() )
                        {
                            ft.replace( R.id.light_fl_show, LightProFragment.newInstance( mac, mPrefer.getDevId(), (LightPro) object ) )
                              .commitAllowingStateLoss();
                        }
                        light_mode_show.setVisibility( View.VISIBLE );
                        light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                        light_ctv_manual.setChecked( false );
                        light_ctv_auto.setChecked( false );
                        light_ctv_pro.setChecked( true );
                        String prof = LightProfileUtil.getProProfileName( LightActivity.this,
                                                                          mPrefer.getDevId(), (LightPro) object );
                        light_toolbar.setSubtitle(prof);
//                        light_prof_name.setText( prof );
                    }
                } );
            }
            else if ( object instanceof LightManual )
            {
                mCountDownTimer.cancel();
                runOnUiThread( new Runnable()
                {
                    @SuppressLint ( "RestrictedApi" )
                    @Override
                    public void run ()
                    {
                        mProgressDialog.dismiss();
                        if ( light_mode_show.getVisibility() != View.VISIBLE || !light_ctv_manual.isChecked() )
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
                                ft.replace( R.id.light_fl_show, RGBWManualFragment.newInstance( mac, mPrefer.getDevId(), (LightManual) object ) )
                                  .commitAllowingStateLoss();
                            }
                            else
                            {
                                ft.replace( R.id.light_fl_show, LightManualFragment.newInstance( mac, mPrefer.getDevId(), (LightManual) object ) )
                                  .commitAllowingStateLoss();
                            }
                        }
                        light_mode_show.setVisibility( View.VISIBLE );
                        light_ctv_pro.setVisibility(isNewVersion() ? View.VISIBLE : View.GONE);
                        light_ctv_manual.setChecked( true );
                        light_ctv_auto.setChecked( false );
                        light_ctv_pro.setChecked( false );
                        light_toolbar.setSubtitle("");
//                        light_prof_name.setText( "" );
                    }
                } );
            }
        }
    }

    private void showRenameDialog ( final DevicePrefer prefer )
    {
        CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_rename, null );
        builder.setTitle( R.string.rename_device );
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        Button btn_cancel = view.findViewById( R.id.rename_cancel );
        Button btn_rename = view.findViewById( R.id.rename_confirm );
        final EditText newname = view.findViewById( R.id.rename_newname );
        final boolean[] flag = new boolean[]{ false};
        newname.setText( prefer.getDeviceName() );
        newname.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {

            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {
                if ( s.length() > 0 && start == 0 && flag[0] == false )
                {
                    flag[0] = true;
                    String str = new StringBuilder().append( s.subSequence( 0, 1 ) ).toString().toUpperCase();
                    str = new StringBuilder( str ).append( s.subSequence( 1, s.length() ) ).toString();
                    newname.setText( str );
                    newname.setSelection( start + count );
                }
                else
                {
                    flag[0] = false;
                }
            }

            @Override
            public void afterTextChanged( Editable s )
            {

            }
        } );
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
//        dialog.setView( view );
//        dialog.setCanceledOnTouchOutside( false );
//        dialog.show();
    }

    private void showRetrievePasswordDialog(final DevicePrefer device) {
        final String address = device.getDeviceMac();
        //        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(LightActivity.this, R.style.DialogTheme );
        View view = LayoutInflater.from( LightActivity.this ).inflate( R.layout.dialog_retrieve_password, null, false );
        builder.setTitle( R.string.retrieve_password );
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final ImageButton ib_copy = view.findViewById(R.id.dialog_retrieve_copy);
        final EditText retrieve_key = view.findViewById( R.id.dialog_retrieve_key);
        final TextView retrieve_msg = view.findViewById(R.id.dialog_retrieve_msg);
        Button btn_cancel = view.findViewById( R.id.dialog_retrieve_cancel );
        Button btn_retrieve = view.findViewById( R.id.dialog_retrieve_retrieve);
        final CountDownTimer tmr = new CountDownTimer( 4000, 4000 ) {
            @Override
            public void onTick ( long millisUntilFinished )
            {

            }

            @Override
            public void onFinish ()
            {
                BleManager.getInstance().disconnectDevice( address );
                retrieve_msg.setText(R.string.timeout);
            }
        };
        final BleCommunicateListener mListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( final String mac )
            {
                if ( mac.equals( address ) )
                {
                    BleManager.getInstance().readPassword( mac );
                }
            }

            @Override
            public void onDataInvalid ( String mac )
            {

            }

            @Override
            public void onReadMfr ( String mac, String s )
            {

            }

            @Override
            public void onReadPassword ( String mac, final int psw )
            {
                if ( mac.equals( address ) )
                {
                    tmr.cancel();
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            DecimalFormat df = new DecimalFormat("000000");
                            retrieve_msg.setText(getString(R.string.retrieve_psw_is) + df.format(psw));
                        }
                    } );
                    BleManager.getInstance().disconnectDevice( mac );
                }
            }

            @Override
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {

            }
        };
        ib_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd = ClipData.newPlainText("device_info", DeviceUtil.getDeviceInfo(device));
                cm.setPrimaryClip(cd);
                Toast.makeText(LightActivity.this, R.string.retrieve_copy_msg, Toast.LENGTH_LONG)
                     .show();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = retrieve_key.getText().toString().toLowerCase();
                if (TextUtils.isEmpty(key)) {
                    retrieve_msg.setText("");
                } else {
                    if (key.equals(Md5Util.encrypt(address).toLowerCase())) {
                        BleManager.getInstance().connectDevice( address );
                        tmr.start();
                        retrieve_msg.setText("");
                    } else {
                        retrieve_msg.setText(R.string.retrieve_wrong_key);
                    }
                }
            }
        });
        dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss ( DialogInterface dialog )
            {
                BleManager.getInstance().disconnectDevice( address );
                BleManager.getInstance().removeBleCommunicateListener( mListener );
                finish();
            }
        } );

        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.setView( view );
        //        dialog.show();
        BleManager.getInstance().addBleCommunicateListener( mListener );
    }

    private void showPasswordDialog ( final int password )
    {
//        AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder( this, R.style.DialogTheme );
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_password, null, false );
        builder.setTitle( R.string.input_password );
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();
        final EditText psw_password = view.findViewById( R.id.psw_password );
        Button btn_retrieve = view.findViewById( R.id.psw_retrieve );
        Button btn_cancel = view.findViewById( R.id.psw_cancel );
        Button btn_login = view.findViewById( R.id.psw_login );
        btn_retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showRetrievePasswordDialog(mPrefer);
            }
        });
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
                        BleManager.getInstance().readMfr( mPrefer.getDeviceMac() );
//                        mCountDownTimer.start();
//                        mProgressDialog.setMessage( getString( R.string.msg_get_device_data ) );
//                        CommUtil.syncDeviceTime( mPrefer.getDeviceMac() );
                        dialog.dismiss();
                    }
                    else
                    {
                        psw_password.setError( getString( R.string.error_password_wrong ) );
                    }
                }
            }
        } );
//        dialog.setView( view );
//        dialog.setCanceledOnTouchOutside( false );
//        dialog.setCancelable( false );
//        dialog.show();
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
        editor.apply();
    }

    private void modifyPassword ( final String mac, final int psw )
    {
        mModifyPswFlag = true;
        mRemotePassword = -1;
        final CountDownTimer tmr = new CountDownTimer( 400, 400 ) {
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
                        if ( mRemotePassword == psw )
                        {
                            Toast.makeText( LightActivity.this, R.string.modify_password_success, Toast.LENGTH_SHORT )
                                 .show();
                        }
                        else
                        {
                            Toast.makeText( LightActivity.this, R.string.modify_password_fail, Toast.LENGTH_SHORT )
                                 .show();
                        }
                    }
                } );
                mModifyPswFlag = false;
                mRemotePassword = -1;
            }
        };
        BleManager.getInstance().setPassword( mPrefer.getDeviceMac(), psw );
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run ()
            {
                tmr.start();
                BleManager.getInstance().readPassword( mac );
            }
        }, 200 );
    }

    private void showModifyPasswordDialog ()
    {
//        AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder( this, R.style.DialogTheme );
        View view = LayoutInflater.from( this ).inflate( R.layout.dialog_modify_password, null, false );
        builder.setTitle( getString( R.string.modify_password ) );
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final TextInputEditText modify_new = view.findViewById(R.id.modify_psw_new);
        final TextInputEditText modify_confirm = view.findViewById( R.id.modify_psw_confirm );
        Button btn_cancel = view.findViewById( R.id.modify_psw_cancel );
        Button btn_modify = view.findViewById( R.id.modify_psw_modify );
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
//        dialog.setView( view );
//        dialog.setCanceledOnTouchOutside( false );
//        dialog.show();
    }

    @Override
    public void onRetryClick ()
    {
        getDeviceData();
    }
}
