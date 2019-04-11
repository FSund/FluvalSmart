package com.inledco.fluvalsmart.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.bean.BaseDevice;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.impl.SwipeItemActionClickListener;
import com.inledco.fluvalsmart.light.LightActivity;
import com.inledco.fluvalsmart.ota.BleOTAActivity;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.scan.ScanActivity;
import com.inledco.fluvalsmart.util.LightPrefUtil;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.inledco.itemtouchhelperextension.ItemTouchHelperCallback;
import com.inledco.itemtouchhelperextension.ItemTouchHelperExtension;
import com.liruya.tuner168blemanager.BleHelper;
import com.liruya.tuner168blemanager.BleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends BaseFragment
{
    private final int BLUETOOTH_REQUEST_ENABLE_CODE = 1;
    private final int PERMISSON_REQUEST_COARSE_CODE = 2;
    private final int SCAN_CODE = 3;

    private ImageView device_iv_add;
    private TextView device_tv_add;
    private RecyclerView device_rv_show;

    private List< BaseDevice > mDevices;
    private DeviceAdapter mDeviceAdapter;

    private BleHelper mBleHelper;

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
            mDevices.add( new BaseDevice( prefer ) );
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
                if (mBleHelper == null) {
                    mBleHelper = new BleHelper((AppCompatActivity) getActivity());
                }
                if (mBleHelper.isBluetoothEnabled()) {
                    BaseDevice device = mDevices.get(position);
                    Intent intent = new Intent(getContext(), LightActivity.class);
                    intent.putExtra("DevicePrefer", device.getDevicePrefer());
                    startActivity(intent);
                } else {
                    mBleHelper.requestBluetoothEnable(BLUETOOTH_REQUEST_ENABLE_CODE);
                }
            }

            @Override
            public void onClickAction ( @IdRes int id, int position )
            {
                switch ( id )
                {
                    case R.id.item_action_remove:
                        showRemoveDeviceDialog( position );
                        break;
//                    case R.id.item_action_reset_psw:
////                        showResetPasswordDialog( mDevices.get( position ).getDevicePrefer().getDeviceMac() );
//                        showRetrievePasswordDialog(mDevices.get(position));
//                        break;
                    case R.id.item_action_upgrade:
                        Intent intent = new Intent( getContext(), BleOTAActivity.class );
                        intent.putExtra( "devid", mDevices.get( position ).getDevicePrefer().getDevId() );
                        intent.putExtra( "name", mDevices.get( position ).getDevicePrefer().getDeviceName() );
                        intent.putExtra( "address", mDevices.get( position ).getDevicePrefer().getDeviceMac() );
                        intent.putExtra("mode", false);
                        startActivity( intent );
                        break;
                }
            }
        } );
    }

    private void showRemoveDeviceDialog ( final int position )
    {
//        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder( getContext(), R.style.DialogTheme );
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
                    LightPrefUtil.removeLocalPassword(getContext(), mDevices.get( position )
                                                                            .getDevicePrefer()
                                                                            .getDeviceMac());
                    mDevices.remove( position );
                    mDeviceAdapter.notifyItemRemoved(position);
                    dialogInterface.dismiss();
                }
            }
        } );
        builder.show();
//        AlertDialog dialog = builder.create();
//        dialog.setCanceledOnTouchOutside( false );
//        dialog.show();
    }

    @Override
    protected void initEvent ()
    {
        device_iv_add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if (mBleHelper == null) {
                    mBleHelper = new BleHelper((AppCompatActivity) getActivity());
                }
                if (mBleHelper.checkLocationPermission()) {
                    startScanActivity();
                } else {
                    mBleHelper.requestLocationPermission(PERMISSON_REQUEST_COARSE_CODE);
                }
            }
        } );
    }

    private void startScanActivity()
    {
        Intent intent = new Intent( getContext(), ScanActivity.class );
        startActivityForResult( intent, SCAN_CODE );
    }

