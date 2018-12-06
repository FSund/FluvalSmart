package com.inledco.fluvalsmart.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class CustomTimePickerDialog extends TimePickerDialog {

    private boolean mResized;
    private float xPercent = 0.72f;

    public CustomTimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, listener, hourOfDay, minute, is24HourView);
        setCanceledOnTouchOutside(false);
    }

    public CustomTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
        if (!mResized) {
            DisplayMetrics dm = getContext().getResources()
                                            .getDisplayMetrics();
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = (int) (dm.widthPixels * xPercent);
            getWindow().setAttributes(lp);
            mResized = true;
        }
    }
}
