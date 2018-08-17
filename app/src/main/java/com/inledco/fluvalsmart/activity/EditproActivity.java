package com.inledco.fluvalsmart.activity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
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
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EditproActivity extends BaseActivity
{
    private LineChart editpro_linechart;
    private MultiPointSeekbar editpro_mps;
    private ImageButton editpro_ib_last;
    private ImageButton editpro_ib_next;
    private ImageButton editpro_ib_dec;
    private ImageButton editpro_ib_inc;
    private TextView editpro_tv_tmr;
    private ImageButton editpro_ib_remove;
    private ImageButton editpro_ib_add;
    private Button editpro_btn_cancel;
    private Button editpro_btn_save;
    private RecyclerView editpro_rv_show;

    private short devid;
    private String mAddress;
    private List<TimerBrightPoint> mPoints;
    private PointComparator mComparator;
    private EditproAdapter mAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_editpro );

        initView();
        initData();
        initEvent();
    }

    @Override
    protected void initView()
    {
        editpro_linechart = findViewById( R.id.editpro_linechart );
        editpro_mps = findViewById( R.id.editpro_mps );
        editpro_ib_last = findViewById( R.id.editpro_ib_last );
        editpro_ib_next = findViewById( R.id.editpro_ib_next );
        editpro_ib_dec = findViewById( R.id.editpro_ib_dec );
        editpro_ib_inc = findViewById( R.id.editpro_ib_inc );
        editpro_tv_tmr = findViewById( R.id.editpro_tv_tmr );
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
                if ( mPoints != null && index >= 0 && index < mPoints.size() )
                {
                    DecimalFormat df = new DecimalFormat( "00" );
                    editpro_tv_tmr.setText( df.format( mPoints.get( index ).getHour() ) + ":" + df.format( mPoints.get( index ).getMinute() ) );
                }
            }

            @Override
            public void onStartPointTouch( int index )
            {

            }

            @Override
            public void onStopPointTouch( int index )
            {

            }

            @Override
            public void onPointProgressChanged( int index, int progress, boolean fromUser )
            {
                if ( mPoints != null && index >= 0 && index < mPoints.size() && progress >= 0 && progress < 1440 )
                {
                    DecimalFormat df = new DecimalFormat( "00" );
                    editpro_tv_tmr.setText( df.format( progress/60 ) + ":" + df.format( progress%60 ) );
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setMinute( progress%60 );
                    refreshChart();
                }
            }
        } );
        editpro_ib_dec.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                int pt = editpro_mps.getSelectedPoint();
                decTimer( pt );
                editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
                refreshSelectedTimer();
                refreshChart();
            }
        } );
        editpro_ib_dec.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( final View v )
            {
                final Timer timer = new Timer();
                timer.schedule( new TimerTask() {
                    @Override
                    public void run()
                    {
                        if ( v.isPressed() )
                        {
                            final int pt = editpro_mps.getSelectedPoint();
                            decTimer( pt );
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
                                    refreshSelectedTimer();
                                    refreshChart();
                                }
                            } );
                        }
                        else
                        {
                            cancel();
                            timer.cancel();
                        }
                    }
                }, 0, 20 );
                return true;
            }
        } );

        editpro_ib_inc.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                int pt = editpro_mps.getSelectedPoint();
                incTimer( pt );
                editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
                refreshSelectedTimer();
                refreshChart();
            }
        } );
        editpro_ib_inc.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( final View v )
            {
                final Timer timer = new Timer();
                timer.schedule( new TimerTask() {
                    @Override
                    public void run()
                    {
                        if ( v.isPressed() )
                        {
                            final int pt = editpro_mps.getSelectedPoint();
                            incTimer( pt );
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    editpro_mps.setProgress( pt, mPoints.get( pt ).getTimer() );
                                    refreshSelectedTimer();
                                    refreshChart();
                                }
                            } );
                        }
                        else
                        {
                            cancel();
                            timer.cancel();
                        }
                    }
                }, 0, 20 );
                return true;
            }
        } );

        editpro_ib_last.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                int pt = getLastPoint( editpro_mps.getSelectedPoint() );
                if ( pt >= 0 && pt < mPoints.size() )
                {
                    editpro_mps.setSelectedPoint( pt );
                    refreshSelectedTimer();
                    mAdapter.setSelectedPoint( pt );
                }
            }
        } );

        editpro_ib_next.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v )
            {
                int pt = getNextPoint( editpro_mps.getSelectedPoint() );
                if ( pt >= 0 && pt < mPoints.size() )
                {
                    editpro_mps.setSelectedPoint( pt );
                    refreshSelectedTimer();
                    mAdapter.setSelectedPoint( pt );
                }
            }
        } );

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
                editpro_mps.setMax( 1439 );
                editpro_mps.setPointCount( count );

                mPoints = new ArrayList<>();
                mComparator = new PointComparator();
                for ( int i = 0; i < count; i++ )
                {
                    mPoints.add( lightPro.getPoints()[i] );
                }
                Collections.sort( mPoints, mComparator );
                for ( int i = 0; i < count; i++ )
                {
                    editpro_mps.setProgress( i, lightPro.getPoints()[i].getTimer() );
                }

                mAdapter = new EditproAdapter();
                mAdapter.setSelectedPoint( 0 );
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

    private void refreshSelectedTimer()
    {
        if ( mPoints != null )
        {
            int sp = editpro_mps.getSelectedPoint();
            if ( sp >= 0 && sp < mPoints.size() )
            {
                DecimalFormat df = new DecimalFormat( "00" );
                editpro_tv_tmr.setText( df.format( mPoints.get( sp ).getHour() ) + ":" + df.format( mPoints.get( sp ).getMinute() ) );
            }
        }
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
                Toast.makeText( this, "Points count min 4.", Toast.LENGTH_SHORT )
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
                refreshSelectedTimer();
                mAdapter.setSelectedPoint( 0 );
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
        for ( int i = 0; i < mPoints.size(); i++ )
        {
            editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
        }

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
            if ( index[i] == count - 1 )
            {
                editpro_mps.setSelectedPoint( index[i] );
                refreshSelectedTimer();
                mAdapter.setSelectedPoint( index[i] );
                return;
            }
        }
    }

    private void showAddPointDialog()
    {
        if ( mPoints != null )
        {
            if ( mPoints.size() >= 10 )
            {
                Toast.makeText( this, "Points count max 10.", Toast.LENGTH_SHORT )
                     .show();
                return;
            }
            TimePickerDialog dialog = new TimePickerDialog( this, new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet( TimePicker view, int hourOfDay, int minute )
                {
                    addPoint( hourOfDay, minute );
                }
            }, 0, 0, true );
            dialog.setTitle( "Add Timer" );
            dialog.setCanceledOnTouchOutside( false );
            dialog.show();
        }
    }

    private void showDeletePointDialog( final int point )
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            if ( mPoints.size() <= 4 )
            {
                Toast.makeText( this, "Points count min 4.", Toast.LENGTH_SHORT )
                     .show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setTitle( "Remove Timer" );
                builder.setNegativeButton( R.string.cancel, null );
                builder.setPositiveButton( R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        mPoints.remove( point );
                        editpro_mps.setPointCount( mPoints.size() );
                        for ( int i = 0; i < mPoints.size(); i++ )
                        {
                            editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
                        }
                        editpro_mps.setSelectedPoint( 0 );
                        refreshSelectedTimer();
                        mAdapter.setSelectedPoint( 0 );
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

    class EditproAdapter extends RecyclerView.Adapter<EditproViewHolder>
    {
        private int mSelectedPoint;

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
        }

        @Override
        public int getItemCount()
        {
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
