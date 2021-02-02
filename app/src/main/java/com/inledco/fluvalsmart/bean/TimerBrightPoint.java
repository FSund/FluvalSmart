package com.inledco.fluvalsmart.bean;

import java.io.Serializable;

public class TimerBrightPoint implements Serializable {
    private static final long serialVersionUID = 3779611915963944981L;
    private int mHour;
    private int mMinute;
    private byte[] mBrights;

    public TimerBrightPoint() {
    }

    public TimerBrightPoint(int hour, int minute, int chn) {
        mHour = hour;
        mMinute = minute;
        mBrights = new byte[chn];
    }

    public TimerBrightPoint(int hour, int minute, byte[] brights) {
        mHour = hour;
        mMinute = minute;
        mBrights = brights;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("invalid hour");
        }
        mHour = hour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid minute");
        }
        mMinute = minute;
    }

    public byte[] getBrights() {
        return mBrights;
    }

    public void setBrights(byte[] brights) {
        if (brights == null) {
            throw new IllegalArgumentException("brights should not null");
        }
        for (byte val : brights) {
            if (val < 0 || val > 100) {
                throw new IllegalArgumentException("bright shoul be 0~100");
            }
        }
        mBrights = brights;
    }

    public int getTimer() {
        return mHour*60+mMinute;
    }

    public byte[] toArray() {
        byte[] array = new byte[mBrights.length+2];
        array[0] = (byte) mHour;
        array[1] = (byte) mMinute;
        System.arraycopy(mBrights, 0, array, 2, mBrights.length);
        return array;
    }
}
