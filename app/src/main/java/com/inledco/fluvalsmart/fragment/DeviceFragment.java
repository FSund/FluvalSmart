package com.inledco.fluvalsmart.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.bleota.BleOTAActivity;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.activity.LightActivity;
import com.inledco.fluvalsmart.activity.ScanActivity;
import com.inledco.fluvalsmart.adapter.DeviceAdapter;
import com.inledco.fluvalsmart.bean.BaseDevice;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.impl.SwipeItemActionClickListener;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.inledco.itemtouchhelperextension.ItemTouchHelperCallback;
import com.inledco.itemtouchhelperextension.ItemTouchHelperExtension;
import com.liruya.tuner168blemanager.BleCommunicateListener;
import com.liruya.tuner168blemanager.BleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends BaseFragment
{
    private final int PERMISSON_REQUEST_COARSE_CODE = 2;
    private final int SCAN_CODE = 3;

    private ImageView device_iv_add;
    private TextView device_tv_add;
    private RecyclerView device_rv_show;

    private List< BaseDevice > mDevices;
    private DeviceAdapter mDeviceAdapter;

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_device, container, false );
        initView( view );
        initEvent();
        return view;
    }

    @Override
    public void onResume ()
    {
        super.onResume();
        initData();
        BleManager.getInstance().disConnectAll();
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();
    }

    @Override
    protected void initView ( View view )
    {
        device_iv_add = view.findViewById( R.id.device_iv_add );
        device_tv_add = view.findViewById( R.id.device_tv_add );
        device_rv_show = view.findViewById( R.id.device_rv_show );
        device_rv_show.setLayoutManager( new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false ) );
        device_rv_show.addItemDecoration( new DividerItemDecoration( getContext(), OrientationHelper.VERTICAL ) );
    }

    @Override
    protected void initData ()
    {
        mDevices = new ArrayList<>();
        SharedPreferences sp = getContext().getSharedPreferences( ConstVal.DEV_PREFER_FILENAME, Context.MODE_PRIVATE );
        for ( String key : sp.getAll()
                             .keySet() )
        {
            DevicePrefer prefer = (DevicePrefer) PreferenceUtil.getObjectFromPrefer( getContext(), ConstVal.DEV_PREFER_FILENAME, key );
            mDevices.add( new BaseDevice( prefer, false ) );
        }
        if ( Setting.hasScanTip( getContext() ) || mDevices.size() > 0 )
        {
            device_iv_add.setVisibility( View.GONE );
            device_tv_add.setVisibility( View.GONE );
            device_rv_show.setVisibility( View.VISIBLE );
        }
        else
        {
            device_iv_add.setVisibility( View.VISIBLE );
            device_tv_add.setVisibility( View.VISIBLE );
            device_rv_show.setVisibility( View.GONE );
        }
        mDeviceAdapter = new DeviceAdapter( getContext(), mDevices );
        device_rv_show.setAdapter( mDeviceAdapter );
        ItemTouchHelperCallback callback = new ItemTouchHelperCallback();
        ItemTouchHelperExtension mItemTouchHelperExtension = new ItemTouchHelperExtension( callback );
        mItemTouchHelperExtension.attachToRecyclerView( device_rv_show );
        mDeviceAdapter.setSwipeItemActionClickListener( new SwipeItemActionClickListener() {
            @Override
            public void onClickContent ( int position )
            {
                BaseDevice device = mDevices.get( position );
                Intent intent = new Intent( getContext(), LightActivity.class );
                intent.putExtra( "DevicePrefer", device.getDevicePrefer() );
                startActivity( intent );
            }

            @Override
            public void onClickAction ( @IdRes int id, int position )
            {
                switch ( id )
                {
                    case R.id.item_action_remove:
                        showRemoveDeviceDialog( position );
                        break;
                    case R.id.item_action_reset_psw:
                        showResetPasswordDialog( mDevices.get( position ).getDevicePrefer().getDeviceMac() );
                        break;
                    case R.id.item_action_upgrade:
                        Intent intent = new Intent( getContext(), BleOTAActivity.class );
                        intent.putExtra( "devid", mDevices.get( position ).getDevicePrefer().getDevId() );
                        intent.putExtra( "name", mDevices.get( position ).getDevicePrefer().getDeviceName() );
                        intent.putExtra( "address", mDevices.get( position ).getDevicePrefer().getDeviceMac() );
                        intent.putExtra("mode", Setting.forceUpdate());
                        startActivity( intent );
                        break;
                }
            }
        } );
    }

    private void showRemoveDeviceDialog ( final int position )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        builder.setTitle( R.string.remove_device );
        builder.setNegativeButton( R.string.cancel, null );
        builder.setPositiveButton( R.string.remove, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick ( DialogInterface dialogInterface, int i )
            {
                if ( mDevices.size() > position )
                {
                    PreferenceUtil.deleteObjectFromPrefer( getContext(),
                                                           ConstVal.DEV_PREFER_FILENAME,
                                                           mDevices.get( position )
                                                                   .getDevicePrefer()
                                                                   .getDeviceMac() );

                    mDevices.remove( position );
                    mDeviceAdapter.notifyItemRemoved(position);
                    dialogInterface.dismiss();
                }
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    @Override
    protected void initEvent ()
    {
        device_iv_add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                     ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
                {
                    startScanActivity();
                }
                else
                {
                    ActivityCompat.requestPermissions( getActivity(), new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION },
                                                       PERMISSON_REQUEST_COARSE_CODE );
                }
            }
        } );
    }

    private void startScanActivity()
    {
        Intent intent = new Intent( getContext(), ScanActivity.class );
        startActivityForResult( intent, SCAN_CODE );
    }

    private void showResetPasswordDialog( @NonNull final String address )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        final AlertDialog dialog = builder.create();
        View view = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_reset_password, null, false );
        final EditText reset_key = view.findViewById( R.id.reset_psw_key );
        Button btn_cancel = view.findViewById( R.id.reset_psw_cancel );
        Button btn_reset = view.findViewById( R.id.reset_psw_reset );
        final CountDownTimer tmr = new CountDownTimer( 4000, 4000 ) {
            @Override
            public void onTick ( long millisUntilFinished )
            {

            }

            @Override
            public void onFinish ()
            {
                BleManager.getInstance().disconnectDevice( address );
                Toast.makeText( getContext(), R.string.reset_password_fail, Toast.LENGTH_SHORT )
                     .show();
            }
        };
        final BleCommunicateListener mListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( final String mac )
            {
                if ( mac.equals( address ) )
                {
                    BleManager.getInstance().setPassword( mac, 0 );
                    new Handler().postDelayed( new Runnable() {
                        @Override
                        public void run ()
                        {
                            BleManager.getInstance().readPassword( mac );
                        }
                    }, 200 );
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
                    getActivity().runOnUiThread( new Runnable() {
                        @Override
                        public void run ()
                        {
                            if ( psw == 0 )
                            {
                                dialog.dismiss();
                                Toast.makeText( getContext(), R.string.reset_password_success, Toast.LENGTH_SHORT )
                                     .show();
                            }
                            else
                            {
                                Toast.makeText( getContext(), R.string.reset_password_fail, Toast.LENGTH_SHORT )
                                     .show();
                            }
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
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                dialog.dismiss();
            }
        } );
        btn_reset.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                String key = reset_key.getText().toString();
                if ( key.equals( "*#reset password#*" ) )
                {
                    BleManager.getInstance().connectDevice( address );
                    tmr.start();
                }
                else
                {
                    Toast.makeText( getContext(), "", Toast.LENGTH_SHORT )
                         .show();
                    reset_key.setError( getString( R.string.error_reset_key_wrong ) );
                }
            }
        } );
        dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss ( DialogInterface dialog )
            {
                BleManager.getInstance().disconnectDevice( address );
                BleManager.getInstance().removeBleCommunicateListener( mListener );
            }
        } );
        dialog.setTitle( R.string.reset_password );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setView( view );
        dialog.show();
        BleManager.getInstance().addBleCommunicateListener( mListener );
    }
}
