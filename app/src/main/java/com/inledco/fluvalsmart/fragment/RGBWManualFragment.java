package com.inledco.fluvalsmart.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gigamole.library.ArcProgressStackView;
import com.inledco.blemanager.BleCommunicateListener;
import com.inledco.blemanager.BleManager;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.constant.CustomColor;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class RGBWManualFragment extends BaseFragment implements View.OnClickListener,View.OnLongClickListener
{
    private static final byte DYN_THUNDER1 = 1;
    private static final byte DYN_THUNDER2 = 2;
    private static final byte DYN_THUNDER3 = 3;
    private static final byte DYN_ALLCOLOR = 4;
    private static final byte DYN_CLOUD1 = 5;
    private static final byte DYN_CLOUD2 = 6;
    private static final byte DYN_CLOUD3 = 7;
    private static final byte DYN_CLOUD4 = 8;
    private static final byte DYN_MOON1 = 9;
    private static final byte DYN_MOON2 = 10;
    private static final byte DYN_MOON3 = 11;
    private static final byte DYN_SUNRS = 12;
    private static final byte DYN_PAUSE = 13;

    private ImageButton rgbw_preset1;
    private ImageButton rgbw_preset2;
    private ImageButton rgbw_preset3;
    private ImageButton rgbw_preset4;
    private ImageButton rgbw_preset5;
    private ImageButton rgbw_preset6;
    private CheckableImageButton rgbw_onoff;
    private CheckableImageButton rgbw_pause;
    private ImageButton rgbw_inc_red;
    private ImageButton rgbw_inc_green;
    private ImageButton rgbw_inc_blue;
    private ImageButton rgbw_inc_white;
    private ImageButton rgbw_dec_red;
    private ImageButton rgbw_dec_green;
    private ImageButton rgbw_dec_blue;
    private ImageButton rgbw_dec_white;
    private ArcProgressStackView rgbw_p1;
    private ArcProgressStackView rgbw_p2;
    private ArcProgressStackView rgbw_p3;
    private ArcProgressStackView rgbw_p4;
    private ImageButton rgbw_moon1;
    private ImageButton rgbw_moon2;
    private ImageButton rgbw_moon3;
    private ImageButton rgbw_sunrs;
    private ImageButton rgbw_cloud1;
    private ImageButton rgbw_cloud2;
    private ImageButton rgbw_cloud3;
    private ImageButton rgbw_cloud4;
    private ImageButton rgbw_thunder1;
    private ImageButton rgbw_thunder2;
    private ImageButton rgbw_thunder3;
    private ImageButton rgbw_allcolor;

    private LightManual mLightManual;
    private static String mAddress;
    private short devid;

    private BleCommunicateListener mCommunicateListener;

    private byte[] p1Brt;
    private byte[] p2Brt;
    private byte[] p3Brt;
    private byte[] p4Brt;
    public static RGBWManualFragment newInstance( String address, short id, LightManual manual )
    {
        RGBWManualFragment frag = new RGBWManualFragment();
        Bundle bundle = new Bundle();
        bundle.putString( "address", address );
        bundle.putShort( "id", id );
        bundle.putSerializable( "manual", manual );
        frag.setArguments( bundle );

        return frag;
    }

    @Override
    public void onCreate ( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Bundle bundle = getArguments();
        mAddress = bundle.getString( "address" );
        devid = bundle.getShort( "id" );
        mLightManual = (LightManual) bundle.getSerializable( "manual" );
    }

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_rgbwmanual, container, false );
        initView( view );
        initData();
        return view;
    }

    @Override
    public void onResume ()
    {
        super.onResume();
    }

    @Override
    public void onPause ()
    {
        super.onPause();
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();
        BleManager.getInstance().removeBleCommunicateListener( mCommunicateListener );
    }

    @Override
    protected void initView( View view )
    {
        rgbw_preset1 = (ImageButton) view.findViewById( R.id.rgbw_preset1 );
        rgbw_preset2 = (ImageButton) view.findViewById( R.id.rgbw_preset2 );
        rgbw_preset3 = (ImageButton) view.findViewById( R.id.rgbw_preset3 );
        rgbw_preset4 = (ImageButton) view.findViewById( R.id.rgbw_preset4 );
        rgbw_preset5 = (ImageButton) view.findViewById( R.id.rgbw_preset5 );
        rgbw_preset6 = (ImageButton) view.findViewById( R.id.rgbw_preset6 );
        rgbw_onoff = (CheckableImageButton) view.findViewById( R.id.rgbw_onoff );
        rgbw_pause = (CheckableImageButton) view.findViewById( R.id.rgbw_pause );
        rgbw_inc_red = (ImageButton) view.findViewById( R.id.rgbw_inc_red);
        rgbw_inc_green = (ImageButton) view.findViewById( R.id.rgbw_inc_green);
        rgbw_inc_blue = (ImageButton) view.findViewById( R.id.rgbw_inc_blue);
        rgbw_inc_white = (ImageButton) view.findViewById( R.id.rgbw_inc_white);
        rgbw_dec_red = (ImageButton) view.findViewById( R.id.rgbw_dec_red);
        rgbw_dec_green = (ImageButton) view.findViewById( R.id.rgbw_dec_green);
        rgbw_dec_blue = (ImageButton) view.findViewById( R.id.rgbw_dec_blue);
        rgbw_dec_white = (ImageButton) view.findViewById( R.id.rgbw_dec_white);
        rgbw_p1 = (ArcProgressStackView) view.findViewById( R.id.rgbw_p1 );
        rgbw_p2 = (ArcProgressStackView) view.findViewById( R.id.rgbw_p2 );
        rgbw_p3 = (ArcProgressStackView) view.findViewById( R.id.rgbw_p3 );
        rgbw_p4 = (ArcProgressStackView) view.findViewById( R.id.rgbw_p4 );
        rgbw_moon1 = (ImageButton) view.findViewById( R.id.rgbw_moon1);
        rgbw_moon2 = (ImageButton) view.findViewById( R.id.rgbw_moon2);
        rgbw_moon3 = (ImageButton) view.findViewById( R.id.rgbw_moon3);
        rgbw_sunrs = (ImageButton) view.findViewById( R.id.rgbw_sunrs);
        rgbw_cloud1 = (ImageButton) view.findViewById( R.id.rgbw_cloud1);
        rgbw_cloud2 = (ImageButton) view.findViewById( R.id.rgbw_cloud2);
        rgbw_cloud3 = (ImageButton) view.findViewById( R.id.rgbw_cloud3);
        rgbw_cloud4 = (ImageButton) view.findViewById( R.id.rgbw_cloud4);
        rgbw_thunder1 = (ImageButton) view.findViewById( R.id.rgbw_thunder1);
        rgbw_thunder2 = (ImageButton) view.findViewById( R.id.rgbw_thunder2);
        rgbw_thunder3 = (ImageButton) view.findViewById( R.id.rgbw_thunder3);
        rgbw_allcolor = (ImageButton) view.findViewById( R.id.rgbw_allcolor);
        ArrayList<ArcProgressStackView.Model> models1 = new ArrayList<>();
        ArrayList<ArcProgressStackView.Model> models2 = new ArrayList<>();
        ArrayList<ArcProgressStackView.Model> models3 = new ArrayList<>();
        ArrayList<ArcProgressStackView.Model> models4 = new ArrayList<>();
        Channel[] chns = DeviceUtil.getLightChannel( getContext(), devid );
        for ( int i = 0; i < chns.length; i++ )
        {
            models1.add( new ArcProgressStackView.Model( "", 0, CustomColor.GRAY, chns[i].getColor() ) );
            models2.add( new ArcProgressStackView.Model( "", 0, CustomColor.GRAY, chns[i].getColor() ) );
            models3.add( new ArcProgressStackView.Model( "", 0, CustomColor.GRAY, chns[i].getColor() ) );
            models4.add( new ArcProgressStackView.Model( "", 0, CustomColor.GRAY, chns[i].getColor() ) );
        }
        rgbw_p1.setDrawWidthDimension( models1.size()*6 );
        rgbw_p1.setModels( models1 );
        rgbw_p2.setDrawWidthDimension( models1.size()*6 );
        rgbw_p2.setModels( models2 );
        rgbw_p3.setDrawWidthDimension( models1.size()*6 );
        rgbw_p3.setModels( models3 );
        rgbw_p4.setDrawWidthDimension( models1.size()*6 );
        rgbw_p4.setModels( models4 );

        rgbw_preset1.setOnClickListener( this );
        rgbw_preset2.setOnClickListener( this );
        rgbw_preset3.setOnClickListener( this );
        rgbw_preset4.setOnClickListener( this );
        rgbw_preset5.setOnClickListener( this );
        rgbw_preset6.setOnClickListener( this );
        rgbw_onoff.setOnClickListener( this );
        rgbw_pause.setOnClickListener( this );
        rgbw_inc_red.setOnClickListener( this );
        rgbw_inc_green.setOnClickListener( this );
        rgbw_inc_blue.setOnClickListener( this );
        rgbw_inc_white.setOnClickListener( this );
        rgbw_dec_red.setOnClickListener( this );
        rgbw_dec_green.setOnClickListener( this );
        rgbw_dec_blue.setOnClickListener( this );
        rgbw_dec_white.setOnClickListener( this );
        rgbw_p1.setOnClickListener( this );
        rgbw_p2.setOnClickListener( this );
        rgbw_p3.setOnClickListener( this );
        rgbw_p4.setOnClickListener( this );
        rgbw_p1.setOnClickListener( this );
        rgbw_moon1.setOnClickListener( this );
        rgbw_moon2.setOnClickListener( this );
        rgbw_moon3.setOnClickListener( this );
        rgbw_sunrs.setOnClickListener( this );
        rgbw_cloud1.setOnClickListener( this );
        rgbw_cloud2.setOnClickListener( this );
        rgbw_cloud3.setOnClickListener( this );
        rgbw_cloud4.setOnClickListener( this );
        rgbw_thunder1.setOnClickListener( this );
        rgbw_thunder2.setOnClickListener( this );
        rgbw_thunder3.setOnClickListener( this );
        rgbw_allcolor.setOnClickListener( this );

        rgbw_p1.setOnLongClickListener( this );
        rgbw_p2.setOnLongClickListener( this );
        rgbw_p3.setOnLongClickListener( this );
        rgbw_p4.setOnLongClickListener( this );
        rgbw_inc_red.setOnLongClickListener( this );
        rgbw_inc_green.setOnLongClickListener( this );
        rgbw_inc_blue.setOnLongClickListener( this );
        rgbw_inc_white.setOnLongClickListener( this );
        rgbw_dec_red.setOnLongClickListener( this );
        rgbw_dec_green.setOnLongClickListener( this );
        rgbw_dec_blue.setOnLongClickListener( this );
        rgbw_dec_white.setOnLongClickListener( this );
    }

    @Override
    protected void initEvent ()
    {

    }

    @Override
    protected void initData()
    {
        mCommunicateListener = new BleCommunicateListener() {
            @Override
            public void onDataValid ( String mac )
            {

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
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {
                if ( mac.equals( mAddress ) )
                {
                    Object object = CommUtil.decodeLight( list, devid );
                    if ( object != null && object instanceof LightManual )
                    {
                        mLightManual = (LightManual) object;
                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run ()
                            {
                                refreshData();
                            }
                        } );
                    }
                }
            }
        };
        BleManager.getInstance().addBleCommunicateListener( mCommunicateListener );

        refreshData();
    }

    private void refreshData()
    {
        rgbw_onoff.setChecked( mLightManual.isOn() );
        p1Brt = mLightManual.getCustomP1Values();
        p2Brt = mLightManual.getCustomP2Values();
        p3Brt = mLightManual.getCustomP3Values();
        p4Brt = mLightManual.getCustomP4Values();
        for ( int i = 0; i < DeviceUtil.getChannelCount( devid ); i++ )
        {
            rgbw_p1.getModels().get( i ).setProgress( p1Brt[i] );
            rgbw_p2.getModels().get( i ).setProgress( p2Brt[i] );
            rgbw_p3.getModels().get( i ).setProgress( p3Brt[i] );
            rgbw_p4.getModels().get( i ).setProgress( p4Brt[i] );
        }
        rgbw_p1.invalidate();
        rgbw_p2.invalidate();
        rgbw_p3.invalidate();
        rgbw_p4.invalidate();
    }

    private byte getChannel( int id )
    {
        byte chn = (byte) 0xFF;
        switch ( id )
        {
            case R.id.rgbw_inc_red:
            case R.id.rgbw_dec_red:
                chn = 0x00;
                break;
            case R.id.rgbw_inc_green:
            case R.id.rgbw_dec_green:
                chn = 0x01;
                break;
            case R.id.rgbw_inc_blue:
            case R.id.rgbw_dec_blue:
                chn = 0x02;
                break;
            case R.id.rgbw_inc_white:
            case R.id.rgbw_dec_white:
                chn = 0x03;
                break;
        }
        return chn;
    }

    @Override
    public void onClick ( View view )
    {
        short[] values;
        byte chn;
        int id = view.getId();
        switch ( id )
        {
            case R.id.rgbw_preset1:
//                CommUtil.setLed( mAddress, new short[]{1000, 500, 0, 1000} );
                CommUtil.setLed( mAddress, new short[]{1000, 250, 0, 0} );
                break;
            case R.id.rgbw_preset2:
//                CommUtil.setLed( mAddress, new short[]{0, 0, 1000, 1000} );
                CommUtil.setLed( mAddress, new short[]{0, 0, 1000, 0} );
                break;
            case R.id.rgbw_preset3:
//                CommUtil.setLed( mAddress, new short[]{1000, 0, 1000, 1000} );
                CommUtil.setLed( mAddress, new short[]{1000, 0, 1000, 0} );
                break;
            case R.id.rgbw_preset4:
                CommUtil.setLed( mAddress, new short[]{0, 0, 0, 1000} );
                break;
            case R.id.rgbw_preset5:
//                CommUtil.setLed( mAddress, new short[]{1000, 1000, 1000, 1000} );
                CommUtil.setLed( mAddress, new short[]{1000, 750, 0, 0} );
                break;
            case R.id.rgbw_preset6:
//                CommUtil.setLed( mAddress, new short[]{1000, 0, 1000, 500} );
                CommUtil.setLed( mAddress, new short[]{250, 0, 1000, 0} );
                break;
            case R.id.rgbw_onoff:
                if ( rgbw_onoff.isChecked() )
                {
                    CommUtil.turnOffLed( mAddress );
                }
                else
                {
                    CommUtil.turnOnLed( mAddress );
                }
                break;
            case R.id.rgbw_inc_red:
            case R.id.rgbw_inc_green:
            case R.id.rgbw_inc_blue:
            case R.id.rgbw_inc_white:
                chn = getChannel( id );
                CommUtil.increaseBright( mAddress, chn, (byte) 0xC8 );
                break;
            case R.id.rgbw_dec_red:
            case R.id.rgbw_dec_green:
            case R.id.rgbw_dec_blue:
            case R.id.rgbw_dec_white:
                chn = getChannel( id );
                CommUtil.decreaseBright( mAddress, chn, (byte) 0xC8 );
                break;
            case R.id.rgbw_p1:
                values = new short[p1Brt.length];
                for ( int i = 0; i < p1Brt.length; i++ )
                {
                    values[i] = (short) ( ( p1Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed( mAddress, values );
                break;
            case R.id.rgbw_p2:
                values = new short[p2Brt.length];
                for ( int i = 0; i < p2Brt.length; i++ )
                {
                    values[i] = (short) ( ( p2Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed( mAddress, values );
                break;
            case R.id.rgbw_p3:
                values = new short[p3Brt.length];
                for ( int i = 0; i < p3Brt.length; i++ )
                {
                    values[i] = (short) ( ( p3Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed( mAddress, values );
                break;
            case R.id.rgbw_p4:
                values = new short[p4Brt.length];
                for ( int i = 0; i < p4Brt.length; i++ )
                {
                    values[i] = (short) ( ( p4Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed( mAddress, values );
                break;
            case R.id.rgbw_moon1:
                CommUtil.sendKey( mAddress, DYN_MOON1 );
                break;
            case R.id.rgbw_moon2:
                CommUtil.sendKey( mAddress, DYN_MOON2 );
                break;
            case R.id.rgbw_moon3:
                CommUtil.sendKey( mAddress, DYN_MOON3 );
                break;
            case R.id.rgbw_sunrs:
                CommUtil.sendKey( mAddress, DYN_SUNRS );
                break;
            case R.id.rgbw_cloud1:
                CommUtil.sendKey( mAddress, DYN_CLOUD1 );
                break;
            case R.id.rgbw_cloud2:
                CommUtil.sendKey( mAddress, DYN_CLOUD2 );
                break;
            case R.id.rgbw_cloud3:
                CommUtil.sendKey( mAddress, DYN_CLOUD3 );
                break;
            case R.id.rgbw_cloud4:
                CommUtil.sendKey( mAddress, DYN_CLOUD4 );
                break;
            case R.id.rgbw_thunder1:
                CommUtil.sendKey( mAddress, DYN_THUNDER1 );
                break;
            case R.id.rgbw_thunder2:
                CommUtil.sendKey( mAddress, DYN_THUNDER2 );
                break;
            case R.id.rgbw_thunder3:
                CommUtil.sendKey( mAddress, DYN_THUNDER3 );
                break;
            case R.id.rgbw_allcolor:
                CommUtil.sendKey( mAddress, DYN_ALLCOLOR );
                break;
            case R.id.rgbw_pause:
                CommUtil.sendKey( mAddress, DYN_PAUSE );
                break;
        }
    }

    @Override
    public boolean onLongClick ( final View view )
    {
        final byte chn;
        int id = view.getId();
        switch ( id )
        {
            case R.id.rgbw_p1:
                CommUtil.setLedCustom( mAddress, (byte) 0x00 );
                break;
            case R.id.rgbw_p2:
                CommUtil.setLedCustom( mAddress, (byte) 0x01 );
                break;
            case R.id.rgbw_p3:
                CommUtil.setLedCustom( mAddress, (byte) 0x02 );
                break;
            case R.id.rgbw_p4:
                CommUtil.setLedCustom( mAddress, (byte) 0x03 );
                break;
            case R.id.rgbw_inc_red:
            case R.id.rgbw_inc_green:
            case R.id.rgbw_inc_blue:
            case R.id.rgbw_inc_white:
                chn = getChannel( id );
                new Timer().schedule( new TimerTask() {
                    @Override
                    public void run ()
                    {
                        if ( view.isPressed() )
                        {
                            CommUtil.increaseBright( mAddress, chn, (byte) 0x0A );
                        }
                        else
                        {
                            this.cancel();
                        }
                    }
                }, 0, 40 );
                break;
            case R.id.rgbw_dec_red:
            case R.id.rgbw_dec_green:
            case R.id.rgbw_dec_blue:
            case R.id.rgbw_dec_white:
                chn = getChannel( id );
                new Timer().schedule( new TimerTask() {
                    @Override
                    public void run ()
                    {
                        if ( view.isPressed() )
                        {
                            CommUtil.decreaseBright( mAddress, chn, (byte) 0x0A );
                        }
                        else
                        {
                            this.cancel();
                        }
                    }
                }, 0, 40);
                break;
        }
        return true;
    }
}
