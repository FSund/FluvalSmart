package com.inledco.fluvalsmart.fragment;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.activity.EditproActivity;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.bean.RampTime;
import com.inledco.fluvalsmart.bean.TimerBrightPoint;
import com.inledco.fluvalsmart.constant.CustomColor;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LightProfileUtil;
import com.liruya.tuner168blemanager.BleCommunicateListener;
import com.liruya.tuner168blemanager.BleManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightProFragment extends BaseFragment
{
    private short devid;
    private String mAddress;
    private LightPro mLightPro;

    private LineChart pro_line_chart;
    private TextView pro_tv_points;
    private TextView pro_tv_dynamic;
    private Button pro_btn_export;
    private Button pro_btn_save;
    private ToggleButton pro_tb_preview;
    private Button pro_btn_edit;
    private Button pro_btn_overview;
    private LinearLayout pro_linearlayout;
    private SeekBar pro_seekbar;
    private TextView pro_textview;

    private BleCommunicateListener mCommunicateListener;
    private Timer mPreviewTimer;
    private TimerTask mPreviewTask;
    private int mPreviewCount;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled( true );
    }

    public static LightProFragment newInstance( String address, short id, LightPro pro )
    {
        LightProFragment frag = new LightProFragment();
        Bundle bundle = new Bundle();
        bundle.putString( "address", address );
        bundle.putShort( "id", id );
        bundle.putSerializable( "pro", pro );
        frag.setArguments( bundle );

        return frag;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_light_pro, container, false );

        initView( view );
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (mPreviewTask != null)
        {
            mPreviewTask.cancel();
            mPreviewTask = null;
        }
        if (mPreviewTimer != null)
        {
            mPreviewTimer.cancel();
            mPreviewTimer = null;
        }
        BleManager.getInstance().removeBleCommunicateListener(mCommunicateListener);
    }

    @Override
    protected void initView( View view )
    {
        pro_line_chart = view.findViewById( R.id.pro_line_chart );
        pro_tv_points =view.findViewById( R.id.pro_tv_points );
        pro_tv_dynamic =view.findViewById( R.id.pro_tv_dynamic );
        pro_btn_export = view.findViewById( R.id.pro_btn_export );
        pro_btn_save = view.findViewById( R.id.pro_btn_save );
        pro_tb_preview = view.findViewById( R.id.pro_tb_preview );
        pro_btn_edit = view.findViewById( R.id.pro_btn_edit );
        pro_btn_overview = view.findViewById( R.id.pro_btn_overview );
        pro_linearlayout = view.findViewById( R.id.pro_linearlayout );
        pro_seekbar = view.findViewById( R.id.pro_seekbar );
        pro_textview = view.findViewById( R.id.pro_textview );

        initLineChart( pro_line_chart );
    }

    @Override
    protected void initEvent()
    {
        final DecimalFormat df = new DecimalFormat( "00" );

        pro_seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
            {
                if ( fromUser )
                {
                    mPreviewCount = progress;
                }
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {

            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {

            }
        } );

        pro_tv_dynamic.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                showDynamicDialog();
            }
        } );

        pro_btn_save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                if ( pro_tb_preview.isChecked() )
                {
                    return;
                }
                showSaveDialog();
            }
        } );

        pro_btn_export.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                if ( pro_tb_preview.isChecked() )
                {
                    return;
                }
                showExportDialog();
            }
        } );

        pro_tb_preview.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
            {
                pro_seekbar.setProgress( 0 );
                pro_textview.setText( "00:00" );
                if ( isChecked )
                {
                    pro_tv_points.setVisibility( View.GONE );
                    pro_tv_dynamic.setVisibility( View.GONE );
                    pro_linearlayout.setVisibility( View.VISIBLE );
                    mPreviewCount = -1;
                    mPreviewTimer = new Timer();
                    mPreviewTask = new TimerTask() {
                        @Override
                        public void run()
                        {
                            mPreviewCount++;
                            if ( mPreviewCount >= 1440 )
                            {
                                getActivity().runOnUiThread( new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        pro_tb_preview.setChecked( false );
                                    }
                                } );
                            }
                            else
                            {
                                CommUtil.preview( mAddress, getBrights( mPreviewCount ) );
                                pro_line_chart.getXAxis().removeAllLimitLines();
                                LimitLine limitLine = new LimitLine( mPreviewCount );
                                limitLine.setLineWidth( 1 );
                                limitLine.setLineColor( CustomColor.COLOR_GREEN_A700 );
                                pro_line_chart.getXAxis().addLimitLine( limitLine );
                                getActivity().runOnUiThread( new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        pro_seekbar.setProgress( mPreviewCount );
                                        pro_textview.setText( df.format( mPreviewCount/60 ) + ":" + df.format( mPreviewCount%60 ) );
                                        pro_line_chart.invalidate();
                                    }
                                } );
                            }
                        }
                    };
                    mPreviewTimer.schedule( mPreviewTask, 0, 40 );
                }
                else
                {
                    pro_tv_points.setVisibility( View.VISIBLE );
                    pro_tv_dynamic.setVisibility( mLightPro.isHasDynamic() ? View.VISIBLE : View.GONE );
                    pro_linearlayout.setVisibility( View.GONE );
                    mPreviewCount = -1;
                    mPreviewTask.cancel();
                    mPreviewTimer.cancel();
                    mPreviewTask = null;
                    mPreviewTimer = null;
                    pro_line_chart.getXAxis().removeAllLimitLines();
                    pro_line_chart.invalidate();
                    getActivity().runOnUiThread( new Runnable() {
                        @Override
                        public void run()
                        {
                            new Handler().postDelayed( new Runnable() {
                                @Override
                                public void run()
                                {
                                    CommUtil.stopPreview( mAddress );
                                }
                            }, 64 );
                        }
                    } );
                }
            }
        } );

        pro_btn_edit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
