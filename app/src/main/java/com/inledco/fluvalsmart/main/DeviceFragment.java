package com.inledco.fluvalsmart.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.fluvalsmart.BuildConfig;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.constant.ConstVal;
import com.inledco.fluvalsmart.light.LightActivity;
import com.inledco.fluvalsmart.ota.BleOTAActivity;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.scan.ScanActivity;
import com.inledco.fluvalsmart.util.DevicePrefUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.util.LightPrefUtil;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.liruya.swiperecyclerview.ItemSwipeDragHelper;
import com.liruya.swiperecyclerview.SwipeItemListener;
import com.liruya.tuner168blemanager.BleHelper;
import com.liruya.tuner168blemanager.BleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends BaseFragment {
    private Toolbar device_toolbar;
    private TextView device_title;
    private ImageView device_iv_add;
    private TextView device_tv_add;
//    private LinearLayout device_ll;
//    private TextView device_sort_type;
//    private TextView device_sort_name;
    private RecyclerView device_rv_show;

    private final List<DevicePrefer> mDevices = new ArrayList<>();
    private DeviceAdapter mAdapter;

    private BleHelper mBleHelper;

    private ItemSwipeDragHelper<DeviceAdapter> mSwipeDragHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BleManager.getInstance().disConnectAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSwipeDragHelper != null) {
            mSwipeDragHelper.dettachRecyclerView();
        }
    }

    @Override
    protected void initView(View view) {
        device_toolbar = view.findViewById(R.id.device_toolbar);
        device_title = view.findViewById(R.id.device_title);
        device_iv_add = view.findViewById(R.id.device_iv_add);
        device_tv_add = view.findViewById(R.id.device_tv_add);
        device_rv_show = view.findViewById(R.id.device_rv_show);
//        device_ll = view.findViewById(R.id.device_ll);
//        device_sort_type = view.findViewById(R.id.device_sort_type);
//        device_sort_name = view.findViewById(R.id.device_sort_name);

        device_toolbar.inflateMenu(R.menu.menu_main);
        device_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), OrientationHelper.VERTICAL));
