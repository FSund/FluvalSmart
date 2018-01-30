package com.inledco.fluvalsmart.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.BaseDevice;
import com.inledco.fluvalsmart.impl.SwipeItemActionClickListener;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.itemtouchhelperextension.SwipeItemViewHolder;

import java.util.List;

/**
 * Created by liruya on 2016/10/26.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>
{
    private Context mContext;
    private List< BaseDevice > mDevices;
    private SwipeItemActionClickListener mSwipeItemActionClickListener;

    public DeviceAdapter ( Context context, List< BaseDevice > devices )
    {
        mContext = context;
        mDevices = devices;
    }

    public void setSwipeItemActionClickListener( SwipeItemActionClickListener listener )
    {
        mSwipeItemActionClickListener = listener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder ( ViewGroup parent, int viewType )
    {
        DeviceViewHolder holder = new DeviceViewHolder( LayoutInflater.from( mContext )
                                                     .inflate( R.layout.item_device_with_action, parent, false ) );
        return holder;
    }

    @Override
    public void onBindViewHolder ( final DeviceViewHolder holder, int position )
    {
        BaseDevice device = mDevices.get( holder.getAdapterPosition() );
        holder.iv_icon.setImageResource( DeviceUtil.getDeviceIcon( device.getDevicePrefer()
                                                                        .getDevId() ) );
        holder.tv_name.setText( device.getDevicePrefer()
                                   .getDeviceName() );
        holder.tv_tank.setText( DeviceUtil.getDeviceType( device.getDevicePrefer().getDevId() ) );
        holder.item_content.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( mSwipeItemActionClickListener != null )
                {
                    mSwipeItemActionClickListener.onClickContent( holder.getAdapterPosition() );
                }
            }
        } );
        holder.tv_remove.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( mSwipeItemActionClickListener != null )
                {
                    mSwipeItemActionClickListener.onClickAction( v.getId(), holder.getAdapterPosition() );
                }
            }
        } );
        holder.tv_upgrade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                if ( mSwipeItemActionClickListener != null )
                {
                    mSwipeItemActionClickListener.onClickAction( v.getId(), holder.getAdapterPosition() );
                }
            }
        } );
    }

    @Override
    public int getItemCount ()
    {
        return mDevices == null ? 0 : mDevices.size();
    }

    public class DeviceViewHolder extends SwipeItemViewHolder
    {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_tank;
        private TextView tv_remove;
        private TextView tv_upgrade;
        private View item_content;
        private View item_action;

        public DeviceViewHolder ( View itemView )
        {
            super( itemView );
            iv_icon = (ImageView) itemView.findViewById( R.id.item_device_icon );
            tv_name = (TextView) itemView.findViewById( R.id.item_device_name );
            tv_tank = (TextView) itemView.findViewById( R.id.item_device_tank );
            tv_remove = (TextView) itemView.findViewById( R.id.item_action_remove );
            tv_upgrade = (TextView) itemView.findViewById( R.id.item_action_upgrade );
            item_content = itemView.findViewById( R.id.item_content );
            item_action = itemView.findViewById( R.id.item_action );
        }

        @Override
        public float getActionWidth ()
        {
            return item_action.getWidth();
        }

        @Override
        public View getContentView ()
        {
            return item_content;
        }
    }
}
