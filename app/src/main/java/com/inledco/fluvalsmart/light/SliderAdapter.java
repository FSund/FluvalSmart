package com.inledco.fluvalsmart.light;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by liruya on 2016/10/28.
 */

public class SliderAdapter extends BaseAdapter
{
    private Context mContext;
    private String mac;
    private short devid;
    private ArrayList< Channel > mChannels;
    private static long msc;

    public SliderAdapter ( Context context, String mac, short devid, ArrayList< Channel > channels )
    {
        mContext = context;
        this.mac = mac;
        this.devid = devid;
        mChannels = channels;
        msc = System.currentTimeMillis();
    }

    @Override
    public int getCount ()
    {
        return mChannels == null ? 0 : mChannels.size();
    }

    @Override
    public Object getItem ( int position )
    {
        return mChannels.get( position );
    }

    @Override
    public long getItemId ( int position )
    {
        return position;
    }

    @Override
    public View getView ( final int position, View convertView, ViewGroup parent )
    {
        ViewHolder holder = null;
        if ( convertView == null )
        {
            convertView = LayoutInflater.from( mContext )
                                        .inflate( R.layout.item_slider, parent, false );
            holder = new ViewHolder();
            holder.tv_name = convertView.findViewById( R.id.slider_chn_name );
            holder.slider = convertView.findViewById( R.id.item_chn_slider );
            holder.tv_percent = convertView.findViewById( R.id.item_chn_percent );
            convertView.setTag( holder );
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        final Channel channel = mChannels.get( position );
        //        holder.layout.setBackgroundColor( channel.getColor() );
        holder.tv_name.setText( channel.getName() );
        holder.slider.setProgress( channel.getValue() );
        int[] thumbs = DeviceUtil.getThumb( devid );
        int[] seekBars = DeviceUtil.getSeekbar( devid );
        if ( thumbs != null && position < thumbs.length )
        {
            Drawable progressDrawable = mContext.getResources()
                                                .getDrawable( seekBars[position] );
            holder.slider.setProgressDrawable( progressDrawable );
        }
        if ( seekBars != null && position < seekBars.length )
        {
            Drawable thumb = mContext.getResources()
                                     .getDrawable( thumbs[position] );
            holder.slider.setThumb( thumb );
        }
        final TextView percent = holder.tv_percent;
//        percent.setText( channel.getValue() / 10 + "%" );
        percent.setText( getPercent( channel.getValue() ) );
        holder.slider.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged ( SeekBar seekBar, int progress, boolean fromUser )
            {
//                percent.setText( progress / 10 + "%" );
                percent.setText( getPercent( (short) progress ) );
                if ( fromUser )
                {
                    long t = System.currentTimeMillis();
                    if ( t - msc > 32 )
                    {
                        short[] values = new short[getCount()];
                        for ( int i = 0; i < values.length; i++ )
                        {
                            values[i] = (short) 0xFFFF;
                        }
                        values[position] = (short) progress;
                        CommUtil.setLed( mac, values );
                        msc = t;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch ( SeekBar seekBar )
            {

            }

            @Override
            public void onStopTrackingTouch ( final SeekBar seekBar )
            {
//                percent.setText( seekBar.getProgress() / 10 + "%" );
                percent.setText( getPercent( (short) seekBar.getProgress() ) );
                channel.setValue( (short) seekBar.getProgress() );
                new Handler().postDelayed( new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        short[] values = new short[getCount()];
                        for ( int i = 0; i < values.length; i++ )
                        {
                            values[i] = (short) 0xFFFF;
                        }
                        values[position] = (short) seekBar.getProgress();
                        CommUtil.setLed( mac, values );
                    }
                }, 64 );
            }
        } );
        return convertView;
    }

    private String getPercent ( short value )
    {
//        if ( value >= 1000 )
//        {
//            return "100%";
//        }
//        if ( value <= 0 )
//        {
//            return "0.0%";
//        }
//        if ( value > 99 )
//        {
//            return value/10 + "%";
//        }
//        float fl = ((float)value)/10;
//        DecimalFormat df = new DecimalFormat( "0.0" );
//        return df.format( fl )+"%";
        if ( value > 1000 )
        {
            return "100%";
        }
        DecimalFormat df = new DecimalFormat( "##0" );
        return df.format( value/10 ) + "%";
    }

    class ViewHolder
    {
        private TextView tv_name;
        private SeekBar slider;
        private TextView tv_percent;
    }
}
