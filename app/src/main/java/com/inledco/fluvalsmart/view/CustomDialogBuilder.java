package com.inledco.fluvalsmart.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class CustomDialogBuilder extends AlertDialog.Builder {
    private boolean mCancenOnTouchOutside = false;
    private float xPercent = 0.72f;

    public CustomDialogBuilder(@NonNull Context context) {
        super(context);
    }

    public CustomDialogBuilder(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public CustomDialogBuilder(@NonNull Context context, boolean cancenOnTouchOutside) {
        super(context);
        mCancenOnTouchOutside = cancenOnTouchOutside;
    }

    public CustomDialogBuilder(@NonNull Context context, int themeResId, boolean cancenOnTouchOutside) {
        super(context, themeResId);
        mCancenOnTouchOutside = cancenOnTouchOutside;
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.setCanceledOnTouchOutside(mCancenOnTouchOutside);
        return dialog;
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        dialog.setCanceledOnTouchOutside(mCancenOnTouchOutside);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * xPercent);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = width;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

//    private Drawable getBackgroundDrawable() {
//        float outRectr[] = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius};
//        RoundRectShape roundRectShape = new RoundRectShape(outRectr, null, null);
//        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
//        shapeDrawable.getPaint().setColor(mBackgroundColor);
//        return shapeDrawable;
//    }
}
