package com.inledco.fluvalsmart.scan;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.SelectDevice;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.liruya.tuner168blemanager.BleManager;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {
    private static final String TAG = "ScanAdapter";
    private Context mContext;
    private Handler mHandler;
    private ArrayList<SelectDevice> mSelectDevices;
    private boolean mShowRssi;

    public ScanAdapter(Context context, @NonNull Handler handler, ArrayList<SelectDevice> selectDevices) {
        mContext = context;
        mHandler = handler;
        mSelectDevices = selectDevices;
        mShowRssi = false;
    }

    public boolean isShowRssi() {
        return mShowRssi;
    }

    public void setShowRssi(boolean showRssi) {
        mShowRssi = showRssi;
    }

    @Override
    public ScanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ScanViewHolder holder = new ScanViewHolder(LayoutInflater.from(mContext)
                                                                 .inflate(R.layout.item_scan, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final ScanViewHolder holder, int position) {
//        final SelectDevice device = mSelectDevices.get(holder.getAdapterPosition());
        final SelectDevice device = mSelectDevices.get(position);
        short devid = device.getPrefer()
                            .getDevId();
        holder.iv_icon.setImageResource(DeviceUtil.getDeviceIcon(devid));
        holder.tv_name.setText(device.getPrefer()
                                     .getDeviceName());
        holder.tv_type.setText(DeviceUtil.getDeviceType(devid));
        holder.cb_sel.setEnabled(device.isSelectable());
        holder.cb_sel.setChecked(device.isSelected());
        if (mShowRssi) {
            holder.pb_rssi.setVisibility(View.VISIBLE);
            holder.pb_rssi.setProgress(device.getRssi() + 100);
        }
        else {
            holder.pb_rssi.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!device.isSelectable() && !device.isSelected()) {
                    BleManager.getInstance()
                              .connectDevice(device.getPrefer()
                                                   .getDeviceMac());
                }
            }
        });
        holder.cb_sel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSelectDevices.get(holder.getAdapterPosition())
                              .setSelected(isChecked);
                mHandler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSelectDevices == null ? 0 : mSelectDevices.size();
    }

    /**
     * ViewHolder类
     */
    class ScanViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_type;
        private CheckBox cb_sel;
        private ProgressBar pb_rssi;

        public ScanViewHolder(View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_scan_iv_icon);
            tv_name = itemView.findViewById(R.id.item_scan_tv_name);
            tv_type = itemView.findViewById(R.id.item_scan_tv_type);
            cb_sel = itemView.findViewById(R.id.item_scan_cb_sel);
            pb_rssi = itemView.findViewById(R.id.item_scan_rssi);
        }
    }
}
