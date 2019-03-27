package com.inledco.fluvalsmart.light;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.gigamole.library.ArcProgressStackView;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.bean.Channel;
import com.inledco.fluvalsmart.bean.Light;
import com.inledco.fluvalsmart.bean.LightManual;
import com.inledco.fluvalsmart.util.CommUtil;
import com.inledco.fluvalsmart.util.DeviceUtil;
import com.inledco.fluvalsmart.viewmodel.LightViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class LightManualFragment extends BaseFragment {
    private CheckableImageButton lightmanualonoff;
    private ListView lightmanuallv;
    private ArcProgressStackView apsv_p1;
    private ArcProgressStackView apsv_p2;
    private ArcProgressStackView apsv_p3;
    private ArcProgressStackView apsv_p4;

    private LightManual mLightManual;
    private static String mAddress;
    private short devid;
    private ArrayList<Channel> mChannels;
    private SliderAdapter mSliderAdapter;

    private LightViewModel mLightViewModel;
    private Light mLight;

    private byte[] p1Brt;
    private byte[] p2Brt;
    private byte[] p3Brt;
    private byte[] p4Brt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light_manual, container, false);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initView(View view) {
        lightmanuallv = view.findViewById(R.id.light_manual_lv);
        lightmanualonoff = view.findViewById(R.id.light_manual_onoff);
        apsv_p1 = view.findViewById(R.id.manual_custom_p1);
        apsv_p2 = view.findViewById(R.id.manual_custom_p2);
        apsv_p3 = view.findViewById(R.id.manual_custom_p3);
        apsv_p4 = view.findViewById(R.id.manual_custom_p4);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity())
                                            .get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        if (mLight != null) {
            devid = mLight.getDevicePrefer()
                          .getDevId();
            mAddress = mLight.getDevicePrefer()
                             .getDeviceMac();
            mLightManual = mLight.getLightManual();

            ArrayList<ArcProgressStackView.Model> models1 = new ArrayList<>();
            ArrayList<ArcProgressStackView.Model> models2 = new ArrayList<>();
            ArrayList<ArcProgressStackView.Model> models3 = new ArrayList<>();
            ArrayList<ArcProgressStackView.Model> models4 = new ArrayList<>();
            Channel[] chns = DeviceUtil.getLightChannel(getContext(), devid);
            for (int i = 0; i < chns.length; i++) {
                models1.add(new ArcProgressStackView.Model("", 0, 0xFF9E9E9E, chns[i].getColor()));
                models2.add(new ArcProgressStackView.Model("", 0, 0xFF9E9E9E, chns[i].getColor()));
                models3.add(new ArcProgressStackView.Model("", 0, 0xFF9E9E9E, chns[i].getColor()));
                models4.add(new ArcProgressStackView.Model("", 0, 0xFF9E9E9E, chns[i].getColor()));
            }
            apsv_p1.setDrawWidthDimension(models1.size() * 6);
            apsv_p1.setModels(models1);
            apsv_p2.setDrawWidthDimension(models1.size() * 6);
            apsv_p2.setModels(models2);
            apsv_p3.setDrawWidthDimension(models1.size() * 6);
            apsv_p3.setModels(models3);
            apsv_p4.setDrawWidthDimension(models1.size() * 6);
            apsv_p4.setModels(models4);
            refreshData();
        }

        mLightViewModel.observe(this, new Observer<Light>() {
            @Override
            public void onChanged(@Nullable Light light) {
                if (light != null) {
                    mLightManual = light.getLightManual();
                    refreshData();
                }
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    private void refreshData() {
        if (mLightManual == null) {
            return;
        }
        mChannels = new ArrayList<>();
        Channel[] chns = DeviceUtil.getLightChannel(getContext(), devid);
        for (int i = 0; i < mLightManual.getChnValues().length; i++) {
            chns[i].setValue(mLightManual.getChnValues()[i]);
            mChannels.add(chns[i]);
        }
        mSliderAdapter = new SliderAdapter(getContext(), mAddress, devid, mChannels);
        lightmanuallv.setAdapter(mSliderAdapter);
        p1Brt = mLightManual.getCustomP1Values();
        p2Brt = mLightManual.getCustomP2Values();
        p3Brt = mLightManual.getCustomP3Values();
        p4Brt = mLightManual.getCustomP4Values();
        for (int i = 0; i < mSliderAdapter.getCount(); i++) {
            apsv_p1.getModels()
                   .get(i)
                   .setProgress(p1Brt[i]);
            apsv_p2.getModels()
                   .get(i)
                   .setProgress(p2Brt[i]);
            apsv_p3.getModels()
                   .get(i)
                   .setProgress(p3Brt[i]);
            apsv_p4.getModels()
                   .get(i)
                   .setProgress(p4Brt[i]);
        }
        apsv_p1.invalidate();
        apsv_p2.invalidate();
        apsv_p3.invalidate();
        apsv_p4.invalidate();
        lightmanualonoff.setChecked(mLightManual.isOn());
    }

    @Override
    protected void initEvent() {
        lightmanualonoff.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (lightmanualonoff.isChecked()) {
                    CommUtil.turnOffLed(mAddress);
                }
                else {
                    CommUtil.turnOnLed(mAddress);
                }
            }
        });

        apsv_p1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for (int i = 0; i < mSliderAdapter.getCount(); i++) {
                    Channel channel = (Channel) mSliderAdapter.getItem(i);
                    p1Brt[i] = (byte) (channel.getValue() / 10);
                    apsv_p1.getModels()
                           .get(i)
                           .setProgress(p1Brt[i]);
                }
                apsv_p1.invalidate();
                CommUtil.setLedCustom(mAddress, (byte) 0x00);
                return true;
            }
        });
        apsv_p2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for (int i = 0; i < mSliderAdapter.getCount(); i++) {
                    Channel channel = (Channel) mSliderAdapter.getItem(i);
                    p2Brt[i] = (byte) (channel.getValue() / 10);
                    apsv_p2.getModels()
                           .get(i)
                           .setProgress(p2Brt[i]);
                }
                apsv_p2.invalidate();
                CommUtil.setLedCustom(mAddress, (byte) 0x01);
                return true;
            }
        });
        apsv_p3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for (int i = 0; i < mSliderAdapter.getCount(); i++) {
                    Channel channel = (Channel) mSliderAdapter.getItem(i);
                    p3Brt[i] = (byte) (channel.getValue() / 10);
                    apsv_p3.getModels()
                           .get(i)
                           .setProgress(p3Brt[i]);
                }
                apsv_p3.invalidate();
                CommUtil.setLedCustom(mAddress, (byte) 0x02);
                return true;
            }
        });
        apsv_p4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for (int i = 0; i < mSliderAdapter.getCount(); i++) {
                    Channel channel = (Channel) mSliderAdapter.getItem(i);
                    p4Brt[i] = (byte) (channel.getValue() / 10);
                    apsv_p4.getModels()
                           .get(i)
                           .setProgress(p4Brt[i]);
                }
                apsv_p4.invalidate();
                CommUtil.setLedCustom(mAddress, (byte) 0x03);
                return true;
            }
        });
        apsv_p1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                short[] values = new short[p1Brt.length];
                for (int i = 0; i < p1Brt.length; i++) {
                    values[i] = (short) ((p1Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed(mAddress, values);
                getDeviceData();
            }
        });
        apsv_p2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                short[] values = new short[p2Brt.length];
                for (int i = 0; i < p2Brt.length; i++) {
                    values[i] = (short) ((p2Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed(mAddress, values);
                getDeviceData();
            }
        });
        apsv_p3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                short[] values = new short[p3Brt.length];
                for (int i = 0; i < p3Brt.length; i++) {
                    values[i] = (short) ((p3Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed(mAddress, values);
                getDeviceData();
            }
        });
        apsv_p4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                short[] values = new short[p4Brt.length];
                for (int i = 0; i < p4Brt.length; i++) {
                    values[i] = (short) ((p4Brt[i] & 0xFF) * 10);
                }
                CommUtil.setLed(mAddress, values);
                getDeviceData();
            }
        });
    }

    private void getDeviceData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CommUtil.readDevice(mAddress);
            }
        }, 64);
    }
}
