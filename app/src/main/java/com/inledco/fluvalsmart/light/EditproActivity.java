package com.inledco.fluvalsmart.light;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.LightPro;
import com.inledco.fluvalsmart.bean.PointComparator;
import com.inledco.fluvalsmart.bean.TimerBrightPoint;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LineChartHelper;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
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
    private ImageButton editpro_ib_remove;
    private ImageButton editpro_ib_add;
    private Button editpro_btn_cancel;
    private Button editpro_btn_save;
    private RecyclerView editpro_rv_show;

    private short devid;
    private String mAddress;
    private List<TimerBrightPoint> mPoints;
    private final PointComparator mComparator = new PointComparator();
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
        super.onBackPressed();
    }

    @Override
    protected void initView()
    {
        editpro_linechart = findViewById( R.id.editpro_linechart );
        editpro_mps = findViewById( R.id.editpro_mps );
        editpro_ib_remove = findViewById( R.id.editpro_ib_remove );
        editpro_ib_add = findViewById( R.id.editpro_ib_add );
        editpro_btn_cancel = findViewById( R.id.editpro_btn_cancel );
        editpro_btn_save = findViewById( R.id.editpro_btn_save );
        editpro_rv_show = findViewById( R.id.editpro_rv_show );

        LineChartHelper.init(editpro_linechart);
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
                }
                else
                {
                    editpro_ib_remove.setVisibility( View.GONE );
                }
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
                    int progress = editpro_mps.getProgressByIndex( index );
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setMinute( progress%60 );
                    refreshChart();
                }
            }

            @Override
            public void onPointProgressChanged( int index, int progress, boolean fromUser )
            {
                if ( mPoints != null && index >= 0 && index < mPoints.size() && progress >= 0 && progress < 1440 )
                {
                    mPoints.get( index ).setHour( progress/60 );
                    mPoints.get( index ).setMinute( progress%60 );
                    refreshChart();
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
                    setResult(1);
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
                for ( int i = 0; i < count; i++ )
                {
                    mPoints.add( lightPro.getPoints()[i] );
                }
                Collections.sort( mPoints, mComparator );

                editpro_mps.setPointCount( count );

                for ( int i = 0; i < count; i++ )
                {
                    editpro_mps.setProgress( i, lightPro.getPoints()[i].getTimer() );
                }

                mAdapter = new EditproAdapter();
                editpro_rv_show.setAdapter( mAdapter );

                editpro_mps.setSelectedPoint( 0 );
                refreshChart();
            }
        }
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

//    private void deletePoint(int point)
//    {
//        if ( mPoints != null && point >= 0 && point < mPoints.size() )
//        {
//            if ( mPoints.size() <= 4 )
//            {
//                Toast.makeText( this, "Timepoints count min 4.", Toast.LENGTH_SHORT )
//                     .show();
//            }
//            else
//            {
//                mPoints.remove( point );
//                editpro_mps.setPointCount( mPoints.size() );
//                for ( int i = 0; i < mPoints.size(); i++ )
//                {
//                    editpro_mps.setProgress( i, mPoints.get( i ).getTimer() );
//                }
//                editpro_mps.setSelectedPoint( 0 );
////                editpro_mps.clearSelectedPoint();
////                editpro_mps.setSelectedPoint( 0 );
////                refreshSelectedTimer();
////                mAdapter.setSelectedPoint( -1 );
////                editTimeDone();
//            }
//        }
//    }

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

    }

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
                Toast.makeText( this, R.string.tip_timepoints_max, Toast.LENGTH_SHORT )
                     .show();
                return;
            }
            //INTERVAL * COUNT = 60
            final int INTERVAL = 5;
            final int COUNT = 12;
//            AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
            CustomDialogBuilder builder = new CustomDialogBuilder(this, R.style.DialogTheme );
            View view = LayoutInflater.from( this ).inflate( R.layout.dialog_time_picker, null, false );
            builder.setTitle( R.string.title_add_time );
            builder.setView(view);
            final AlertDialog dialog = builder.show();
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
            }
        }
    }

    private void showDeletePointDialog( final int point )
    {
        if ( mPoints != null && point >= 0 && point < mPoints.size() )
        {
            if ( mPoints.size() <= 4 )
            {
                Toast.makeText( this, R.string.tip_timepoints_min, Toast.LENGTH_SHORT )
                     .show();
            }
            else
            {
                CustomDialogBuilder builder = new CustomDialogBuilder( this, R.style.DialogTheme );
                builder.setTitle( getString(R.string.title_delete_timpoint).replace("{index}", "" + (getPointIndex(point)+1) ) );
                builder.setNegativeButton( R.string.cancel, null );
                builder.setPositiveButton( R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        mPoints.remove( point );
                        editpro_mps.removePoint( point );
                        editpro_mps.setSelectedPoint( 0 );
                        refreshChart();
                    }
                } );
                builder.show();
//                AlertDialog dialog = builder.create();
//                dialog.setCanceledOnTouchOutside( false );
//                dialog.show();
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