//        device_sort_type.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sort_24dp, 0, 0, 0);
//        device_sort_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sort_24dp, 0, 0, 0);
    }

    @Override
    protected void initData() {
        mBleHelper = new BleHelper((AppCompatActivity) getActivity());
        mDevices.addAll(DevicePrefUtil.getLocalDevices(getContext()));
        if (Setting.hasScanTip(getContext()) || mDevices.size() > 0) {
            device_iv_add.setVisibility(View.GONE);
            device_tv_add.setVisibility(View.GONE);
            device_rv_show.setVisibility(View.VISIBLE);
        } else {
            device_iv_add.setVisibility(View.VISIBLE);
            device_tv_add.setVisibility(View.VISIBLE);
            device_rv_show.setVisibility(View.GONE);
        }
        mAdapter = new DeviceAdapter(getContext(), mDevices);
        mAdapter.setSwipeItemListener(new SwipeItemListener() {
            @Override
            public boolean onContentLongClick(RecyclerView.ViewHolder holder, int postion) {
                if (!mSwipeDragHelper.isDragEnable()) {
                    startSort();
                }
                return true;
            }

            @Override
            public void onContentClick(RecyclerView.ViewHolder holder, int position) {
                if (mSwipeDragHelper.isDragEnable()) {
                    return;
                }
                if (mBleHelper.isBluetoothEnabled()) {
                    DevicePrefer device = mDevices.get(holder.getAdapterPosition());
                    Intent intent = new Intent(getContext(), LightActivity.class);
                    intent.putExtra("DevicePrefer", device);
                    getActivity().startActivityForResult(intent, ConstVal.RENAME_CODE);
                } else {
                    mBleHelper.requestBluetoothEnable(ConstVal.BLUETOOTH_REQUEST_ENABLE_CODE);
                }
            }

            @Override
            public void onActionClick(RecyclerView.ViewHolder holder, int position, @IdRes int id) {
                final int pos = holder.getAdapterPosition();
                switch (id) {
                    case R.id.item_device_action_remove:
                        showRemoveDeviceDialog(pos);
                        break;
                    //                    case R.id.item_device_action_reset_psw:
                    ////                        showResetPasswordDialog( mDevices.get( pos ).getDevicePrefer().getDeviceMac() );
                    //                        showRetrievePasswordDialog(mDevices.get(pos));
                    //                        break;
                    case R.id.item_device_action_upgrade:
                        Intent intent = new Intent(getContext(), BleOTAActivity.class);
                        intent.putExtra("devid", mDevices.get(pos).getDevId());
                        intent.putExtra("name", mDevices.get(pos).getDeviceName());
                        intent.putExtra("address", mDevices.get(pos).getDeviceMac());
                        intent.putExtra("mode", BuildConfig.FORCE_UPDATE);
                        startActivity(intent);
                        mSwipeDragHelper.closeOpened();
                        break;
                }
            }
        });

        mSwipeDragHelper = new ItemSwipeDragHelper<>();
        mSwipeDragHelper.attachRecyclerView(device_rv_show, mAdapter);
    }

    private void showRemoveDeviceDialog(final int position) {
        //        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme);
        builder.setTitle(R.string.remove_device);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mDevices.size() > position) {
                    mSwipeDragHelper.closeOpened();
                    String mac = mDevices.get(position).getDeviceMac();
                    mDevices.remove(position);
                    mAdapter.notifyItemRemoved(position);
                    DevicePrefUtil.setLocalDevices(getContext(), mDevices);
                    LightPrefUtil.removeLocalPassword(getContext(), mac);
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
        //        AlertDialog dialog = builder.create();
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.show();
    }

    @Override
    protected void initEvent() {
        device_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwipeDragHelper.isDragEnable()) {
                    stopSort();
                    mDevices.clear();
                    mDevices.addAll(DevicePrefUtil.getLocalDevices(getContext()));
                    mAdapter.notifyDataSetChanged();
                } else {
//                    startSort();
                    showSortDialog();
                }
            }
        });
        device_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_search_ble:
                        if (mBleHelper.checkLocationPermission()) {
                            startScanActivity();
                        } else {
                            mBleHelper.requestLocationPermission(ConstVal.PERMISSON_REQUEST_COARSE_CODE);
                        }
                        break;
                    case R.id.menu_done:
                        stopSort();
                        DevicePrefUtil.setLocalDevices(getContext(), mDevices);
                        break;
                }
                return false;
            }
        });
        device_iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBleHelper.checkLocationPermission()) {
                    startScanActivity();
                } else {
                    mBleHelper.requestLocationPermission(ConstVal.PERMISSON_REQUEST_COARSE_CODE);
                }
            }
        });