//    private void showRetrievePasswordDialog(final BaseDevice device) {
//        final String address = device.getDevicePrefer().getDeviceMac();
////        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
//        CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme );
//        View view = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_retrieve_password, null, false );
//        builder.setTitle( R.string.retrieve_password );
//        builder.setView(view);
//        final AlertDialog dialog = builder.show();
//        final ImageButton ib_copy = view.findViewById(R.id.dialog_retrieve_copy);
//        final EditText retrieve_key = view.findViewById( R.id.dialog_retrieve_key);
//        final TextView retrieve_msg = view.findViewById(R.id.dialog_retrieve_msg);
//        Button btn_cancel = view.findViewById( R.id.dialog_retrieve_cancel );
//        Button btn_retrieve = view.findViewById( R.id.dialog_retrieve_retrieve);
//        final CountDownTimer tmr = new CountDownTimer( 4000, 4000 ) {
//            @Override
//            public void onTick ( long millisUntilFinished )
//            {
//
//            }
//
//            @Override
//            public void onFinish ()
//            {
//                BleManager.getInstance().disconnectDevice( address );
//                retrieve_msg.setText(R.string.timeout);
//            }
//        };
//        final BleCommunicateListener mListener = new BleCommunicateListener() {
//            @Override
//            public void onDataValid ( final String mac )
//            {
//                if ( mac.equals( address ) )
//                {
//                    BleManager.getInstance().readPassword( mac );
//                }
//            }
//
//            @Override
//            public void onDataInvalid ( String mac )
//            {
//
//            }
//
//            @Override
//            public void onReadMfr ( String mac, String s )
//            {
//
//            }
//
//            @Override
//            public void onReadPassword ( String mac, final int psw )
//            {
//                if ( mac.equals( address ) )
//                {
//                    tmr.cancel();
//                    getActivity().runOnUiThread( new Runnable() {
//                        @Override
//                        public void run ()
//                        {
//                            DecimalFormat df = new DecimalFormat("000000");
//                            retrieve_msg.setText(getString(R.string.retrieve_psw_is) + df.format(psw));
//                        }
//                    } );
//                    BleManager.getInstance().disconnectDevice( mac );
//                }
//            }
//
//            @Override
//            public void onDataReceived ( String mac, ArrayList< Byte > list )
//            {
//
//            }
//        };
//        ib_copy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData cd = ClipData.newPlainText("device_info", getDeviceInfo(device));
//                cm.setPrimaryClip(cd);
//                Toast.makeText(getContext(), R.string.retrieve_copy_msg, Toast.LENGTH_LONG)
//                     .show();
//            }
//        });
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        btn_retrieve.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String key = retrieve_key.getText().toString().toLowerCase();
//                if (TextUtils.isEmpty(key)) {
//                    retrieve_msg.setText("");
//                } else {
//                    if (key.equals(Md5Util.encrypt(address).toLowerCase())) {
//                        BleManager.getInstance().connectDevice( address );
//                        tmr.start();
//                        retrieve_msg.setText("");
//                    } else {
//                        retrieve_msg.setText(R.string.retrieve_wrong_key);
//                    }
//                }
//            }
//        });
//        dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss ( DialogInterface dialog )
//            {
//                BleManager.getInstance().disconnectDevice( address );
//                BleManager.getInstance().removeBleCommunicateListener( mListener );
//            }
//        } );
//
////        dialog.setCanceledOnTouchOutside( false );
////        dialog.setView( view );
////        dialog.show();
//        BleManager.getInstance().addBleCommunicateListener( mListener );
//    }
//
//    private String getDeviceInfo(BaseDevice device) {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("type", DeviceUtil.getDeviceType(device.getDevicePrefer().getDevId()));
//        jsonObject.addProperty("name", device.getDevicePrefer().getDeviceName());
//        jsonObject.addProperty("address", device.getDevicePrefer().getDeviceMac());
//        jsonObject.addProperty("app_version", BuildConfig.VERSION_NAME);
//        return jsonObject.toString();
//    }
}
