package com.inledco.fluvalsmart.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.bean.PointComparator;
import com.inledco.fluvalsmart.bean.TimerBrightPoint;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.view.MultiPointSeekbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EditproActivity extends BaseActivity
{
    private LineChart editpro_linechart;
    private MultiPointSeekbar editpro_mps;
//    private ImageButton editpro_ib_last;
//    private ImageButton editpro_ib_next;
//    private ImageButton editpro_ib_dec;
//    private ImageButton editpro_ib_inc;
//    private TextView editpro_tv_tmr;
    private ImageButton editpro_ib_remove;
    private ImageButton editpro_ib_add;
    private Button editpro_btn_cancel;
    private Button editpro_btn_save;
    private RecyclerView editpro_rv_show;

    private short devid;
    private String mAddress;
    private List<TimerBrightPoint> mPoints;
//    private TimerBrightPoint mSelectPoint;
    private PointComparator mComparator;
    private EditproAdapter mAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_editpro );

        initView();
        initEvent();
        initData();
    }

    @Override
    public void onBackPressed()
    {
//        int idx = editpro_mps.getSelectedPoint();
//        if ( idx >= 0 && idx < mPoints.size() )
//        {
//            mPoints.get( idx ).setHour( mSelectPoint.getHour() );
//            mPoints.get( idx ).setMinute( mSelectPoint.getMinute() );
//            for ( int i = 0; i < mSelectPoint.getBrights().length; i++ )
//            {
//                mPoints.get( idx ).getBrights()[i] = mSelectPoint.getBrights()[i];
//            }
//            editTimeDone();
//            editpro_mps.setProgress( idx, mPoints.get( idx ).getHour() * 60 + mPoints.get( idx ).getMinute() );
//            refreshChart();
//            return;
//        }
//        super.onBackPressed();
    }

    @Override
    protected void initView()
    {
        editpro_linechart = findViewById( R.id.editpro_linechart );
        editpro_mps = findViewById( R.id.editpro_mps );
//        editpro_ib_last = findViewById( R.id.editpro_ib_last );
//        editpro_ib_next = findViewById( R.id.editpro_ib_next );
//        editpro_ib_dec = findViewById( R.id.editpro_ib_dec );
//        editpro_ib_inc = findViewById( R.id.editpro_ib_inc );
//        editpro_tv_tmr = findViewById( R.id.editpro_tv_tmr );
        editpro_ib_remove = findViewById( R.id.editpro_ib_remove );
        editpro_ib_add = findViewById( R.id.editpro_ib_add );
        editpro_btn_cancel = findViewById( R.id.editpro_btn_cancel );
        editpro_btn_save = findViewById( R.id.editpro_btn_save );
        editpro_rv_show = findViewById( R.id.editpro_rv_show );

        initLineChart( editpro_linechart );
        editpro_mps.setMaxLengthHint( "88:88" );
        editpro_mps.setGetTextImpl( new MultiPointSeekbar.GetTextImpl() {
            @Override
            public String getText( int progress )
            {
                DecimalFormat df = new DecimalFormat( "00" );
                String result = df.format( progress/60 ) + ":" + df.format( progress%60 );
                return result;
            }
        } );
        editpro_rv_show.setLayoutManager( new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false ) );
    }

    @Override
    protected void initEvent()
    {
        editpro_mps.setListener( new MultiPointSeekbar.Listener() {
            @Override
            public void onPointCountChanged( int pointCount )
            {

            }

            @Override
            public void onPointSelected( int index )
            {
                if ( mPoints != null && index >= 0 && index < editpro_mps.getPointCount() )
                {
                    editpro_ib_remove.setVisibility( View.VISIBLE );
                    mAdapter.setSelectedPoint( index );
//                    DecimalFormat df = new DecimalFormat( "00" );
//                    editpro_tv_tmr.setText( df.format( mPoints.get( index ).getHour() ) + ":" + df.format( mPoints.get( index ).getMinute() ) );
                }
                else
                {
                    editpro_ib_remove.setVisibility( View.GONE );
                }
            }

            @Override
            public void onMultiPointTouched( List< Integer > points )
            {
                showMultiTouchPointDialog( points );
            }

            @Override
            public void onStartPointTouch( int index )
            {

            }

            @Override
            public void onStopPointTouch( int index )
            {
                if ( mPoints != null && index >= 0 && index < mPoints.size() )
                {
//                    DecimalFormat df = new DecimalFormat( "00" );
                    //                    editpro_tv_tmr.setText( df.format( progress/60 ) + ":" + df.format( progress%60 ) );
                    int progress = editpro_mps.getProgressByIndex( index );
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setMinute( progress%60 );
                    refreshChart();
//                    editpro_ib_add.setEnabled( !editpro_mps.isSelectedPointCoincide() );
                }
            }

            @Override
            public void onPointProgressChanged( int index, int progress, boolean fromUser )
            {
                if ( mPoints != null && index >= 0 && index < mPoints.size() && progress >= 0 && progress < 1440 )
                {
//                    DecimalFormat df = new DecimalFormat( "00" );
//                    editpro_tv_tmr.setText( df.format( progress/60 ) + ":" + df.format( progress%60 ) );
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setMinute( progress%60 );
                    refreshChart();
                }
            }
        } );
//        editpro_ib_dec.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View v )
//            {
//                int pt = editpro_mps.getSelectedPoint();
//                decTimer( pt );
//                editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
//                refreshSelectedTimer();
//                refreshChart();
//            }
//        } );
//        editpro_ib_dec.setOnLongClickListener( new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick( final View v )
//            {
//                final Timer timer = new Timer();
//                timer.schedule( new TimerTask() {
//                    @Override
//                    public void run()
//                    {
//                        if ( v.isPressed() )
//                        {
//                            final int pt = editpro_mps.getSelectedPoint();
//                            decTimer( pt );
//                            runOnUiThread( new Runnable() {
//                                @Override
//                                public void run()
//                                {
//                                    editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
//                                    refreshSelectedTimer();
//                                    refreshChart();
//                                }
//                            } );
//                        }
//                        else
//                        {
//                            cancel();
//                            timer.cancel();
//                        }
//                    }
//                }, 0, 20 );
//                return true;
//            }
//        } );
//
//        editpro_ib_inc.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View v )
//            {
//                int pt = editpro_mps.getSelectedPoint();
//                incTimer( pt );
//                editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
//                refreshSelectedTimer();
//                refreshChart();
//            }
//        } );
//        editpro_ib_inc.setOnLongClickListener( new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick( final View v )
//            {
//                final Timer timer = new Timer();
//                timer.schedule( new TimerTask() {
//                    @Override
//                    public void run()
//                    {
//                        if ( v.isPressed() )
//                        {
//                            final int pt = editpro_mps.getSelectedPoint();
//                            incTimer( pt );
//                            runOnUiThread( new Runnable() {
//                                @Override
//                                public void run()
//                                {
//                                    editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
//                                    refreshSelectedTimer();
//                                    refreshChart();
//                                }
//                            } );
//                        }
//                        else
//                        {
//                            cancel();
//                            timer.cancel();
//                        }
//                    }
//                }, 0, 20 );
//                return true;
//            }
//        } );
//
//        editpro_ib_last.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View v )
//            {
//                int pt = getLastPoint( editpro_mps.getSelectedPoint() );
//                if ( pt >= 0 && pt < mPoints.size() )
//                {
//                    editpro_mps.setSelectedPoint( pt );
//                    refreshSelectedTimer();
//                    mAdapter.setSelectedPoint( pt );
//                }
//            }
//        } );
//
//        editpro_ib_next.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View v )
//            {
//                int pt = getNextPoint( editpro_mps.getSelectedPoint() );
//                if ( pt >= 0 && pt < mPoints.size() )
//                {
//                    editpro_mps.setSelectedPoint( pt );
//                    refreshSelectedTimer();
//                    mAdapter.setSelectedPoint( pt );
//                }
//            }
//        } );

        editpro_ib_remove.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                showDeletePointDialog( editpro_mps.getSelectedPoint() );
            }
        } );

        editpro_ib_add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                showAddPointDialog();
            }
        } );

