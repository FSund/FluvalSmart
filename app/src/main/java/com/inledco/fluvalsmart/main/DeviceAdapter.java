package com.inledco.fluvalsmart.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.liruya.swiperecyclerview.BaseSwipeAdapter;
import com.liruya.swiperecyclerview.SwipeLayout;
import com.liruya.swiperecyclerview.SwipeViewHolder;

import java.util.Collections;
import java.util.List;

/**
 * Created by liruya on 2016/10/26.
 */

public class DeviceAdapter extends BaseSwipeAdapter<DeviceAdapter.DeviceViewHolder> {
    private List<DevicePrefer> mDevices;
    private boolean mDragEnable;

    public DeviceAdapter(@NonNull Context context, List<DevicePrefer> devices) {
        super(context);
        mDevices = devices;
    }

    public boolean isDragEnable() {
        return mDragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        mDragEnable = dragEnable;
        notifyDataSetChanged();
    }

    public void refresh(List<DevicePrefer> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    protected int getLayoutResID(int viewType) {
        return R.layout.item_device;
    }

    @Override
    protected DeviceViewHolder createSwipeViewHolder(SwipeLayout swipeLayout) {
        return new DeviceViewHolder(swipeLayout);
    }

    @Override
    protected void bindSwipeViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DevicePrefer device = mDevices.get(holder.getAdapterPosition());
        holder.icon.setImageResource(DeviceUtil.getDeviceIcon(device.getDevId()));
        holder.name.setText(device.getDeviceName());
        holder.tank.setText(DeviceUtil.getDeviceType(device.getDevId()));
        holder.sort.setVisibility(mDragEnable ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onMove(int from, int to) {
        Collections.swap(mDevices, from, to);
        notifyItemMoved(from, to);
    }

    class DeviceViewHolder extends SwipeViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView tank;
        private ImageButton sort;

        public DeviceViewHolder(@NonNull SwipeLayout view) {
            super(view);
            icon = itemView.findViewById(R.id.item_device_content_icon);
            name = itemView.findViewById(R.id.item_device_content_name);
            tank = itemView.findViewById(R.id.item_device_content_tank);
            sort = itemView.findViewById(R.id.item_device_content_sort);
        }
    }
}

//public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
//    private Context mContext;
//    private List<DevicePrefer> mDevices;
//    private SwipeItemListener mSwipeItemListener;
//
//    public DeviceAdapter(Context context, List<DevicePrefer> devices) {
//        mContext = context;
//        mDevices = devices;
//    }
//
//    public void setSwipeItemListener(SwipeItemListener listener) {
//        mSwipeItemListener = listener;
//    }
//
//    @Override
//    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        DeviceViewHolder holder = new DeviceViewHolder(LayoutInflater.from(mContext)
//                                                                     .inflate(R.layout.item_device_with_action, parent, false));
//        return holder;
//    }
//
//    @Override
//    public void onBindViewHolder(final DeviceViewHolder holder, int position) {
//        DevicePrefer device = mDevices.get(holder.getAdapterPosition());
//        holder.icon.setImageResource(DeviceUtil.getDeviceIcon(device.getDevId()));
//        holder.name.setText(device.getDeviceName());
//        holder.tank.setText(DeviceUtil.getDeviceType(device.getDevId()));
//        holder.item_content.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mSwipeItemListener != null) {
//                    return mSwipeItemListener.onLongClick(holder.getAdapterPosition());
//                }
//                return false;
//            }
//        });
//        holder.item_content.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSwipeItemListener != null) {
//                    mSwipeItemListener.onClickContent(holder.getAdapterPosition());
//                }
//            }
//        });
//        holder.tv_remove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSwipeItemListener != null) {
//                    mSwipeItemListener.onClickAction(v.getId(), holder.getAdapterPosition());
//                }
//            }
//        });
//        holder.tv_reset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSwipeItemListener != null) {
//                    mSwipeItemListener.onClickAction(v.getId(), holder.getAdapterPosition());
//                }
//            }
//        });
//        holder.tv_upgrade.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSwipeItemListener != null) {
//                    mSwipeItemListener.onClickAction(v.getId(), holder.getAdapterPosition());
//                }
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mDevices == null ? 0 : mDevices.size();
//    }
//
//    public class DeviceViewHolder extends SwipeItemViewHolder {
//        private ImageView icon;
//        private TextView name;
//        private TextView tank;
//        private TextView tv_remove;
//        private TextView tv_reset;
//        private TextView tv_upgrade;
//        private View item_content;
//        private View item_action;
//
//        public DeviceViewHolder(View itemView) {
//            super(itemView);
//            icon = itemView.findViewById(R.id.item_device_icon);
//            name = itemView.findViewById(R.id.item_device_name);
//            tank = itemView.findViewById(R.id.item_device_tank);
//            tv_remove = itemView.findViewById(R.id.item_action_remove);
//            tv_reset = itemView.findViewById(R.id.item_action_reset_psw);
//            tv_upgrade = itemView.findViewById(R.id.item_action_upgrade);
//            item_content = itemView.findViewById(R.id.item_content);
//            item_action = itemView.findViewById(R.id.item_action);
//        }
//
//        @Override
//        public int getActionWidth() {
//            return item_action.getWidth();
//        }
//
//        @Override
//        public View getContentView() {
//            return item_content;
//        }
//    }
//}
