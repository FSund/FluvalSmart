package com.inledco.fluvalsmart.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class CustomDialog extends AlertDialog {

    private float xPercent = 0.5f;
    private float yPercent = 0.5f;
    private float mCornerRadius = 16;
    private int mBackgroundColor = 0xFFFFFFFF;

    protected CustomDialog(@NonNull Context context) {
        super(context);
    }

    protected CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void show() {
        super.show();
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * xPercent);
        int height = (int) (dm.heightPixels * yPercent);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = width;
        lp.height = height;
        getWindow().setBackgroundDrawable(getBackgroundDrawable());
        getWindow().setAttributes(lp);
    }

    private Drawable getBackgroundDrawable() {
        float outRectr[] = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius};
        RoundRectShape roundRectShape = new RoundRectShape(outRectr, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(mBackgroundColor);
        return shapeDrawable;
    }
}