//        device_sort_type.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Collections.sort(mDevices, new Comparator<DevicePrefer>() {
//                    @Override
//                    public int compare(DevicePrefer o1, DevicePrefer o2) {
//                        String type1 = DeviceUtil.getDeviceType(o1.getDevId());
//                        String type2 = DeviceUtil.getDeviceType(o2.getDevId());
//                        int result = type1.compareToIgnoreCase(type2);
//                        if (result == 0) {
//                            String name1 = o1.getDeviceName();
//                            String name2 = o2.getDeviceName();
//                            result = name1.compareToIgnoreCase(name2);
//                        }
//                        return result;
//                    }
//                });
//                mAdapter.notifyDataSetChanged();
//            }
//        });
//
//        device_sort_name.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Collections.sort(mDevices, new Comparator<DevicePrefer>() {
//                    @Override
//                    public int compare(DevicePrefer o1, DevicePrefer o2) {
//                        String name1 = o1.getDeviceName();
//                        String name2 = o2.getDeviceName();
//                        int result = name1.compareToIgnoreCase(name2);
//                        return result;
//                    }
//                });
//                mAdapter.notifyDataSetChanged();
//            }
//        });
    }

    private void startScanActivity() {
        Intent intent = new Intent(getContext(), ScanActivity.class);
        getActivity().startActivityForResult(intent, ConstVal.SCAN_CODE);
    }

    private void startSort() {
        mSwipeDragHelper.setDragEnable(true);
        mAdapter.setDragEnable(true);
        device_toolbar.setNavigationIcon(R.drawable.ic_clear);
        device_toolbar.getMenu().findItem(R.id.menu_search_ble).setVisible(false);
        device_toolbar.getMenu().findItem(R.id.menu_done).setVisible(true);
//        device_ll.setVisibility(View.VISIBLE);
        device_title.setText(R.string.drag_to_sort);
    }

    private void stopSort() {
        mSwipeDragHelper.setDragEnable(false);
        mAdapter.setDragEnable(false);
        device_toolbar.setNavigationIcon(R.drawable.ic_sort);
        device_toolbar.getMenu().findItem(R.id.menu_search_ble).setVisible(true);
        device_toolbar.getMenu().findItem(R.id.menu_done).setVisible(false);
//        device_ll.setVisibility(View.GONE);
        device_title.setText(R.string.app_name);
    }

    private void sortByType() {
        Collections.sort(mDevices, new Comparator<DevicePrefer>() {
            @Override
            public int compare(DevicePrefer o1, DevicePrefer o2) {
                String type1 = DeviceUtil.getDeviceType(o1.getDevId());
                String type2 = DeviceUtil.getDeviceType(o2.getDevId());
                int result = type1.compareToIgnoreCase(type2);
                if (result == 0) {
                    String name1 = o1.getDeviceName();
                    String name2 = o2.getDeviceName();
                    result = name1.compareToIgnoreCase(name2);
                }
                return result;
            }
        });
        mAdapter.notifyDataSetChanged();
        DevicePrefUtil.setLocalDevices(getContext(), mDevices);
    }

    private void sortByName() {
        Collections.sort(mDevices, new Comparator<DevicePrefer>() {
            @Override
            public int compare(DevicePrefer o1, DevicePrefer o2) {
                String name1 = o1.getDeviceName();
                String name2 = o2.getDeviceName();
                int result = name1.compareToIgnoreCase(name2);
                return result;
            }
        });
        mAdapter.notifyDataSetChanged();
        DevicePrefUtil.setLocalDevices(getContext(), mDevices);
    }

    private void showSortDialog() {
        //        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
        CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme, true);
        builder.setTitle(R.string.sort);
        String[] items = new String[] {getString(R.string.sort_by_type), getString(R.string.sort_by_name), getString(R.string.sort_manual)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        sortByType();
                        break;
                    case 1:
                        sortByName();
                        break;
                    case 2:
                        startSort();
                        break;
                }
            }
        });
        builder.show();
        //        AlertDialog dialog = builder.create();
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.show();
    }

    //    private void showRetrievePasswordDialog(final BaseDevice device) {
    //        final String address = device.getDevicePrefer().getDeviceMac();
    ////        AlertDialog.Builder builder = new AlertDialog.Builder( getContext(), R.style.DialogTheme );
    //        CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme );
    //        View view = LayoutInflater.from( getContext() ).inflate( R.layout.dialog_retrieve_password, null, false );
    //        builder.setTitle( R.string.retrieve_password );
    //        builder.setView(view);
    //        final AlertDialog dialog = builder.show();
    //        final ImageButton ib_copy = view.findViewById(R.id.dialog_retrieve_copy);
    //        final EditText retrieve_key = view.findViewById( R.id.dialog_retrieve_key);
    //        final TextView retrieve_msg = view.findViewById(R.id.dialog_retrieve_msg);
    //        Button btn_cancel = view.findViewById( R.id.dialog_retrieve_cancel );
    //        Button btn_retrieve = view.findViewById( R.id.dialog_retrieve_retrieve);
    //        final CountDownTimer tmr = new CountDownTimer( 4000, 4000 ) {
    //            @Override
    //            public void onTick ( long millisUntilFinished )
    //            {
    //
    //            }
    //
    //            @Override
    //            public void onFinish ()
    //            {
    //                BleManager.getInstance().disconnectDevice( address );
    //                retrieve_msg.setText(R.string.timeout);
    //            }
    //        };
    //        final BleCommunicateListener mListener = new BleCommunicateListener() {
    //            @Override
    //            public void onDataValid ( final String mac )
    //            {
    //                if ( mac.equals( address ) )
    //                {
    //                    BleManager.getInstance().readPassword( mac );
    //                }
    //            }
    //
    //            @Override
    //            public void onDataInvalid ( String mac )
    //            {
    //
    //            }
    //
    //            @Override
    //            public void onReadMfr ( String mac, String s )
    //            {
    //
    //            }
    //
    //            @Override
    //            public void onReadPassword ( String mac, final int psw )
    //            {
    //                if ( mac.equals( address ) )
    //                {
    //                    tmr.cancel();
    //                    getActivity().runOnUiThread( new Runnable() {
    //                        @Override
    //                        public void run ()
    //                        {
    //                            DecimalFormat df = new DecimalFormat("000000");
    //                            retrieve_msg.setText(getString(R.string.retrieve_psw_is) + df.format(psw));
    //                        }
    //                    } );
    //                    BleManager.getInstance().disconnectDevice( mac );
    //                }
    //            }
    //
    //            @Override
    //            public void onDataReceived ( String mac, ArrayList< Byte > list )
    //            {
    //
    //            }
    //        };
    //        ib_copy.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    //                ClipData cd = ClipData.newPlainText("device_info", getDeviceInfo(device));
    //                cm.setPrimaryClip(cd);
    //                Toast.makeText(getContext(), R.string.retrieve_copy_msg, Toast.LENGTH_LONG)
    //                     .show();
    //            }
    //        });
    //        btn_cancel.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                dialog.dismiss();
    //            }
    //        });
    //        btn_retrieve.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                String key = retrieve_key.getText().toString().toLowerCase();
    //                if (TextUtils.isEmpty(key)) {
    //                    retrieve_msg.setText("");
    //                } else {
    //                    if (key.equals(Md5Util.encrypt(address).toLowerCase())) {
    //                        BleManager.getInstance().connectDevice( address );
    //                        tmr.start();
    //                        retrieve_msg.setText("");
    //                    } else {
    //                        retrieve_msg.setText(R.string.retrieve_wrong_key);
    //                    }
    //                }
    //            }
    //        });
    //        dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
    //            @Override
    //            public void onDismiss ( DialogInterface dialog )
    //            {
    //                BleManager.getInstance().disconnectDevice( address );
    //                BleManager.getInstance().removeBleCommunicateListener( mListener );
    //            }
    //        } );
    //
    ////        dialog.setCanceledOnTouchOutside( false );
    ////        dialog.setView( view );
    ////        dialog.show();
    //        BleManager.getInstance().addBleCommunicateListener( mListener );
    //    }
    //
    //    private String getDeviceInfo(BaseDevice device) {
    //        JsonObject jsonObject = new JsonObject();
    //        jsonObject.addProperty("type", DeviceUtil.getDeviceType(device.getDevicePrefer().getDevId()));
    //        jsonObject.addProperty("name", device.getDevicePrefer().getDeviceName());
    //        jsonObject.addProperty("address", device.getDevicePrefer().getDeviceMac());
    //        jsonObject.addProperty("app_version", BuildConfig.VERSION_NAME);
    //        return jsonObject.toString();
    //    }
}
