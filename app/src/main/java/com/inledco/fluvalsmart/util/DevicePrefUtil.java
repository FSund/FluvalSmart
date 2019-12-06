package com.inledco.fluvalsmart.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inledco.fluvalsmart.bean.DevicePrefer;
import com.inledco.fluvalsmart.constant.ConstVal;

import java.util.ArrayList;
import java.util.List;

public class DevicePrefUtil {
    public static List<DevicePrefer> getLocalDevices(Context context) {
        List<DevicePrefer> devices = new ArrayList<>();
        Gson gson = new Gson();
        if (context == null) {
            return devices;
        }
        SharedPreferences sp = context.getSharedPreferences(ConstVal.DEV_PREFER_FILENAME, Context.MODE_PRIVATE);
        if (sp.getAll().size() > 0) {
            for (String key : sp.getAll().keySet()) {
                DevicePrefer prefer = (DevicePrefer) PreferenceUtil.getObjectFromPrefer(context, ConstVal.DEV_PREFER_FILENAME, key);
                devices.add(prefer);
            }
            PreferenceUtil.clear(context, ConstVal.DEV_PREFER_FILENAME);
            for (int i = 0; i < devices.size(); i++) {
                devices.get(i).setIndex(i);
            }

            String json = gson.toJson(devices);
            PreferenceUtil.putString(context, ConstVal.DEVICE_LIST_FILENAME, ConstVal.KEY_DEVICES, json);
        } else {
            String devJson = PreferenceUtil.getString(context, ConstVal.DEVICE_LIST_FILENAME, ConstVal.KEY_DEVICES);
            if (!TextUtils.isEmpty(devJson)) {
                try {
                    List<DevicePrefer> devs = gson.fromJson(devJson, new TypeToken<List<DevicePrefer>>() {}.getType());
                    if (devs != null) {
                        devices.addAll(devs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return devices;
    }

    public static void setLocalDevices(Context context, List<DevicePrefer> devices) {
        if (context == null || devices == null) {
            return;
        }
        if (devices.size() == 0) {
            PreferenceUtil.clear(context, ConstVal.DEVICE_LIST_FILENAME);
            return;
        }
        for (int i = 0; i < devices.size(); i++) {
            devices.get(i).setIndex(i);
        }
        Gson gson = new Gson();
        String json = gson.toJson(devices);
        PreferenceUtil.putString(context, ConstVal.DEVICE_LIST_FILENAME, ConstVal.KEY_DEVICES, json);
    }

    public static void renameLocalDevice(Context context, DevicePrefer device) {
        if (context == null || device == null) {
            return;
        }
        List<DevicePrefer> devices = new ArrayList<>();
        String devJson = PreferenceUtil.getString(context, ConstVal.DEVICE_LIST_FILENAME, ConstVal.KEY_DEVICES);
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(devJson)) {
            try {
                List<DevicePrefer> devs = gson.fromJson(devJson, new TypeToken<List<DevicePrefer>>() {}.getType());
                if (devs != null) {
                    devices.addAll(devs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (DevicePrefer dev : devices) {
            if (TextUtils.equals(dev.getDeviceMac(), device.getDeviceMac())) {
                dev.setDeviceName(device.getDeviceName());
            }
        }
        String json = gson.toJson(devices);
        PreferenceUtil.putString(context, ConstVal.DEVICE_LIST_FILENAME, ConstVal.KEY_DEVICES, json);
    }
}