//                showEditDialog();
                if ( pro_tb_preview.isChecked() )
                {
                    return;
                }
                Intent intent = new Intent( getContext(), EditproActivity.class );
                intent.putExtra( "devid", devid );
                intent.putExtra( "address", mAddress );
                intent.putExtra( "light_pro", mLightPro );
                startActivity( intent );
            }
        } );

        pro_btn_overview.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                if ( pro_tb_preview.isChecked() )
                {
                    return;
                }
                showOverviewDialog();
            }
        } );
    }

    @Override
    protected void initData()
    {
        Bundle bundle = getArguments();
        mLightPro = (LightPro) bundle.getSerializable( "pro" );
        devid = bundle.getShort( "id" );
        mAddress = bundle.getString( "address" );

        mCommunicateListener = new BleCommunicateListener() {
            @Override
            public void onDataValid( String mac )
            {

            }

            @Override
            public void onDataInvalid( String mac )
            {

            }

            @Override
            public void onReadMfr( String mac, String s )
            {

            }

            @Override
            public void onReadPassword( String mac, int psw )
            {

            }

            @Override
            public void onDataReceived( String mac, ArrayList< Byte > list )
            {
                if ( mac.equals( mAddress ) )
                {
                    Object object = CommUtil.decodeLight( list, devid );
                    if ( object != null && object instanceof LightPro )
                    {
                        mLightPro = (LightPro) object;
                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run()
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
    }

    private void initLineChart(LineChart lineChart)
    {
        XAxis xAxis = lineChart.getXAxis();
        YAxis axisLeft = lineChart.getAxisLeft();
        YAxis axisRight = lineChart.getAxisRight();
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
        lineChart.setTouchEnabled( false );
        lineChart.setDragEnabled( false );
        lineChart.setScaleEnabled( false );
        lineChart.setPinchZoom( false );
        lineChart.setDoubleTapToZoomEnabled( false );
        lineChart.setBorderColor( Color.CYAN );
        lineChart.setBorderWidth( 1 );
        lineChart.setDrawBorders( false );
        lineChart.setDrawGridBackground( true );
        lineChart.setGridBackgroundColor( Color.TRANSPARENT );
        lineChart.setDescription( null );
        lineChart.setMaxVisibleValueCount( 0 );
        lineChart.getLegend().setHorizontalAlignment( Legend.LegendHorizontalAlignment.CENTER );
        lineChart.getLegend().setTextSize( 14 );
        lineChart.getLegend().setFormSize( 12 );
        lineChart.getLegend().setTextColor( Color.WHITE );
        final String[] hours = new String[]{ "00:00", "06:00", "12:00", "18:00", "00:00" };
        IAxisValueFormatter formatter = new IAxisValueFormatter()
        {
            @Override
            public String getFormattedValue( float value, AxisBase axis )
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

    private void refreshData()
    {
        if (mLightPro == null)
        {
            return;
        }
        int pointCount = mLightPro.getPointCount();
        if ( pointCount < LightPro.POINT_COUNT_MIN && pointCount > LightPro.POINT_COUNT_MAX )
        {
            return;
        }
        if ( getContext() == null )
        {
            return;
        }
        Channel[] channels = DeviceUtil.getLightChannel( getContext(), devid );
        DecimalFormat df = new DecimalFormat( "00" );
        int chns = DeviceUtil.getChannelCount( devid );
        List<ILineDataSet> dataSets = new ArrayList<>();
        int[] index = new int[pointCount];
        int[] tmr = new int[pointCount];
        for ( int i = 0; i < pointCount; i++ )
        {
            index[i] = i;
            tmr[i] = mLightPro.getPoints()[i].getHour()*60+mLightPro.getPoints()[i].getMinute();
        }
        for ( int i = pointCount-1; i > 0 ; i-- )
        {
            for ( int j = 0; j < i; j++ )
            {
                if ( tmr[index[j]] > tmr[index[j+1]] )
                {
                    int temp = index[j];
                    index[j] = index[j+1];
                    index[j+1] = temp;
                }
            }
        }
        for ( int i = 0; i < chns; i++ )
        {
            List<Entry> entries = new ArrayList<>();
            int ts = tmr[index[0]];
            int te = tmr[index[pointCount-1]];
            int bs = mLightPro.getPoints()[index[0]].getBrights()[i];
            int be = mLightPro.getPoints()[index[pointCount-1]].getBrights()[i];
            int duration = 1440 - te + ts;
            int dbrt = bs - be;
            float b0 = be + dbrt * (1440 - te)/duration;
            entries.add( new Entry( 0, b0 ) );
            for ( int j = 0; j < pointCount; j++ )
            {
                entries.add( new Entry( tmr[index[j]], mLightPro.getPoints()[index[j]].getBrights()[i] ) );
            }
            entries.add( new Entry( 1440, b0 ) );
            LineDataSet lineDataSet = new LineDataSet( entries, channels[i].getName() );
            lineDataSet.setColor( channels[i].getColor() );
            lineDataSet.setCircleRadius( 4.0f );
            lineDataSet.setCircleColor( channels[i].getColor() );
            lineDataSet.setDrawCircleHole( false );
            lineDataSet.setLineWidth( 2.0f );
            dataSets.add( lineDataSet );
        }
        LineData lineData = new LineData( dataSets );
        pro_line_chart.setData( lineData );
        pro_line_chart.invalidate();

        pro_tv_points.setText( "" + mLightPro.getPointCount() + " Timepoints Set" );
        if ( mLightPro.isHasDynamic() )
        {
            pro_tv_dynamic.setVisibility( pro_tb_preview.isChecked() ? View.GONE : View.VISIBLE );
            int week = mLightPro.getWeek() & 0xFF;
            if ( week > 0x80 && mLightPro.getDynamicMode() > 0 && mLightPro.getDynamicMode() < 12 )
            {
                StringBuffer sb = new StringBuffer();
                if ( mLightPro.isSun() )
                {
                    sb.append( getString( R.string.weekday_sun ) );
                }
                if ( mLightPro.isMon() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_mon ) );
                }
                if ( mLightPro.isTue() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_tue ) );
                }
                if ( mLightPro.isWed() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_wed ) );
                }
                if ( mLightPro.isThu() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_thu ) );
                }
                if ( mLightPro.isFri() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_fri ) );
                }
                if ( mLightPro.isSat() )
                {
                    sb.append( " " ).append( getString( R.string.weekday_sat ) );
                }
                sb.append( "\r\n" )
                  .append( df.format( mLightPro.getDynamicPeriod().getStartHour() ) )
                  .append( ":" )
                  .append( df.format( mLightPro.getDynamicPeriod().getStartMinute() ) )
                  .append( " - " )
                  .append( df.format( mLightPro.getDynamicPeriod().getEndHour() ) )
                  .append( ":" )
                  .append( df.format( mLightPro.getDynamicPeriod().getEndMinute() ) );
                pro_tv_dynamic.setText( sb );
                pro_tv_dynamic.setCompoundDrawablesWithIntrinsicBounds( DeviceUtil.getDynamicRes( mLightPro.getDynamicMode() ), 0, 0, 0 );
            }
            else
            {
                pro_tv_dynamic.setText( R.string.light_auto_dynamic_disabled );
                pro_tv_dynamic.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_block_white_36dp, 0, 0, 0 );
            }
        }
        else
        {
            pro_tv_dynamic.setVisibility( View.GONE );
        }
    }

    private void showExportDialog()
    {
        final Map<String, LightPro> localProfiles = LightProfileUtil.getLocalProProfiles( getContext(), devid, mLightPro.isHasDynamic() );
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        builder.setTitle( R.string.export_profile );
        if ( localProfiles == null || localProfiles.size() == 0 )
        {
            builder.setMessage( R.string.msg_no_profile );
        }
        else
        {
            final String[] keys = new String[localProfiles.size()];
            int idx = 0;
            for ( String s : localProfiles.keySet() )
            {
                keys[idx++] = s;
            }
            final int[] index = {0};
            builder.setSingleChoiceItems( keys, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    index[0] = which;
                }
            } );
            builder.setPositiveButton( R.string.dialog_export_use, new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    CommUtil.setLedPro( mAddress, localProfiles.get( keys[index[0]] ) );
                }
            } );
            builder.setNeutralButton( R.string.dialog_export_remove, new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    LightProfileUtil.deleteProProfile( getContext(), devid, keys[index[0]] );
                }
            } );
        }

        builder.setNegativeButton( R.string.cancel, null );
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    private void showSaveDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        final AlertDialog dialog = builder.create();
        View view = LayoutInflater.from( getContext() )
                                  .inflate( R.layout.dialog_export_profile, null );
        final EditText name = view.findViewById( R.id.export_name );
        Button btn_cancel = view.findViewById( R.id.export_cancel );
        Button btn_ok = view.findViewById( R.id.export_ok );
        final boolean[] flag = new boolean[]{false};
        name.addTextChangedListener( new TextWatcher() {
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
                    name.setText( str );
                    name.setSelection( start + count );
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
                    LightProfileUtil.saveProProfile( getContext(),
                                                      mLightPro,
                                                      devid,
                                                      name.getText().toString() );
                    dialog.dismiss();
                    Toast.makeText( getContext(), R.string.save_success, Toast.LENGTH_SHORT )
                         .show();
                }
            }
        } );
        dialog.setView( view );
        dialog.setTitle( R.string.save_profile );
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    private short[] getBrights(final int ct)
    {
        final int chns = DeviceUtil.getChannelCount( devid );
        final int count = mLightPro.getPointCount();
        if ( chns <= 0 || chns > 6 || count < LightPro.POINT_COUNT_MIN || count > LightPro.POINT_COUNT_MAX )
        {
            return null;
        }
        int[] timers = new int[count];
        byte[][] brights = new byte[count][chns];
        int[] index = new int[count];
        for ( int i = 0; i < count; i++ )
        {
            index[i] = i;
            TimerBrightPoint tbp = mLightPro.getPoints()[i];
            timers[i] = tbp.getHour()*60+tbp.getMinute();
            for ( int j = 0; j < chns; j++ )
            {
                brights[i][j] = tbp.getBrights()[j];
            }
        }
        for ( int i = count-1; i > 0; i-- )
        {
            for ( int j = 0; j < i; j++ )
            {
                if ( timers[index[j]] > timers[index[j+1]] )
                {
                    int tmp = index[j];
                    index[j] = index[j+1];
                    index[j+1] = tmp;
                }
            }
        }
        boolean flag = false;
        int start = index[count-1];
        int end = index[0];
        int duration = 1440 - timers[start] + timers[end];
        int dt = 0;
        if ( ct >= timers[start] )
        {
            dt = ct - timers[start];
            flag = true;
        }
        else if ( ct < timers[end] )
        {
            dt = 1440 - timers[start] + ct;
            flag = true;
        }
        else
        {
            for ( int i = 0; i < count - 1; i++ )
            {
                start = index[i];
                end = index[i+1];
                if ( ct >= timers[start] && ct < timers[end] )
                {
                    duration = timers[end] - timers[start];
                    dt = ct - timers[start];
                    flag = true;
                    break;
                }
            }
        }
        if ( flag )
        {
            short[] values = new short[chns];
            for ( int i = 0; i < chns; i++ )
            {
                int sbrt = brights[start][i] * 10;
                int ebrt = brights[end][i] * 10;
                values[i] = (short) ( sbrt + ( ebrt - sbrt) * dt / duration);
            }
            return values;
        }
        return null;
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
        bval[0] = mLightPro.isSun();
        bval[1] = mLightPro.isMon();
        bval[2] = mLightPro.isTue();
        bval[3] = mLightPro.isWed();
        bval[4] = mLightPro.isThu();
        bval[5] = mLightPro.isFri();
        bval[6] = mLightPro.isSat();
        bval[7] = mLightPro.isDynamicEnable();
        ival[0] = mLightPro.getDynamicPeriod().getStartHour();
        ival[1] = mLightPro.getDynamicPeriod().getStartMinute();
        ival[2] = mLightPro.getDynamicPeriod().getEndHour();
        ival[3] = mLightPro.getDynamicPeriod().getEndMinute();
        ival[4] = mLightPro.getDynamicMode();
        final BottomSheetDialog dialog = new BottomSheetDialog( getContext() );
        View dialogView = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_edit_dynamic, null );
        final CheckBox[] cb_week = new CheckBox[7];
        final TextView tv_start = dialogView.findViewById( R.id.dialog_dynamic_start );
        final TextView tv_end = dialogView.findViewById( R.id.dialog_dynamic_end );
        final Switch sw_enable = dialogView.findViewById( R.id.dialog_dynamic_enable );
        cb_week[0] = dialogView.findViewById( R.id.dialog_dynamic_sun );
        cb_week[1] = dialogView.findViewById( R.id.dialog_dynamic_mon );
        cb_week[2] = dialogView.findViewById( R.id.dialog_dynamic_tue );
        cb_week[3] = dialogView.findViewById( R.id.dialog_dynamic_wed );
        cb_week[4] = dialogView.findViewById( R.id.dialog_dynamic_thu );
        cb_week[5] = dialogView.findViewById( R.id.dialog_dynamic_fri );
        cb_week[6] = dialogView.findViewById( R.id.dialog_dynamic_sat );
        GridView gv_show = dialogView.findViewById( R.id.item_dynamic_gv_show );
        Button btn_cancel = dialogView.findViewById( R.id.dialog_dynamic_cancel );
        Button btn_save = dialogView.findViewById( R.id.dialog_dynamic_save );
        //initialize data
        final DecimalFormat df = new DecimalFormat( "00" );
        tv_start.setText( df.format( ival[0] ) + ":" + df.format( ival[1] ) );
        tv_end.setText( df.format( ival[2] ) + ":" + df.format( ival[3] ) );
        sw_enable.setChecked( bval[7] );
        for ( int i = 0; i < 7; i++ )
        {
            cb_week[i].setChecked( bval[i] );
        }
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
                    public void onTimeSet( TimePicker view, int hourOfDay, int minute )
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
                byte week = (byte) ( sw_enable.isChecked() ? 0x80 : 0x00);
                for ( int i = 0; i < 7; i++ )
                {
                    if ( cb_week[i].isChecked() )
                    {
                        week |= (1<<i);
                    }
                }
                RampTime rt = new RampTime( (byte) ival[0], (byte) ival[1], (byte) ival[2], (byte) ival[3] );
                byte mode = (byte) adapter.getSelectIndex();
                CommUtil.setLedDynamicPeriod( mAddress, week, rt, mode );
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
            CheckableImageButton cib = convertView.findViewById( R.id.item_cib_dynamic );
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

    private void showOverviewDialog()
    {
        final BottomSheetDialog dialog = new BottomSheetDialog( getContext() );
        View view = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_pro_overview, null );
        RecyclerView rv_show = view.findViewById( R.id.dialog_overview_show );
        Button btn_ok = view.findViewById( R.id.dialog_overview_ok );
        TimerPointsAdapter adapter = new TimerPointsAdapter();
        rv_show.setLayoutManager( new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false ) );
        rv_show.setAdapter( adapter );
        btn_ok.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                dialog.dismiss();
            }
        } );
        dialog.setContentView( view );
        dialog.setCanceledOnTouchOutside( false );
        dialog.show();
    }

    class TimerPointsAdapter extends RecyclerView.Adapter<TimerPointsViewHolder>
    {
        @NonNull
        @Override
        public TimerPointsViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
        {
            TimerPointsViewHolder holder = new TimerPointsViewHolder( LayoutInflater.from( getContext() ).inflate( R.layout.item_timer_point, parent, false ) );
            return holder;
        }

        @Override
        public void onBindViewHolder( @NonNull TimerPointsViewHolder holder, int position )
        {
            int chns = DeviceUtil.getChannelCount( devid );
            if ( chns <= 0 || chns > 6 )
            {
                return;
            }
            Channel[] channels = DeviceUtil.getLightChannel( getContext(), devid );
            TimerBrightPoint tbp = mLightPro.getPoints()[position];
            DecimalFormat df = new DecimalFormat( "00" );
            holder.tv_num.setText( "" + df.format(position + 1) );
            holder.tv_tmr.setText( df.format( tbp.getHour() ) + ":" + df.format( tbp.getMinute() ) );
            for ( int i = 0; i < chns; i++ )
            {
                holder.tv_brts[i].setVisibility( View.VISIBLE );
                holder.tv_brts[i].setText( " " + tbp.getBrights()[i] + "%" );
                holder.tv_brts[i].setCompoundDrawablesWithIntrinsicBounds( channels[i].getIcon(), 0, 0, 0 );
            }
            for ( int i = chns; i < 6; i++ )
            {
                holder.tv_brts[i].setVisibility( View.GONE );
            }
        }

        @Override
        public int getItemCount()
        {
            return mLightPro == null ? 0 : mLightPro.getPointCount();
        }
    }

    class TimerPointsViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tv_num;
        private TextView tv_tmr;
        private TextView[] tv_brts;

        public TimerPointsViewHolder( View itemView )
        {
            super( itemView );
            tv_num = itemView.findViewById( R.id.item_tbp_num );
            tv_tmr = itemView.findViewById( R.id.item_tbp_tmr );
            tv_brts = new TextView[6];
            tv_brts[0] = itemView.findViewById( R.id.item_tbp_chn1 );
            tv_brts[1] = itemView.findViewById( R.id.item_tbp_chn2 );
            tv_brts[2] = itemView.findViewById( R.id.item_tbp_chn3 );
            tv_brts[3] = itemView.findViewById( R.id.item_tbp_chn4 );
            tv_brts[4] = itemView.findViewById( R.id.item_tbp_chn5 );
            tv_brts[5] = itemView.findViewById( R.id.item_tbp_chn6 );
        }
    }
}
