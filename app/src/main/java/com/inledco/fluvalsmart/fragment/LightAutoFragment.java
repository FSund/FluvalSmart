package com.inledco.fluvalsmart.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.blemanager.BleCommunicateListener;
import com.inledco.blemanager.BleManager;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.adapter.ExpanSliderAdapter;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightAuto;
import com.inledco.fluvalsmart.bean.RampTime;
import com.inledco.fluvalsmart.constant.CustomColor;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LightAutoProfileUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightAutoFragment extends BaseFragment
{
    private static final int EDIT_ITEM_SUNRISE = 1;
    private static final int EDIT_ITEM_MIDDAY = 2;
    private static final int EDIT_ITEM_SUNSET = 3;
    private static final int EDIT_ITEM_NIGHT = 4;

    private LightAuto mLightAuto;
    private short devid;
    private String mAddress;

    private LineChart lightautochart;
    private Button lightautoimport;
    private Button lightautoexport;
    private ToggleButton light_auto_preview;
    private TextView auto_sunrs_time;
    private TextView auto_midday_brt;
    private TextView auto_sunset_time;
    private TextView auto_night_brt;
    private LinearLayout light_auto_dynamic_show;
    private ImageView light_auto_dynamic_icon;
    private TextView light_auto_dynamic;

    private LineData mLineData;
    private ArrayList< ILineDataSet > mDataSets;

    private Timer tmr;
    private PreviewTimerTask tsk;

    private BleCommunicateListener mCommunicateListener;

    public static LightAutoFragment newInstance ( String address, short id, LightAuto auto )
    {
        LightAutoFragment frag = new LightAutoFragment();
        Bundle bundle = new Bundle();
        bundle.putString( "address", address );
        bundle.putShort( "id", id );
        bundle.putSerializable( "auto", auto );
        frag.setArguments( bundle );

        return frag;
    }

    @Override
    public void onCreate ( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Bundle bundle = getArguments();
        mLightAuto = (LightAuto) bundle.getSerializable( "auto" );
        devid = bundle.getShort( "id" );
        mAddress = bundle.getString( "address" );
    }

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_light_auto, container, false );
        initView( view );
        initData();
        initEvent();
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
        if ( light_auto_preview.isChecked() )
        {
            light_auto_preview.setChecked( false );
        }
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();
        BleManager.getInstance().removeBleCommunicateListener( mCommunicateListener );
    }

    @Override
    protected void initView ( View view )
    {
        auto_sunrs_time = view.findViewById( R.id.auto_sunrs_time );
        auto_midday_brt = view.findViewById( R.id.auto_midday_brt );
        auto_sunset_time = view.findViewById( R.id.auto_sunset_time );
        auto_night_brt = view.findViewById( R.id.auto_night_brt );
        light_auto_preview = view.findViewById( R.id.light_auto_preview );
        lightautoexport = view.findViewById( R.id.light_auto_export );
        lightautoimport = view.findViewById( R.id.light_auto_import );
        lightautochart = view.findViewById( R.id.light_auto_chart );
        light_auto_dynamic_show = view.findViewById( R.id.light_auto_dynamic_show );
        light_auto_dynamic_icon = view.findViewById( R.id.light_auto_dynamic_icon );
        light_auto_dynamic = view.findViewById( R.id.light_auto_dynamic );

        XAxis xAxis = lightautochart.getXAxis();
        YAxis axisLeft = lightautochart.getAxisLeft();
        YAxis axisRight = lightautochart.getAxisRight();
        xAxis.setAxisMaximum( 24 * 60 );
        xAxis.setAxisMinimum( 0 );
        xAxis.setLabelCount( 5, true );
        xAxis.setGranularity( 1 );
        xAxis.setGranularityEnabled( true );
        xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );
        xAxis.setDrawGridLines( false );
        xAxis.setDrawAxisLine( false );
        xAxis.setTextColor( Color.WHITE );
        xAxis.setEnabled( true );
        axisLeft.setAxisMaximum( 100 );
        axisLeft.setAxisMinimum( 0 );
        axisLeft.setLabelCount( 5, true );
        axisLeft.setValueFormatter( new PercentFormatter( new DecimalFormat( "##0" ) ) );
        axisLeft.setPosition( YAxis.YAxisLabelPosition.OUTSIDE_CHART );
        axisLeft.setTextColor( Color.WHITE );
        axisLeft.setDrawGridLines( true );
        axisLeft.setGridColor( 0xFF9E9E9E );
        axisLeft.setGridLineWidth( 0.75f );
        axisLeft.setDrawAxisLine( false );
        axisLeft.setAxisLineColor( Color.WHITE );
        axisLeft.setGranularity( 1 );
        axisLeft.setGranularityEnabled( true );
        axisLeft.setSpaceTop( 0 );
        axisLeft.setSpaceBottom( 0 );
        axisLeft.setEnabled( true );
        axisRight.setEnabled( false );
        lightautochart.setTouchEnabled( false );
        lightautochart.setDragEnabled( false );
        lightautochart.setScaleEnabled( false );
        lightautochart.setPinchZoom( false );
        lightautochart.setDoubleTapToZoomEnabled( false );
        lightautochart.setBorderColor( Color.CYAN );
        lightautochart.setBorderWidth( 1 );
        lightautochart.setDrawBorders( false );
        lightautochart.setDrawGridBackground( true );
        lightautochart.setGridBackgroundColor( Color.TRANSPARENT );
        lightautochart.setDescription( null );
        lightautochart.setMaxVisibleValueCount( 0 );
        lightautochart.getLegend().setTextColor( Color.WHITE );
        final String[] hours = new String[]{ "00:00", "06:00", "12:00", "18:00", "00:00" };
        IAxisValueFormatter formatter = new IAxisValueFormatter()
        {
            @Override
            public String getFormattedValue ( float value, AxisBase axis )
            {
                return hours[(int) ( value / 360 )];
            }

            // we don't draw numbers, so no decimal digits needed
            @Override
            public int getDecimalDigits ()
            {
                return 0;
            }
        };
        xAxis.setValueFormatter( formatter );
    }

    @Override
    protected void initData ()
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
            public void onReadPassword ( String mac, int psw )
            {

            }

            @Override
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {
                if ( mac.equals( mAddress ) )
                {
                    Object object = CommUtil.decodeLight( list, devid );
                    if ( object != null && object instanceof LightAuto )
                    {
                        final long t = System.currentTimeMillis();
                        mLightAuto = (LightAuto) object;
                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run ()
                            {
                                refreshData();
                                Toast.makeText( getContext(), R.string.load_success, Toast.LENGTH_SHORT )
                                     .show();
                            }
                        } );
                    }
                }
            }
        };
        BleManager.getInstance().addBleCommunicateListener( mCommunicateListener );
        refreshData();
        tmr = new Timer();
    }

    private int getDynamicRes( int index )
    {
        int res = 0;
        switch ( index )
        {
            case 1:
                res = R.mipmap.ic_thunder1;
                break;
            case 2:
                res = R.mipmap.ic_thunder2;
                break;
            case 3:
                res = R.mipmap.ic_thunder3;
                break;
            case 4:
                res = R.mipmap.ic_allcolor;
                break;
            case 5:
                res = R.mipmap.ic_cloud1;
                break;
            case 6:
                res = R.mipmap.ic_cloud2;
                break;
            case 7:
                res = R.mipmap.ic_cloud3;
                break;
            case 8:
                res = R.mipmap.ic_cloud4;
                break;
            case 9:
                res = R.mipmap.ic_moon1;
                break;
            case 10:
                res = R.mipmap.ic_moon2;
                break;
            case 11:
                res = R.mipmap.ic_moon3;
                break;
        }
        return res;
    }

    private void refreshData()
    {
        int sunrise_starthour = mLightAuto.getSunrise().getStartHour();
        int sunrise_startminute = mLightAuto.getSunrise().getStartMinute();
        int sunrise_endhour = mLightAuto.getSunrise().getEndHour();
        int sunrise_endminute = mLightAuto.getSunrise().getEndMinute();
        int sunset_starthour = mLightAuto.getSunset().getStartHour();
        int sunset_startminute = mLightAuto.getSunset().getStartMinute();
        int sunset_endhour = mLightAuto.getSunset().getEndHour();
        int sunset_endminute = mLightAuto.getSunset().getEndMinute();

        if ( mDataSets == null )
        {
            mDataSets = new ArrayList<>();
        }
        mDataSets.clear();
        int dlen = mLightAuto.getDayBright().length;
        int nlen = mLightAuto.getNightBright().length;
        Context context = getContext();
        if ( context == null )
        {
            return;
        }
        Channel[] channels = DeviceUtil.getLightChannel( context, devid );
        if ( dlen == nlen )
        {

            for ( int i = 0; i < dlen; i++ )
            {
                List< Entry > entry = new ArrayList<>();
                entry.add( new Entry( 0, mLightAuto.getNightBright()[i] & 0xFF ) );
                entry.add( new Entry( ( sunrise_starthour & 0xFF ) * 60 + ( sunrise_startminute & 0xFF ), mLightAuto.getNightBright()[i] & 0xFF ) );
                entry.add( new Entry( ( sunrise_endhour & 0xFF ) * 60 + ( sunrise_endminute & 0xFF ), mLightAuto.getDayBright()[i] & 0xFF ) );
                entry.add( new Entry( ( sunset_starthour & 0xFF ) * 60 + ( sunset_startminute & 0xFF ), mLightAuto.getDayBright()[i] & 0xFF ) );
                entry.add( new Entry( ( sunset_endhour & 0xFF ) * 60 + ( sunset_endminute & 0xFF ), mLightAuto.getNightBright()[i] & 0xFF ) );
                entry.add( new Entry( 24 * 60, mLightAuto.getNightBright()[i] & 0xFF ) );
                LineDataSet lineDataSet = new LineDataSet( entry, channels[i].getName() );
                lineDataSet.setColor( channels[i].getColor() );
                lineDataSet.setCircleRadius( 3.0f );
                lineDataSet.setCircleColor( channels[i].getColor() );
                lineDataSet.setDrawCircleHole( false );
                lineDataSet.setLineWidth( 2.0f );
                mDataSets.add( lineDataSet );
            }
            mLineData = new LineData( mDataSets );
            lightautochart.setData( mLineData );
            lightautochart.invalidate();
        }

        DecimalFormat df = new DecimalFormat( "00" );
        auto_sunrs_time.setText( df.format( sunrise_starthour ) + ":" + df.format( sunrise_startminute ) +
                              " - " + df.format( sunrise_endhour ) + ":" + df.format( sunrise_endminute ) );
        auto_sunset_time.setText( df.format( sunset_starthour ) + ":" + df.format( sunset_startminute ) +
                             " - " + df.format( sunset_endhour ) + ":" + df.format( sunset_endminute ) );
        String txtd = "";
        String txtn = "";
        for ( int i = 0; i < channels.length; i++ )
        {
            txtd = txtd + channels[i].getName() + ": " + mLightAuto.getDayBright()[i] + "%\n";
            txtn = txtn + channels[i].getName() + ": " + mLightAuto.getNightBright()[i] + "%\n";
        }
        auto_midday_brt.setText( txtd.substring( 0, txtd.lastIndexOf( "\n" ) ) );
        auto_night_brt.setText( txtn.substring( 0, txtn.lastIndexOf( "\n" ) ) );
        if ( mLightAuto.isHasDynamic() )
        {
            light_auto_dynamic_show.setVisibility( View.VISIBLE );
            int temp = mLightAuto.getWeek()&0xFF;
            if ( temp > 0x80 && mLightAuto.getDynamicMode() > 0 && mLightAuto.getDynamicMode() < 12 )
            {
                StringBuffer sb = new StringBuffer();
                if ( mLightAuto.isSun() )
                {
                    sb.append( getString( R.string.weekday_sun ) );
                }
                if ( mLightAuto.isMon() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_mon ) );
                }
                if ( mLightAuto.isTue() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_tue ) );
                }
                if ( mLightAuto.isWed() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_wed ) );
                }
                if ( mLightAuto.isThu() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_thu ) );
                }
                if ( mLightAuto.isFri() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_fri ) );
                }
                if ( mLightAuto.isSat() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_sat ) );
                }
                sb.append( "\r\n" )
                  .append( df.format( mLightAuto.getDynamicPeriod().getStartHour() ) )
                  .append( ":" )
                  .append( df.format( mLightAuto.getDynamicPeriod().getStartMinute() ) )
                  .append( " - " )
                  .append( df.format( mLightAuto.getDynamicPeriod().getEndHour() ) )
                  .append( ":" )
                  .append( df.format( mLightAuto.getDynamicPeriod().getEndMinute() ) );
                light_auto_dynamic.setText( sb );
                light_auto_dynamic_icon.setImageResource( getDynamicRes( mLightAuto.getDynamicMode() ) );
            }
            else
            {
                light_auto_dynamic.setText( R.string.light_auto_dynamic_disabled );
                light_auto_dynamic_icon.setImageResource( R.drawable.ic_block_white_36dp );
            }
        }
        else
        {
            light_auto_dynamic_show.setVisibility( View.GONE );
        }
    }

    private short[] getBright ( int ct )
    {
        int chns = DeviceUtil.getLightChannel( getContext(), devid ).length;
        short[] values = new short[chns];
        RampTime sunrise = mLightAuto.getSunrise();
        RampTime sunset = mLightAuto.getSunset();
        int[] tms = new int[]{ ( sunrise.getStartHour() & 0xFF ) * 60 + ( sunrise.getStartMinute() & 0xFF ),
                               ( sunrise.getEndHour() & 0xFF ) * 60 + ( sunrise.getEndMinute() & 0xFF ),
                               ( sunset.getStartHour() & 0xFF ) * 60 + ( sunset.getStartMinute() & 0xFF ),
                               ( sunset.getEndHour() & 0xFF ) * 60 + ( sunset.getEndMinute() & 0xFF ) };
        byte[][] vals = new byte[4][];
        vals[0] = new byte[chns];
        vals[1] = new byte[chns];
        vals[2] = new byte[chns];
        vals[3] = new byte[chns];
        for ( int i = 0; i < chns; i++ )
        {
            vals[0][i] = mLightAuto.getNightBright()[i];
            vals[1][i] = mLightAuto.getDayBright()[i];
            vals[2][i] = mLightAuto.getDayBright()[i];
            vals[3][i] = mLightAuto.getNightBright()[i];
        }
        for ( int i = 0; i < 4; i++ )
        {
            int j = ( i + 1 ) % 4;
            int st = tms[i];
            int et = tms[j];
            int duration;
            int dt;
            int dbrt;
            if ( et >= st )
            {
                if ( ct >= st && ct < et )
                {
                    duration = et - st;
                    dt = ct - st;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                if ( ct >= st || ct < et )
                {
                    duration = 1440 - st + et;
                    if ( ct >= st )
                    {
                        dt = ct - st;
                    }
                    else
                    {
                        dt = 1440 - st + ct;
                    }
                }
                else
                {
                    continue;
                }
            }
            for ( int k = 0; k < chns; k++ )
            {
                byte sbrt = vals[i][k];
                byte ebrt = vals[j][k];
                if ( ebrt >= sbrt )
                {
                    dbrt = ebrt - sbrt;
                    values[k] = (short) ( ( sbrt & 0xFF ) * 10 + dbrt * 10 * dt / duration );
                }
                else
                {
                    dbrt = sbrt - ebrt;
                    values[k] = (short) ( ( sbrt & 0xFF ) * 10 - dbrt * 10 * dt / duration );
                }
            }
        }
        return values;
    }

    @Override
    protected void initEvent ()
    {
        auto_sunrs_time.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showEditSunrsDialog( EDIT_ITEM_SUNRISE );
            }
        } );
        auto_midday_brt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showEditDayNightDialog( EDIT_ITEM_MIDDAY );
            }
        } );
        auto_sunset_time.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showEditSunrsDialog( EDIT_ITEM_SUNSET );
            }
        } );
        auto_night_brt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showEditDayNightDialog( EDIT_ITEM_NIGHT );
            }
        } );
        //快速预览自动模式
        light_auto_preview.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged ( CompoundButton buttonView, boolean isChecked )
            {
                if ( isChecked )
                {
                    tsk = new PreviewTimerTask();
                    tsk.setListener( new PreviewTaskListener()
                    {
                        @Override
                        public void onFinish ()
                        {
                            getActivity().runOnUiThread( new Runnable()
                            {
                                @Override
                                public void run ()
                                {
                                    light_auto_preview.setChecked( false );
                                }
                            } );
                        }

                        @Override
                        public void onUpdate ( final int tm )
                        {
                            getActivity().runOnUiThread( new Runnable()
                            {
                                @Override
                                public void run ()
                                {
                                    lightautochart.getXAxis()
                                                  .removeAllLimitLines();
                                    LimitLine limitLine = new LimitLine( tm );
                                    limitLine.setLineWidth( 1 );
                                    limitLine.setLineColor( CustomColor.COLOR_ACCENT );
                                    lightautochart.getXAxis()
                                                  .addLimitLine( limitLine );
                                    lightautochart.invalidate();
                                }
                            } );
                        }
                    } );
                    tmr.schedule( tsk, 0, 40 );
                }
                else
                {
                    tsk.cancel();
                    new Handler().postDelayed( new Runnable()
                    {
                        @Override
                        public void run ()
                        {
                            CommUtil.stopPreview( mAddress );
                            lightautochart.getXAxis()
                                          .removeAllLimitLines();
                            lightautochart.invalidate();
                        }
                    }, 80 );
                }
            }
        } );

        light_auto_dynamic_show.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showDynamicDialog();
            }
        } );

        lightautoimport.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {
                showImportDialog();
            }
        } );
        lightautoexport.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {
                showExportDialog();
            }
        } );
    }

    private void showImportDialog ()
    {
        final Map< String, LightAuto > localProfiles = LightAutoProfileUtil.getLocalProfiles( getContext(), devid, mLightAuto.isHasDynamic() );
        final String[] keys = new String[localProfiles.size()];
        final int[] index = { 0 };
        int i = 0;
        for ( String s : localProfiles.keySet() )
        {
            keys[i] = s;
            i++;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        builder.setTitle( R.string.export_profile );
        builder.setSingleChoiceItems( keys, 0, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick ( DialogInterface dialogInterface, int i )
            {
                index[0] = i;
            }
        } );
        builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick ( DialogInterface dialogInterface, int i )
            {
                dialogInterface.dismiss();
            }
        } );
        builder.setPositiveButton( R.string.dialog_export_use, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick ( DialogInterface dialogInterface, int i )
            {
                mLightAuto = localProfiles.get( keys[index[0]] );
                refreshData();
                CommUtil.setLedAuto( mAddress, mLightAuto );
                dialogInterface.dismiss();
            }
        } );
        builder.setNeutralButton( R.string.dialog_export_remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick ( DialogInterface dialog, int which )
            {
                LightAutoProfileUtil.deleteProfile( getContext(), devid, keys[index[0]] );
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    private void showExportDialog ()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        final AlertDialog dialog = builder.create();
        View view = LayoutInflater.from( getContext() )
                                  .inflate( R.layout.dialog_export_profile, null );
        final EditText name = (EditText) view.findViewById( R.id.export_name );
        Button btn_cancel = (Button) view.findViewById( R.id.export_cancel );
        Button btn_ok = (Button) view.findViewById( R.id.export_ok );
        btn_cancel.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {
                dialog.dismiss();
            }
        } );
        btn_ok.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {
                if ( TextUtils.isEmpty( name.getText()
                                            .toString() ) )
                {
                    name.setError( getContext().getString( R.string.error_input_empty ) );
                }
                else
                {
                    LightAutoProfileUtil.saveProfile( getContext(),
                                                      mLightAuto,
                                                      devid,
                                                      name.getText()
                                                          .toString() );
                    dialog.dismiss();
                }
            }
        } );
        dialog.setView( view );
        dialog.setTitle( R.string.save_profile );
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    private void showEditSunrsDialog ( final int item )
    {
        int bgres;
        final int start_hour;
        final int start_minute;
        final int end_hour;
        final int end_minute;
        if ( item == EDIT_ITEM_SUNRISE )
        {
            bgres = R.mipmap.ic_sunrise;
            start_hour = mLightAuto.getSunrise().getStartHour();
            start_minute = mLightAuto.getSunrise().getStartMinute();
            end_hour = mLightAuto.getSunrise().getEndHour();
            end_minute = mLightAuto.getSunrise().getEndMinute();
        }
        else if ( item == EDIT_ITEM_SUNSET )
        {
            bgres = R.mipmap.ic_sunset;
            start_hour = mLightAuto.getSunset().getStartHour();
            start_minute = mLightAuto.getSunset().getStartMinute();
            end_hour = mLightAuto.getSunset().getEndHour();
            end_minute = mLightAuto.getSunset().getEndMinute();
        }
        else
        {
            return;
        }
        final BottomSheetDialog dialog = new BottomSheetDialog( getContext() );
        View dialogView = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_edit_sunrise_sunset, null );
        dialogView.findViewById( R.id.dialog_sunrs_bg ).setBackgroundResource( bgres );
        TimePicker tp_start = (TimePicker) dialogView.findViewById( R.id.dialog_sunrs_start );
        TimePicker tp_end = (TimePicker) dialogView.findViewById( R.id.dialog_sunrs_end );
        Button btn_cancel = (Button) dialogView.findViewById( R.id.dialog_sunrs_cancel );
        Button btn_save = (Button) dialogView.findViewById( R.id.dialog_sunrs_save );
        DatePicker dp = new DatePicker( getContext() );
        tp_start.setIs24HourView( true );
        tp_end.setIs24HourView( true );
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            tp_start.setHour( start_hour );
            tp_start.setMinute( start_minute );
            tp_end.setHour( end_hour );
            tp_end.setMinute( end_minute );
        }
        else
        {
            tp_start.setCurrentHour( start_hour );
            tp_start.setCurrentMinute( start_minute );
            tp_end.setCurrentHour( end_hour );
            tp_end.setCurrentMinute( end_minute );
        }
        tp_start.setOnTimeChangedListener( new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged ( TimePicker view, int hourOfDay, int minute )
            {
                if ( item == EDIT_ITEM_SUNRISE )
                {
                    mLightAuto.getSunrise().setStartHour( (byte) hourOfDay );
                    mLightAuto.getSunrise().setStartMinute( (byte) minute );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_SUNSET )
                {
                    mLightAuto.getSunset().setStartHour( (byte) hourOfDay );
                    mLightAuto.getSunset().setStartMinute( (byte) minute );
                    refreshData();
                }
            }
        } );
        tp_end.setOnTimeChangedListener( new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged ( TimePicker view, int hourOfDay, int minute )
            {
                if ( item == EDIT_ITEM_SUNRISE )
                {
                    mLightAuto.getSunrise().setEndHour( (byte) hourOfDay );
                    mLightAuto.getSunrise().setEndMinute( (byte) minute );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_SUNSET )
                {
                    mLightAuto.getSunset().setEndHour( (byte) hourOfDay );
                    mLightAuto.getSunset().setEndMinute( (byte) minute );
                    refreshData();
                }
            }
        } );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( item == EDIT_ITEM_SUNRISE )
                {
                    mLightAuto.getSunrise().setStartHour( (byte) start_hour );
                    mLightAuto.getSunrise().setStartMinute( (byte) start_minute );
                    mLightAuto.getSunrise().setEndHour( (byte) end_hour );
                    mLightAuto.getSunrise().setEndMinute( (byte) end_minute );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_SUNSET )
                {
                    mLightAuto.getSunset().setStartHour( (byte) start_hour );
                    mLightAuto.getSunset().setStartMinute( (byte) start_minute );
                    mLightAuto.getSunset().setEndHour( (byte) end_hour );
                    mLightAuto.getSunset().setEndMinute( (byte) end_minute );
                    refreshData();
                }
                dialog.dismiss();
            }
        } );
        btn_save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                CommUtil.setLedAuto( mAddress, mLightAuto );
                dialog.dismiss();
            }
        } );
        dialog.setContentView( dialogView );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel ( DialogInterface dialog )
            {
                if ( item == EDIT_ITEM_SUNRISE )
                {
                    mLightAuto.getSunrise().setStartHour( (byte) start_hour );
                    mLightAuto.getSunrise().setStartMinute( (byte) start_minute );
                    mLightAuto.getSunrise().setEndHour( (byte) end_hour );
                    mLightAuto.getSunrise().setEndMinute( (byte) end_minute );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_SUNSET )
                {
                    mLightAuto.getSunset().setStartHour( (byte) start_hour );
                    mLightAuto.getSunset().setStartMinute( (byte) start_minute );
                    mLightAuto.getSunset().setEndHour( (byte) end_hour );
                    mLightAuto.getSunset().setEndMinute( (byte) end_minute );
                    refreshData();
                }
            }
        } );
        dialog.show();
    }

    private void showEditDayNightDialog ( final int item )
    {
        int bgres;
        final byte[] brts;
        if ( item == EDIT_ITEM_MIDDAY )
        {
            bgres = R.mipmap.ic_midday;
            brts = Arrays.copyOf( mLightAuto.getDayBright(), mLightAuto.getDayBright().length );
        }
        else if ( item == EDIT_ITEM_NIGHT )
        {
            bgres = R.mipmap.ic_night;
            brts = Arrays.copyOf( mLightAuto.getNightBright(), mLightAuto.getNightBright().length );
        }
        else
        {
            return;
        }
        final BottomSheetDialog dialog = new BottomSheetDialog( getContext() );
        View dialogView = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_edit_day_night, null );
        dialogView.findViewById( R.id.dialog_daynight_bg ).setBackgroundResource( bgres );
        ListView dialog_daynight_lv = (ListView) dialogView.findViewById( R.id.dialog_daynight_lv );
        Button btn_cancel = (Button) dialogView.findViewById( R.id.dialog_daynight_cancel );
        Button btn_save = (Button) dialogView.findViewById( R.id.dialog_daynight_save );
        Channel[] chns = DeviceUtil.getLightChannel( getContext(), devid );
        ArrayList< Channel > channels = new ArrayList<>();
        for ( int i = 0; i < chns.length; i++ )
        {
            channels.add( new Channel( chns[i].getName(), chns[i].getColor(), brts[i] ) );
        }
        ExpanSliderAdapter adapter = new ExpanSliderAdapter( getContext(), devid, channels, new ExpanSliderAdapter.ItemChangeListener() {
            @Override
            public void onItemChanged ( int position, int newValue )
            {
                if ( item == EDIT_ITEM_MIDDAY )
                {
                    mLightAuto.getDayBright()[position] = (byte) newValue;
                    refreshData();
                }
                else if ( item == EDIT_ITEM_NIGHT )
                {
                    mLightAuto.getNightBright()[position] = (byte) newValue;
                    refreshData();
                }
            }
        } );
        dialog_daynight_lv.setAdapter( adapter );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( item == EDIT_ITEM_MIDDAY )
                {
                    mLightAuto.setDayBright( brts );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_NIGHT )
                {
                    mLightAuto.setNightBright( brts );
                    refreshData();
                }
                dialog.dismiss();
            }
        } );
        btn_save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                CommUtil.setLedAuto( mAddress, mLightAuto );
                dialog.dismiss();
            }
        } );
        dialog.setContentView( dialogView );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel ( DialogInterface dialog )
            {
                if ( item == EDIT_ITEM_MIDDAY )
                {
                    mLightAuto.setDayBright( brts );
                    refreshData();
                }
                else if ( item == EDIT_ITEM_NIGHT )
                {
                    mLightAuto.setNightBright( brts );
                    refreshData();
                }
            }
        } );
        dialog.show();
    }

    private void showTimePickerDialog ( TimePickerDialog.OnTimeSetListener listener, int hour, int minute )
    {
        TimePickerDialog dialog = new TimePickerDialog( getContext(), listener, hour, minute, true );
        dialog.show();
    }

    private void showDynamicDialog()
    {
        boolean[] bval = new boolean[8];
        final int[] ival = new int[5];
        bval[0] = mLightAuto.isSun();
        bval[1] = mLightAuto.isMon();
        bval[2] = mLightAuto.isTue();
        bval[3] = mLightAuto.isWed();
        bval[4] = mLightAuto.isThu();
        bval[5] = mLightAuto.isFri();
        bval[6] = mLightAuto.isSat();
        bval[7] = mLightAuto.isDynamicEnable();
        ival[0] = mLightAuto.getDynamicPeriod().getStartHour();
        ival[1] = mLightAuto.getDynamicPeriod().getStartMinute();
        ival[2] = mLightAuto.getDynamicPeriod().getEndHour();
        ival[3] = mLightAuto.getDynamicPeriod().getEndMinute();
        ival[4] = mLightAuto.getDynamicMode();
        final BottomSheetDialog dialog = new BottomSheetDialog( getContext() );
        View dialogView = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_edit_dynamic, null );
        final TextView tv_start = (TextView) dialogView.findViewById( R.id.dialog_dynamic_start );
        final TextView tv_end = (TextView) dialogView.findViewById( R.id.dialog_dynamic_end );
        final Switch sw_enable = (Switch) dialogView.findViewById( R.id.dialog_dynamic_enable );
        final CheckBox cb_sun = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_sun );
        final CheckBox cb_mon = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_mon );
        final CheckBox cb_tue = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_tue );
        final CheckBox cb_wed = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_wed );
        final CheckBox cb_thu = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_thu );
        final CheckBox cb_fri = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_fri );
        final CheckBox cb_sat = (CheckBox) dialogView.findViewById( R.id.dialog_dynamic_sat );
        GridView gv_show = (GridView) dialogView.findViewById( R.id.item_dynamic_gv_show );
        Button btn_cancel = (Button) dialogView.findViewById( R.id.dialog_dynamic_cancel );
        Button btn_save = (Button) dialogView.findViewById( R.id.dialog_dynamic_save );
        //initialize data
        final DecimalFormat df = new DecimalFormat( "00" );
        tv_start.setText( df.format( ival[0] ) + ":" + df.format( ival[1] ) );
        tv_end.setText( df.format( ival[2] ) + ":" + df.format( ival[3] ) );
        sw_enable.setChecked( bval[7] );
        cb_sun.setChecked( bval[0] );
        cb_mon.setChecked( bval[1] );
        cb_tue.setChecked( bval[2] );
        cb_wed.setChecked( bval[3] );
        cb_thu.setChecked( bval[4] );
        cb_fri.setChecked( bval[5] );
        cb_sat.setChecked( bval[6] );
        int[] res = new int[]{ R.drawable.ic_block_white_36dp,
                               R.mipmap.ic_thunder1,
                               R.mipmap.ic_thunder2,
                               R.mipmap.ic_thunder3,
                               R.mipmap.ic_allcolor,
                               R.mipmap.ic_cloud1,
                               R.mipmap.ic_cloud2,
                               R.mipmap.ic_cloud3,
                               R.mipmap.ic_cloud4,
                               R.mipmap.ic_moon1,
                               R.mipmap.ic_moon2,
                               R.mipmap.ic_moon3 };
        final DynamicAdapter adapter = new DynamicAdapter( res, ival[4] );
        gv_show.setAdapter( adapter );

        tv_start.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showTimePickerDialog( new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet ( TimePicker view, int hourOfDay, int minute )
                    {
                        ival[0] = hourOfDay;
                        ival[1] = minute;
                        tv_start.setText( df.format( ival[0] ) + ":" + df.format( ival[1] ) );
                    }
                }, ival[0], ival[1] );
            }
        } );
        tv_end.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                showTimePickerDialog( new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet ( TimePicker view, int hourOfDay, int minute )
                    {
                        ival[2] = hourOfDay;
                        ival[3] = minute;
                        tv_end.setText( df.format( ival[2] ) + ":" + df.format( ival[3] ) );
                    }
                }, ival[2], ival[3] );
            }
        } );
        btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                dialog.dismiss();
            }
        } );
        btn_save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                mLightAuto.setDynamicEnable( sw_enable.isChecked() );
                mLightAuto.setSun( cb_sun.isChecked() );
                mLightAuto.setMon( cb_mon.isChecked() );
                mLightAuto.setTue( cb_tue.isChecked() );
                mLightAuto.setWed( cb_wed.isChecked() );
                mLightAuto.setThu( cb_thu.isChecked() );
                mLightAuto.setFri( cb_fri.isChecked() );
                mLightAuto.setSat( cb_sat.isChecked() );
                mLightAuto.getDynamicPeriod().setStartHour( (byte) ival[0] );
                mLightAuto.getDynamicPeriod().setStartMinute( (byte) ival[1] );
                mLightAuto.getDynamicPeriod().setEndHour( (byte) ival[2] );
                mLightAuto.getDynamicPeriod().setEndMinute( (byte) ival[3] );
                mLightAuto.setDynamicMode( (byte) adapter.getSelectIndex() );
                CommUtil.setLedAuto( mAddress, mLightAuto );
                dialog.dismiss();
            }
        } );
        dialog.setContentView( dialogView );
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    class DynamicAdapter extends BaseAdapter
    {
        private int[] resArray;
        private int selectIndex;

        public DynamicAdapter ( int[] resArray, int selectIndex )
        {
            this.resArray = resArray;
            this.selectIndex = selectIndex;
            if ( resArray == null && selectIndex >= resArray.length )
            {
                this.selectIndex = 0;
            }
        }

        public int getSelectIndex ()
        {
            return selectIndex;
        }

        @Override
        public int getCount ()
        {
            return resArray == null ? 0 : resArray.length;
        }

        @Override
        public Object getItem ( int position )
        {
            return resArray[position];
        }

        @Override
        public long getItemId ( int position )
        {
            return position;
        }

        @SuppressLint ( "RestrictedApi" )
        @Override
        public View getView ( final int position, View convertView, ViewGroup parent )
        {
            int resid = resArray[position];
            if ( convertView == null )
            {
                convertView = LayoutInflater.from( getContext() ).inflate( R.layout.item_dynamic, parent, false );
            }
            CheckableImageButton cib = (CheckableImageButton) convertView.findViewById( R.id.item_cib_dynamic );
            cib.setImageResource( resid );
            cib.setChecked( false );
            if ( position == selectIndex )
            {
                cib.setChecked( true );
            }
            cib.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick ( View v )
                {
                    selectIndex = position;
                    notifyDataSetChanged();
                }
            } );
            return convertView;
        }
    }

    class PreviewTimerTask extends TimerTask
    {
//        private static final int TOTAL_COUNT = 24 * 60;
//        private static final int TIME_STEP = 1;

        private int tm;
        private PreviewTaskListener mListener;

        public PreviewTimerTask ()
        {
            tm = 0;
        }

        public void setListener ( PreviewTaskListener listener )
        {
            mListener = listener;
        }

        @Override
        public void run ()
        {
//            tm += TIME_STEP;
            tm++;
            if ( tm >= 1440 )
            {
                tm = 0;
                if ( mListener != null )
                {
                    mListener.onFinish();
                }
            }
            else
            {
                if ( mListener != null )
                {
                    mListener.onUpdate( tm );
                }
            }
            CommUtil.previewAuto( mAddress, getBright( tm ) );
        }
    }

    interface PreviewTaskListener
    {
        void onFinish ();

        void onUpdate ( int tm );
    }
}
