package com.inledco.fluvalsmart.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.blemanager.BleManager;
import com.inledco.bleota.BleOTAActivity;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.activity.LightActivity;
import com.inledco.fluvalsmart.activity.ScanActivity;
import com.inledco.fluvalsmart.adapter.DeviceAdapter;
import com.inledco.fluvalsmart.bean.BaseDevice;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.impl.SwipeItemActionClickListener;
import com.inledco.fluvalsmart.util.PreferenceUtil;
import com.inledco.itemtouchhelperextension.ItemTouchHelperCallback;
import com.inledco.itemtouchhelperextension.ItemTouchHelperExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends BaseFragment
{
    private ImageView device_iv_add;
    private TextView device_tv_add;
    private RecyclerView device_rv_show;

    private List< BaseDevice > mDevices;
    private DeviceAdapter mDeviceAdapter;

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_device, null );
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
    public void onPause ()
    {
        super.onPause();
    }

    @Override
    public void onStop ()
    {
        super.onStop();
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        if ( requestCode == 1 && resultCode == 1 )
        {
            setFlag();
        }
    }

    @Override
    protected void initView ( View view )
    {
        device_iv_add = (ImageView) view.findViewById( R.id.device_iv_add );
        device_tv_add = (TextView) view.findViewById( R.id.device_tv_add );
        device_rv_show = (RecyclerView) view.findViewById( R.id.device_rv_show );
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
        if ( getFlag() || mDevices.size() > 0 )
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
                    case R.id.item_action_upgrade:
                        Intent intent = new Intent( getContext(), BleOTAActivity.class );
                        intent.putExtra( "devid", mDevices.get( position ).getDevicePrefer().getDevId() );
                        intent.putExtra( "name", mDevices.get( position ).getDevicePrefer().getDeviceName() );
                        intent.putExtra( "address", mDevices.get( position ).getDevicePrefer().getDeviceMac() );
                        startActivity( intent );
                        break;
                }
            }
        } );
    }

    private void setFlag ()
    {
        SharedPreferences sp = getContext().getSharedPreferences( "device_scan_flag", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sp.edit().putBoolean( "flag", true );
        SharedPreferencesCompat.EditorCompat.getInstance().apply( editor );
    }

    private boolean getFlag ()
    {
        SharedPreferences sp = getContext().getSharedPreferences( "device_scan_flag", Context.MODE_PRIVATE );
        return sp.getBoolean( "flag", false );
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
                Intent intent = new Intent( getContext(), ScanActivity.class );
                startActivityForResult( intent, 1 );
//                setFlag();
            }
        } );
    }
}