//        editpro_ib_add.setOnClickListener( new View.OnClickListener() {
//            @SuppressLint ( "RestrictedApi" )
//            @Override
//            public void onClick( View v )
//            {
//                if ( editpro_ib_add.isChecked() )
//                {
//                    editTimeDone();
//                }
//                else
//                {
//                    showAddPointDialog();
//                }
//            }
//        } );

        editpro_btn_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                finish();
            }
        } );

        editpro_btn_save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                if ( mPoints != null )
                {
                    Collections.sort( mPoints, mComparator );
                    CommUtil.setLedPro( mAddress, mPoints );
                    finish();
                }
            }
        } );
    }

    @Override
    protected void initData()
    {
        Intent intent = getIntent();
        if ( intent != null )
        {
            mAddress = intent.getStringExtra( "address" );
            devid = intent.getShortExtra( "devid", (short) 0 );
            LightPro lightPro = (LightPro) intent.getSerializableExtra( "light_pro" );
            if ( lightPro != null && TextUtils.isEmpty( mAddress ) != true && devid != 0 )
            {
                int count = lightPro.getPointCount();

                mPoints = new ArrayList<>();
                mComparator = new PointComparator();
                for ( int i = 0; i < count; i++ )
                {
                    mPoints.add( lightPro.getPoints()[i] );
                }
                Collections.sort( mPoints, mComparator );

                editpro_mps.setMax( 1439 );
                editpro_mps.setPointCount( count );

                for ( int i = 0; i < count; i++ )
                {
                    editpro_mps.setProgress( i, lightPro.getPoints()[i].getTimer() );
                }

                mAdapter = new EditproAdapter();
                editpro_rv_show.setAdapter( mAdapter );

                editpro_mps.setSelectedPoint( 0 );
                refreshSelectedTimer();
                refreshChart();
            }
        }
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

    private void refreshChart()
    {
        if ( mPoints != null )
        {
            int count = mPoints.size();
            int[] index = new int[count];
            int[] tmr = new int[count];
            for ( int i = 0; i < count; i++ )
            {
                index[i] = i;
                tmr[i] = mPoints.get( i ).getTimer();
            }
            for ( int i = count-1; i > 0; i-- )
            {
                for ( int j = 0; j < i; j++ )
                {
                    if ( tmr[index[j]] > tmr[index[j+1]] )
                    {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }
            Channel[] channels = DeviceUtil.getLightChannel( this, devid );
            int chns = DeviceUtil.getChannelCount( devid );
            List<ILineDataSet> dataSets = new ArrayList<>();
            for ( int i = 0; i < chns; i++ )
            {
                List<Entry> entries = new ArrayList<>();
                int ts = tmr[index[0]];
                int te = tmr[index[count-1]];
                int bs = mPoints.get( index[0] ).getBrights()[i];
                int be = mPoints.get( index[count-1] ).getBrights()[i];
                int duration = 1440 - te + ts;
                int dbrt = bs - be;
                float b0 = be + dbrt * (1440 - te)/duration;
                entries.add( new Entry( 0, b0 ) );
                for ( int j = 0; j < count; j++ )
                {
                    entries.add( new Entry( tmr[index[j]], mPoints.get( index[j] ).getBrights()[i] ) );
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
            editpro_linechart.setData( lineData );
            editpro_linechart.invalidate();
        }
    }

//    @SuppressLint ( "RestrictedApi" )
//    private void editTimeStart()
//    {
//        TimerBrightPoint tbp = mPoints.get( editpro_mps.getSelectedPoint() );
//        mSelectPoint = new TimerBrightPoint( tbp.getHour(), tbp.getMinute(), tbp.getBrights() );
//        editpro_ib_remove.setVisibility( View.VISIBLE );
//        editpro_btn_cancel.setVisibility( View.GONE );
//        editpro_btn_save.setVisibility( View.GONE );
//        mAdapter.setSelectedPoint( editpro_mps.getSelectedPoint() );
//        editpro_ib_add.setChecked( true );
//        editpro_ib_remove.setEnabled( true );
//        editpro_btn_cancel.setEnabled( false );
//        editpro_btn_save.setEnabled( false );
//        mAdapter.setSelectedPoint( editpro_mps.getSelectedPoint() );
//    }

//    @SuppressLint ( "RestrictedApi" )
//    private void editTimeDone()
//    {
//        mSelectPoint = null;
//        editpro_ib_remove.setVisibility( View.GONE );
//        editpro_btn_cancel.setVisibility( View.VISIBLE );
//        editpro_btn_save.setVisibility( View.VISIBLE );
//        editpro_mps.clearSelectedPoint();
//        mAdapter.setSelectedPoint( editpro_mps.getSelectedPoint() );
//        editpro_ib_add.setChecked( false );
//        editpro_ib_remove.setEnabled( false );
//        editpro_btn_cancel.setEnabled( true );
//        editpro_btn_save.setEnabled( true );
//        editpro_mps.clearSelectedPoint();
//        mAdapter.setSelectedPoint( editpro_mps.getSelectedPoint() );
//    }

    private void refreshSelectedTimer()
    {
//        if ( mPoints != null )
//        {
//            int sp = editpro_mps.getSelectedPoint();
//            if ( sp >= 0 && sp < mPoints.size() )
//            {
//                DecimalFormat df = new DecimalFormat( "00" );
//                editpro_tv_tmr.setText( df.format( mPoints.get( sp ).getHour() ) + ":" + df.format( mPoints.get( sp ).getMinute() ) );
//            }
//        }
    }

    private void decTimer(final int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            int tmr = mPoints.get( point ).getTimer();
            if ( tmr > 0 )
            {
                tmr--;
            }
            else
            {
                tmr = 1439;
            }
            mPoints.get( point ).setHour( tmr/60 );
            mPoints.get( point ).setMinute( tmr%60 );
        }
    }

    private void incTimer(final int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            int tmr = mPoints.get( point ).getTimer();
            if ( tmr < 1439 )
            {
                tmr++;
            }
            else
            {
                tmr = 0;
            }
            mPoints.get( point ).setHour( tmr/60 );
            mPoints.get( point ).setMinute( tmr%60 );
        }
    }

    private int getPointIndex(final int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            int count = mPoints.size();
            int[] index = new int[count];
            int[] tmr = new int[count];
            for ( int i = 0; i < count; i++ )
            {
                index[i] = i;
                tmr[i] = mPoints.get( i ).getTimer();
            }
            for ( int i = count-1; i > 0; i-- )
            {
                for ( int j = 0; j < i; j++ )
                {
                    if ( tmr[index[j]] > tmr[index[j+1]] )
                    {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }
            for ( int i = 0; i < count; i++ )
            {
                if ( index[i] == point )
                {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getLastPoint(final int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            int count = mPoints.size();
            int[] index = new int[count];
            int[] tmr = new int[count];
            for ( int i = 0; i < count; i++ )
            {
                index[i] = i;
                tmr[i] = mPoints.get( i ).getTimer();
            }
            for ( int i = count-1; i > 0; i-- )
            {
                for ( int j = 0; j < i; j++ )
                {
                    if ( tmr[index[j]] > tmr[index[j+1]] )
                    {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }
            for ( int i = 0; i < count; i++ )
            {
                if ( index[i] == point )
                {
                    return index[(count+i-1)%count];
                }
            }
        }
        return -1;
    }

    private void deletePoint(int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            if ( mPoints.size() <= 4 )
            {
                Toast.makeText( this, "Timepoints count min 4.", Toast.LENGTH_SHORT )
                     .show();
            }
            else
            {
                mPoints.remove( point );
                editpro_mps.setPointCount( mPoints.size() );
                for ( int i = 0; i < mPoints.size(); i++ )
                {
                    editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
                }
                editpro_mps.setSelectedPoint( 0 );
//                editpro_mps.clearSelectedPoint();
//                editpro_mps.setSelectedPoint( 0 );
//                refreshSelectedTimer();
//                mAdapter.setSelectedPoint( -1 );
//                editTimeDone();
            }
        }
    }

    private void addPoint(int hour, int minute)
    {
        if ( hour < 0 || hour > 23 || minute < 0 || minute > 59 )
        {
            return;
        }
        TimerBrightPoint tbp = new TimerBrightPoint( hour, minute, DeviceUtil.getChannelCount( devid ) );
        mPoints.add( tbp );
        editpro_mps.setPointCount( mPoints.size() );
        int idx = mPoints.size() - 1;
        editpro_mps.setProgress( idx, mPoints.get( idx ).getTimer() );
        editpro_mps.setSelectedPoint( idx );
//        editTimeStart();
//        for ( int i = 0; i < mPoints.size(); i++ )
//        {
//            editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
//        }

//        int count = mPoints.size();
//        int[] index = new int[count];
//        int[] tmr = new int[count];
//        for ( int i = 0; i < count; i++ )
//        {
//            index[i] = i;
//            tmr[i] = mPoints.get( i ).getTimer();
//        }
//        for ( int i = count-1; i > 0; i-- )
//        {
//            for ( int j = 0; j < i; j++ )
//            {
//                if ( tmr[index[j]] > tmr[index[j+1]] )
//                {
//                    int tmp = index[j];
//                    index[j] = index[j+1];
//                    index[j+1] = tmp;
//                }
//            }
//        }
//        for ( int i = 0; i < count; i++ )
//        {
//            if ( index[i] == count - 1 )
//            {
//                editpro_mps.setSelectedPoint( index[i] );
////                refreshSelectedTimer();
////                mAdapter.setSelectedPoint( index[i] );
//                editTimeStart();
//                return;
//            }
//        }
    }

//    private int getTime( int tmr )
//    {
//        if ( tmr < 0 || tmr >= 1439 )
//        {
//            tmr = 0;
//        }
//        int m = tmr%5;
//        switch ( m )
//        {
//            case 1:
//            case 2:
//                tmr -= m;
//                break;
//            case 3:
//            case 4:
//                tmr += 5 - m;
//                break;
//        }
//        for ( int i = 0; i < mPoints.size(); i++ )
//        {
//            if ( tmr == mPoints.get( i ).getTimer() )
//            {
//
//            }
//        }
//    }

    private boolean isValidTime( int tmr )
    {
        if ( tmr < 0 || tmr > 1439 )
        {
            return false;
        }
        for ( int i = 0; i < mPoints.size(); i++ )
        {
            if ( tmr == mPoints.get( i ).getTimer() )
            {
                return false;
            }
        }
        return true;
    }

    private void showAddPointDialog()
    {
        if ( mPoints != null )
        {
            if ( mPoints.size() >= 10 )
            {
                Toast.makeText( this, "Timepoints count max 10.", Toast.LENGTH_SHORT )
                     .show();
                return;
            }
            //INTERVAL * COUNT = 60
            final int INTERVAL = 5;
            final int COUNT = 12;
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            final AlertDialog dialog = builder.create();
            View view = LayoutInflater.from( this ).inflate( R.layout.dialog_time_picker, null, false );
            final TimePicker tp = view.findViewById( R.id.dialog_timepicker );
            final Button btn_ok = view.findViewById( R.id.dialog_timer_ok );
            final Button btn_cancel = view.findViewById( R.id.dialog_timer_cancel );
            tp.setIs24HourView( true );
            View mv = tp.findViewById( Resources.getSystem().getIdentifier( "minute", "id", "android" ) );
            if ( mv != null && mv instanceof NumberPicker )
            {
                NumberPicker mnp = (NumberPicker) mv;
                mnp.setMinValue( 0 );
                mnp.setMaxValue( COUNT - 1 );
                DecimalFormat df = new DecimalFormat( "00" );
                String[] values = new String[COUNT];
                for ( int i = 0; i < values.length; i++ )
                {
                    values[i] = df.format( i*INTERVAL );
                }
                mnp.setDisplayedValues( values );
                int tmr = Calendar.getInstance().get( Calendar.HOUR_OF_DAY ) * 60 + Calendar.getInstance().get( Calendar.MINUTE ) + INTERVAL;
                if ( tmr >= 1440 )
                {
                    tmr -= 1440;
                }
                tmr -= tmr%INTERVAL;
                if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M )
                {
                    tp.setHour( tmr/60 );
                    tp.setMinute( (tmr%60)/INTERVAL );
                }
                else
                {
                    tp.setCurrentHour( tmr/60 );
                    tp.setCurrentMinute( (tmr%60)/INTERVAL );
                }
                btn_ok.setEnabled( isValidTime( tmr ) );
                btn_ok.setTextColor( getResources().getColor( isValidTime( tmr ) ? R.color.colorGreen : R.color.colorRed ) );
                tp.setOnTimeChangedListener( new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged( TimePicker view, int hourOfDay, int minute )
                    {
                        int v = hourOfDay*60 + minute * INTERVAL;
                        boolean valid = isValidTime( v );
                        btn_ok.setEnabled( valid );
                        btn_ok.setTextColor( getResources().getColor( valid ? R.color.colorGreen : R.color.colorRed ) );
                        //                        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setEnabled( isValidTime( v ) ? true : false );
                    }
                } );
                btn_cancel.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v )
                    {
                        dialog.dismiss();
                    }
                } );
                btn_ok.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v )
                    {
                        int hour;
                        int minute;
                        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M )
                        {
                            hour = tp.getHour();
                            minute = tp.getMinute()*INTERVAL;
                        }
                        else
                        {
                            hour = tp.getCurrentHour();
                            minute = tp.getCurrentMinute()*INTERVAL;
                        }
                        addPoint( hour, minute );
                        dialog.dismiss();
                    }
                } );
                dialog.setView( view );
//                dialog.setButton( DialogInterface.BUTTON_NEGATIVE, getString( R.string.cancel ), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick( DialogInterface dialog, int which )
//                    {
//
//                    }
//                } );
//                dialog.setButton( DialogInterface.BUTTON_POSITIVE, getString( R.string.dialog_ok ), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick( DialogInterface dialog, int which )
//                    {
//                        int hour;
//                        int minute;
//                        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M )
//                        {
//                            hour = tp.getHour();
//                            minute = tp.getMinute()*INTERVAL;
//                        }
//                        else
//                        {
//                            hour = tp.getCurrentHour();
//                            minute = tp.getCurrentMinute()*INTERVAL;
//                        }
//                        addPoint( hour, minute );
//                    }
//                } );
                dialog.setTitle( "Add Time" );
                dialog.setCanceledOnTouchOutside( false );
                dialog.show();
                dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setEnabled( isValidTime( tmr ) ? true : false );
            }
        }
    }

    private void showDeletePointDialog( final int point )
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            if ( mPoints.size() <= 4 )
            {
                Toast.makeText( this, "Timepoints count min 4.", Toast.LENGTH_SHORT )
                     .show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setTitle( "Delete #" + (getPointIndex( point )+1) + " Set Time" );
                builder.setNegativeButton( R.string.cancel, null );
                builder.setPositiveButton( R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        mPoints.remove( point );
//                        editpro_mps.setPointCount( mPoints.size() );
//                        for ( int i = 0; i < mPoints.size(); i++ )
//                        {
//                            editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
//                        }
//                        editpro_mps.clearSelectedPoint();
//                        refreshSelectedTimer();
//                        mAdapter.setSelectedPoint( -1 );
                        editpro_mps.removePoint( point );
                        editpro_mps.setSelectedPoint( 0 );
//                        editTimeDone();
                        refreshChart();
                    }
                } );
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside( false );
                dialog.show();
            }
        }
    }

    private int getNextPoint(final int point)
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            int count = mPoints.size();
            int[] index = new int[count];
            int[] tmr = new int[count];
            for ( int i = 0; i < count; i++ )
            {
                index[i] = i;
                tmr[i] = mPoints.get( i ).getTimer();
            }
            for ( int i = count-1; i > 0; i-- )
            {
                for ( int j = 0; j < i; j++ )
                {
                    if ( tmr[index[j]] > tmr[index[j+1]] )
                    {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }
            for ( int i = 0; i < count; i++ )
            {
                if ( index[i] == point )
                {
                    return index[(i+1)%count];
                }
            }
        }
        return -1;
    }

    private void showMultiTouchPointDialog( final List<Integer> points )
    {
        if ( points == null || points.size() <= 1 || points.size() > mPoints.size() )
        {
            return;
        }
        DecimalFormat df = new DecimalFormat( "00" );
        String[] array = new String[points.size()];
        int idx;
        int tmr;
        for ( int i = 0; i < array.length; i++ )
        {
            idx = points.get( i );
            tmr = mPoints.get( idx ).getTimer();
            array[i] = "#" + df.format( getPointIndex( idx ) + 1) + "  " + df.format( tmr/60 ) + ":" + df.format( tmr%60 );
        }
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setSingleChoiceItems( array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {
                editpro_mps.setSelectedPoint( points.get( which ) );
//                editTimeStart();
                dialog.dismiss();
            }
        } );
        builder.show();
    }

    class EditproAdapter extends RecyclerView.Adapter<EditproViewHolder>
    {
        private int mSelectedPoint = -1;

        public void setSelectedPoint( int selectedPoint )
        {
            mSelectedPoint = selectedPoint;
            notifyDataSetChanged();
        }

        public String getPercent(int percent)
        {
            if ( percent >= 0 && percent < 10 )
            {
                return "  " + percent +"%";
            }
            else if ( percent >= 10 && percent < 100 )
            {
                return " " + percent + "%";
            }
            else if ( percent == 100 )
            {
                return "100%";
            }
            return "";
        }

        @NonNull
        @Override
        public EditproViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
        {
            EditproViewHolder holder = new EditproViewHolder( LayoutInflater.from( EditproActivity.this ).inflate( R.layout.item_point_bright, parent, false ) );
            return holder;
        }

        @Override
        public void onBindViewHolder( @NonNull final EditproViewHolder holder, final int position )
        {
            if ( mSelectedPoint >= 0 && mSelectedPoint < mPoints.size() )
            {
                holder.itemView.setVisibility( View.VISIBLE );
                final TimerBrightPoint tbp = mPoints.get( mSelectedPoint );
                int[] thumbs = DeviceUtil.getThumb( devid );
                int[] seekBars = DeviceUtil.getSeekbar( devid );
                Channel[] channels = DeviceUtil.getLightChannel( EditproActivity.this, devid );
                holder.iv_icon.setImageResource( channels[holder.getAdapterPosition()].getIcon() );
                if ( seekBars != null && position < seekBars.length )
                {
                    Drawable progressDrawable = getResources().getDrawable( seekBars[position] );
                    holder.seekbar.setProgressDrawable( progressDrawable );
                }
                if ( thumbs != null && position < thumbs.length )
                {
                    Drawable thumb = getResources().getDrawable( thumbs[position] );
                    holder.seekbar.setThumb( thumb );
                }
                holder.seekbar.setProgress(tbp.getBrights()[position]);
                holder.tv_percent.setText( getPercent( tbp.getBrights()[position] ) );
                holder.seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
                    {
                        mPoints.get( mSelectedPoint ).getBrights()[position] = (byte) progress;
                        holder.tv_percent.setText( getPercent( progress ) );
                        refreshChart();
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
            }
            else
            {
                holder.itemView.setVisibility( View.INVISIBLE );
            }
        }

        @Override
        public int getItemCount()
        {
//            return (mSelectedPoint >= 0 && mSelectedPoint < mPoints.size()) ? DeviceUtil.getChannelCount( devid ) : 0;
            return DeviceUtil.getChannelCount( devid );
        }
    }

    class EditproViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView iv_icon;
        private SeekBar seekbar;
        private TextView tv_percent;

        public EditproViewHolder( View itemView )
        {
            super( itemView );
            iv_icon = itemView.findViewById( R.id.item_point_bright_icon );
            seekbar = itemView.findViewById( R.id.item_point_bright_seekbar );
            tv_percent = itemView.findViewById( R.id.item_point_bright_percent );
        }
    }
}
