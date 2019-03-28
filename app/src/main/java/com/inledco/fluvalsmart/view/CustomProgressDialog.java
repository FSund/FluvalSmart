package com.inledco.fluvalsmart.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class CustomProgressDialog extends ProgressDialog {

    private boolean mResized;
    private float xPercent = 0.8f;

    public CustomProgressDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(false);
    }

    public CustomProgressDialog(Context context, boolean cancelOnTouchOutside) {
        super(context);
        setCanceledOnTouchOutside(cancelOnTouchOutside);
    }

    public CustomProgressDialog(Context context, int theme, boolean cancelOnTouchOutside) {
        super(context, theme);
        setCanceledOnTouchOutside(cancelOnTouchOutside);
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
