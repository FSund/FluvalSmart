package com.inledco.fluvalsmart.light;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.util.DeviceUtil;

import java.util.ArrayList;

/**
 * Created by liruya on 2016/11/7.
 */

public class ExpanSliderAdapter extends BaseAdapter
{
    private Context mContext;
    private short devid;
    private ArrayList< Channel > mChannels;
    private ItemChangeListener mItemChangeListener;

    public ExpanSliderAdapter( Context context, short id, ArrayList< Channel > channels, @NonNull ItemChangeListener listener )
    {
        mContext = context;
        devid = id;
        mChannels = channels;
        mItemChangeListener = listener;
    }

    @Override
    public int getCount()
    {
        return mChannels == null ? 0 : mChannels.size();
    }

    @Override
    public Object getItem( int position )
    {
        return mChannels == null ? null : mChannels.get( position );
    }

    @Override
    public long getItemId( int position )
    {
        return position;
    }

    @Override
    public View getView( final int position, View convertView, ViewGroup parent )
    {
        ViewHolder holder;
        if ( convertView == null )
        {
            convertView = LayoutInflater.from( mContext )
                                        .inflate( R.layout.item_daynight_expan_slider, parent, false );
            holder = new ViewHolder();
            holder.tv_name = convertView.findViewById( R.id.item_expan_chn_name );
            holder.slider = convertView.findViewById( R.id.item_expan_chn_slider );
            holder.tv_percent = convertView.findViewById( R.id.item_expan_chn_percent );
            convertView.setTag( holder );
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        final Channel channel = mChannels.get( position );
        final TextView percent = holder.tv_percent;
        holder.tv_name.setText( channel.getName() );
        int[] thumbs = DeviceUtil.getThumb( devid );
        int[] seekBars = DeviceUtil.getSeekbar( devid );
        if ( seekBars != null && position < seekBars.length )
        {
            Drawable progressDrawable = mContext.getResources()
                                                .getDrawable( seekBars[position] );
            holder.slider.setProgressDrawable( progressDrawable );
        }
        if ( thumbs != null && position < thumbs.length )
        {
            Drawable thumb = mContext.getResources()
                                     .getDrawable( thumbs[position] );
            holder.slider.setThumb( thumb );
        }
        holder.slider.setProgress( channel.getValue() );
        percent.setText( channel.getValue() + "%" );
        holder.slider.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
            {
                if ( fromUser )
                {
                    channel.setValue( (short) progress );
                    percent.setText( progress + "%" );
                    if ( mItemChangeListener != null )
                    {
                        mItemChangeListener.onItemChanged( position, progress );
                    }
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
        return convertView;
    }

    class ViewHolder
    {
        TextView tv_name;
        SeekBar slider;
        TextView tv_percent;
    }

    public interface ItemChangeListener
    {
        void onItemChanged( int position, int newValue );
    }
}
