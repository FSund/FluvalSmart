package com.inledco.fluvalsmart.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.ble.api.DataUtil;
import com.inledco.blemanager.BleCommunicateListener;
import com.inledco.blemanager.BleManager;
import com.inledco.blemanager.BleScanListener;
import com.inledco.blemanager.LogUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.adapter.ScanAdapter;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.bean.SelectDevice;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScanActivity extends BaseActivity
{
    private Toolbar scan_toolbar;
    private ToggleButton scan_tb_scan;
    private ProgressBar scan_pb_scanning;
    private RecyclerView scan_rv_show;
    private FloatingActionButton scan_fab_confirm;

    private Set<String> scannedAddress;
//    private Map<String, Boolean> scannedAddress;
//    private Map<String, String > macNames;
    private Map<String, DevicePrefer> storedAddress;

    private ArrayList<SelectDevice> mDevices;
    private ScanAdapter mScanAdapter;
    private Handler mHandler;

    private BleScanListener mScanListener;
    private BleCommunicateListener mCommunicateListener;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_scan );

        initView();
        initEvent();
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        initData();
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        BleManager.getInstance().stopScan();
        BleManager.getInstance().removeBleScanListener( mScanListener );
        BleManager.getInstance().removeBleCommunicateListener( mCommunicateListener );
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu_scan, menu );
        MenuItem menuItem = menu.findItem( R.id.scan_menu_scan );
        scan_pb_scanning = (ProgressBar) menuItem.getActionView().findViewById( R.id.menu_item_progress );
        scan_tb_scan = (ToggleButton) menuItem.getActionView().findViewById( R.id.menu_item_scan );
        scan_tb_scan.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged ( CompoundButton buttonView, boolean isChecked )
            {
                if ( isChecked )
                {
                    BleManager.getInstance().startScan();
                }
                else
                {
                    BleManager.getInstance().stopScan();
                }
            }
        } );
        scan_tb_scan.setChecked( true );
        return true;
    }

    @Override
    protected void initView ()
    {
        scan_toolbar = (Toolbar) findViewById( R.id.scan_toolbar );
        scan_toolbar.setTitle( "" );
        setSupportActionBar( scan_toolbar );
        scan_rv_show = (RecyclerView) findViewById( R.id.scan_rv_show );
        scan_fab_confirm = (FloatingActionButton) findViewById( R.id.scasn_fab_confirm );

        scan_rv_show.setLayoutManager( new LinearLayoutManager( ScanActivity.this,
                                                                LinearLayoutManager.VERTICAL,
                                                                false) );
        scan_rv_show.addItemDecoration( new DividerItemDecoration( this, OrientationHelper.VERTICAL ) );
        scan_fab_confirm.setVisibility( View.GONE );
    }

    @Override
    protected void initData()
    {
        mHandler = new Handler()
        {
            @Override
            public void handleMessage ( Message msg )
            {
                super.handleMessage( msg );
                boolean show = false;
                switch ( msg.what )
                {
                    case 0:
//                        scan_fab_confirm.setVisibility( View.GONE );
                        for ( SelectDevice dev : mDevices )
                        {
                            if ( dev.isSelectable() && dev.isSelected() )
                            {
//                                scan_fab_confirm.setVisibility( View.VISIBLE );
                                show = true;
                                break;
                            }
                        }
                        if ( show )
                        {
                            if ( scan_fab_confirm.getVisibility() != View.VISIBLE )
                            {
                                scan_fab_confirm.setVisibility( View.VISIBLE );
                                ValueAnimator animator = ObjectAnimator.ofFloat( scan_fab_confirm, "alpha", 0.5f, 1.0f );
                                animator.setDuration( 800 );
                                animator.setRepeatCount( ValueAnimator.INFINITE );
                                animator.setRepeatMode( ValueAnimator.REVERSE );
                                animator.start();
                            }
                        }
                        else
                        {
                            scan_fab_confirm.setVisibility( View.GONE );
                        }
                        break;
                }
            }
        };
        mScanListener = new BleScanListener() {
            @Override
            public void onStartScan ()
            {
                LogUtil.d( TAG, "onStartScan: " );
                scannedAddress = new HashSet<>();
                storedAddress = PreferenceUtil.getAllObjectMapFromPrefer( ScanActivity.this, ConstVal.DEV_PREFER_FILENAME );
//                macNames = new HashMap<>();
                mDevices.clear();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run ()
                    {
                        mScanAdapter.notifyDataSetChanged();
                        scan_pb_scanning.setVisibility( View.VISIBLE );
                        scan_fab_confirm.setVisibility( View.GONE );
                    }
                } );
            }

            @Override
            public void onStopScan ()
            {
                BleManager.getInstance().disConnectAll();
                scan_tb_scan.setChecked( false );
                scan_pb_scanning.setVisibility( View.GONE );
                LogUtil.d( TAG, "onStopScan: " );
            }

            @Override
            public void onDeviceScanned ( String mac, String name, byte[] bytes )
            {
                decodeScanData( mac, name, bytes );
            }
        };
        mCommunicateListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( String mac )
            {
//                if ( !scannedAddress.contains( mac ) )
//                {
//                    scannedAddress.add( mac );
//                    mDevices.add( new SelectDevice( false, false, new DevicePrefer( mac, macNames.get( mac ) ) ) );
//                    runOnUiThread( new Runnable() {
//                        @Override
//                        public void run ()
//                        {
//                            mScanAdapter.notifyItemInserted( mDevices.size() - 1 );
//                        }
//                    } );
//                    BleManager.getInstance()
//                              .readMfr( mac );
//                }
                BleManager.getInstance()
                          .readMfr( mac );
            }

            @Override
            public void onDataInvalid ( final String mac )
            {

            }

            @Override
            public void onReadMfr ( String mac, String s )
            {
                BleManager.getInstance().disconnectDevice( mac );
                decodeMfrData( mac, s );
            }

            @Override
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {

            }
        };
        BleManager.getInstance().addBleScanListener( mScanListener );
        BleManager.getInstance().addBleCommunicateListener( mCommunicateListener );

        mDevices = new ArrayList<>();
        mScanAdapter = new ScanAdapter( ScanActivity.this, mHandler, mDevices );
        scan_rv_show.setAdapter( mScanAdapter );
    }

    @Override
    protected void initEvent()
    {
        scan_toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                finish();
            }
        } );

        //确认按键点击事件
        scan_fab_confirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                for ( SelectDevice dev : mDevices )
                {
                    if ( dev.isSelected() )
                    {
                        PreferenceUtil.setObjectToPrefer( ScanActivity.this,
                                                          ConstVal.DEV_PREFER_FILENAME,
                                                          dev.getPrefer(),
                                                          dev.getPrefer().getDeviceMac());
                    }
                }
                setResult( 1 );
                finish();
            }
        } );
    }

    private void decodeScanData ( final String mac, String name, byte[] bytes )
    {
//        macNames.put( mac, name );
        if ( scannedAddress.contains( mac ) )
        {
            return;
        }
        if ( storedAddress != null && storedAddress.containsKey( mac ) )
        {
            mDevices.add( new SelectDevice( false, true, storedAddress.get( mac )) );
            scannedAddress.add( mac );
            runOnUiThread( new Runnable() {
                @Override
                public void run ()
                {
                    mScanAdapter.notifyItemInserted( mDevices.size() - 1 );
                }
            } );
            return;
        }
        short devid = 0;
        //no mfr data
        if ( bytes == null )
        {
            mDevices.add( new SelectDevice( false, false, new DevicePrefer( devid, mac, name ) ) );
            scannedAddress.add( mac );
            runOnUiThread( new Runnable() {
                @Override
                public void run ()
                {
                    mScanAdapter.notifyItemInserted( mDevices.size() - 1 );
                    BleManager.getInstance().connectDevice( mac );
                }
            } );
            return;
        }
//        if ( bytes != null && bytes.length > 3 )
//        {
            for ( int i = 0; i < 4 && i < bytes.length; i++ )
            {
                if ( bytes[i] >= 0x30 && bytes[i] <= 0x39 )
                {
                    devid = (short) ( ( devid << 4 ) | ( bytes[i] - 0x30 ) );
                }
                else
                {
                    break;
                }
//                else
//                {
//                    devid = 0;
//                    BleManager.getInstance()
//                              .connectDevice( mac );
//                    return;
//                }
            }
            boolean selectable = DeviceUtil.isCorrectDevType( devid );
//            if ( selectable )
//            {
                mDevices.add( new SelectDevice( selectable, false, new DevicePrefer( devid, mac, name ) ) );
                scannedAddress.add( mac );
                runOnUiThread( new Runnable() {
                    @Override
                    public void run ()
                    {
                        mScanAdapter.notifyItemInserted( mDevices.size() - 1 );
                    }
                } );
//                return;
//            }
//        }
//        else
//        {
//            mDevices.add( new SelectDevice( false, false, new DevicePrefer( devid, mac, name ) ) );
//        }
//        BleManager.getInstance().connectDevice( mac );
    }

    private void decodeMfrData( String mac, String s )
    {
        byte[] mfr = DataUtil.hexToByteArray( s.replace( " ", "" ) );
//        LogUtil.d( TAG, "onReadMfr: " + mac + "\t" + s );
        short devid;
        if ( mfr == null || mfr.length < 2 )
        {
            devid = 0;
        }
        else
        {
            devid = (short) ( ( ( mfr[0] & 0xFF ) << 8 ) | ( mfr[1] & 0xFF ) );
        }
        if ( !DeviceUtil.isCorrectDevType( devid ) )
        {
            return;
        }
        for ( int i = 0; i < mDevices.size(); i++ )
        {
            if ( mDevices.get( i ).getPrefer().getDeviceMac().equals( mac ) )
            {
                mDevices.get( i ).getPrefer().setDevId( devid );
                mDevices.get( i ).setSelectable( true );
                final int position = i;
                runOnUiThread( new Runnable()
                 {
                     @Override
                     public void run ()
                     {
                         mScanAdapter.notifyItemChanged( position );
                     }
                 } );
                break;
            }
        }
    }
}
