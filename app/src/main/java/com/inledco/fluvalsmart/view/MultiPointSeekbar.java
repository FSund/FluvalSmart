package com.inledco.fluvalsmart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.Pools;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.util.SizeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiPointSeekbar extends View
{
    private static final String TAG = "MultiPointSeekbar";

    private final int POINT_COUNT_DEFAULT = 1;
    private final int INTERVAL_DEFAULT = 1;
    private final int POINT_COUNT_MIN = 1;
    private final int POINT_COUNT_MAX = 10;
    private final int HINT_MODE_HIDE = 0;
    private final int HINT_MODE_MOVING = 1;
    private final int HINT_MODE_SELECTED = 2;

    private final int POINT_NONE = -1;

    private float mDpUnitSize;
    private float mSpUnitSize;

    private boolean mAttached;

    private int mMaxWidth;
    private int mMinWidth;
    private int mMaxHeight;
    private int mMinHeight;

    private int mLineWidth;

    private int mInterval;
    private int mLineColor;
    private int mThumbColor;
    private int mSelectedThumbColor;
    private Drawable mThumb;
    private Drawable mSelectedThumb;
//    private int[] mProgress;
    private List<Integer> mProgress;
    private String mMaxLengthHint;
    private int mHintBackgroundColor;
    private int mHintPadding;
    private int mHintColor;
    private int mHintSize;
    private int mHintMode;
    //    private ColorStateList mThumbTintList;
    //    private PorterDuff.Mode mThumbMode;
    //    private boolean mHasThumbTint;
    //    private boolean mHasThumbTintMode;
    private int mMax;
    private int mMin;

    private float mTouchDownX;


    private int mSelectedPoint = POINT_NONE;

    private boolean mDragEnabled;
    private boolean mDragging;

    boolean mIsUserSeekable = true;

    private final int NO_ALPHA = 0xFF;
    private float mDisabledAlpha;

    private int mScaledTouchSlop;

    private int mHintWidth;
    private int mHintHeight;
    private int mTextHeight;

    private Paint mLinePaint;
    private Paint mThumbPaint;
    private Paint mHintPaint;

    private List<Integer> mTouchPoints;

    private List<RefreshData> mRefreshDataList = new ArrayList<>();
    private boolean mRefreshIsPosted;
    private RefreshDataRunnable mRefreshDataRunnable;
    private long mUiThreadId;

    private GetTextImpl mGetTextImpl;
    private Listener mListener;

    public MultiPointSeekbar(Context context)
    {
        this(context, null);
    }

    public MultiPointSeekbar(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MultiPointSeekbar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mDpUnitSize = SizeUtil.dp2px(context, 1);
        mSpUnitSize = SizeUtil.sp2px(context, 1);
        mUiThreadId = Thread.currentThread().getId();
        final TypedArray a = context.obtainStyledAttributes(attrs,
                                                             R.styleable.MultiPointSeekbar);
        mInterval = a.getInt(R.styleable.MultiPointSeekbar_interval, INTERVAL_DEFAULT);
        mProgress = new ArrayList<>(POINT_COUNT_MAX);
        mThumbColor = a.getColor(R.styleable.MultiPointSeekbar_thumbColor, 0xFFFFFFFF);
        mSelectedThumbColor = a.getColor(R.styleable.MultiPointSeekbar_selectedThumbColor, getResources().getColor(R.color.colorAccent));
        mMinWidth = (int) a.getDimension(R.styleable.MultiPointSeekbar_minWidth, mDpUnitSize * 64);
        mMinHeight = (int) a.getDimension(R.styleable.MultiPointSeekbar_minHeight, mDpUnitSize * 4);
        mMaxWidth = (int) a.getDimension(R.styleable.MultiPointSeekbar_maxWidth, mDpUnitSize * 1024);
        mMaxHeight = (int) a.getDimension(R.styleable.MultiPointSeekbar_maxHeight, mDpUnitSize * 32);
        mLineColor = a.getColor(R.styleable.MultiPointSeekbar_lineColor, 0xFFFFFFFF);
        mMax = a.getInt(R.styleable.MultiPointSeekbar_maximum, 100);
        mMin = a.getInt(R.styleable.MultiPointSeekbar_minimum, 0);
        mThumb = a.getDrawable(R.styleable.MultiPointSeekbar_thumbDrawable);
        mSelectedThumb = a.getDrawable(R.styleable.MultiPointSeekbar_selectedThumDrawable);
        mHintBackgroundColor = a.getColor(R.styleable.MultiPointSeekbar_hintBackgroundColor, getResources().getColor(R.color.colorAccent));
        mMaxLengthHint = a.getString(R.styleable.MultiPointSeekbar_maxLengthHint);
        mHintColor = a.getColor(R.styleable.MultiPointSeekbar_hintColor, 0xFFFFFFFF);
        mHintSize = (int) a.getDimension(R.styleable.MultiPointSeekbar_hintSize, 12 * mSpUnitSize);
        mHintPadding = (int) a.getDimension(R.styleable.MultiPointSeekbar_hintPadding, 4 * mDpUnitSize);
        mHintMode = a.getInt(R.styleable.MultiPointSeekbar_hintMode, HINT_MODE_SELECTED);

        a.recycle();
        if (mMin >= mMax)
        {
            mMin = 0;
            mMax = 100;
        }
        if (mInterval <= 0 || mInterval >= mMax - mMin)
        {
            mInterval = INTERVAL_DEFAULT;
        }
        mTouchPoints = new ArrayList<>();
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (mRefreshDataList != null) {
            synchronized (this) {
                final int count = mRefreshDataList.size();
                for (int i = 0; i < count; i++) {
                    final RefreshData rd = mRefreshDataList.get(i);
                    doRefreshProgress(rd.index, rd.progress, rd.fromUser);
                    rd.recycle();
                }
                mRefreshDataList.clear();
            }
        }
        mAttached = true;
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (mRefreshDataRunnable != null) {
            removeCallbacks(mRefreshDataRunnable);
            mRefreshIsPosted = false;
        }
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = Math.max(mMinWidth, Math.min(mMaxWidth, widthSize));
        int height = Math.max(mMinHeight, Math.min(mMaxHeight, heightSize));
        height = Math.min(height, mHintHeight + mHintPadding + getThumbHeight());
        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else if (widthMode == MeasureSpec.AT_MOST)
        {
        }
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        }
        else if (heightMode == MeasureSpec.AT_MOST)
        {
            height = mHintHeight + mHintPadding + getThumbHeight() + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        drawLine(canvas, w, h);
        for (int i = 0; i < mProgress.size(); i++) {
            if (mSelectedPoint == i) {
                continue;
            }
            drawThumb(canvas, w, h, i);
        }
        drawThumb(canvas, w, h, mSelectedPoint);
        drawHint(canvas, w, h, mSelectedPoint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int point = getTouchedPoint(x, y);
                if (point >= 0 && point < mProgress.size()) {
                    mTouchDownX = x;
                    mDragEnabled = true;
                    if (mSelectedPoint != point) {
                        mSelectedPoint = point;
                        if (mListener != null) {
                            mListener.onPointSelected(mSelectedPoint);
                        }
                    }
                    invalidate();
                    return true;
                } else {
                    mDragEnabled = false;
                }
                mDragging = false;
                break;
//            case MotionEvent.ACTION_DOWN:
//                if (mSelectedPoint == SELECTED_POINT_NONE)
//                {
//                    mDragEnabled = false;
//                    mDragging = false;
//                    mTouchPoints.clear();
//                    for (int i = 0; i < mProgress.size(); i++)
//                    {
//                        if (isPointTouched(i, x, y))
//                        {
//                            mTouchPoints.add(i);
//                        }
//                    }
//                    if (mTouchPoints.size() == 1)
//                    {
//                        mSelectedPoint = mTouchPoints.get(0);
//                        mTouchDownX = x;
//                        if (mListener != null)
//                        {
//                            mListener.onPointSelected(mSelectedPoint);
//                        }
//                        invalidate();
//                        return true;
//                    }
//                    else if (mTouchPoints.size() > 1 && mTouchPoints.size() < mProgress.size())
//                    {
//                        if (mListener != null)
//                        {
//                            Collections.sort(mTouchPoints, new Comparator< Integer >() {
//                                @Override
//                                public int compare(Integer o1, Integer o2)
//                                {
//                                    if (mProgress[o1] < mProgress[o2])
//                                    {
//                                        return -1;
//                                    }
//                                    if (mProgress[o1] > mProgress[o2])
//                                    {
//                                        return 1;
//                                    }
//                                    return 0;
//                                }
//                            });
//                            mListener.onMultiPointTouched(mTouchPoints);
//                        }
//                    }
//                }
//                else if (mSelectedPoint >= 0 && mSelectedPoint < mProgress.size())
//                {
//                    if (isPointTouched(mSelectedPoint, x, y))
//                    {
//                        mTouchDownX = x;
//                        mDragEnabled = true;
//                        mDragging = false;
//                        return true;
//                    }
//                    else
//                    {
//                        mDragEnabled = false;
//                        mDragging = false;
//                    }
//                }
//                else
//                {
//                    mDragEnabled = false;
//                    mDragging = false;
//                }
//                break;
            case MotionEvent.ACTION_MOVE:
                if (mSelectedPoint >= 0 && mSelectedPoint < mProgress.size() && mDragEnabled) {
                    if (mDragging) {
                        pointTouchEvent(event);
                    } else {
                        if (Math.abs(mTouchDownX - event.getX()) >= mScaledTouchSlop) {
                            mDragging = true;
                            if (mListener != null) {
                                mListener.onStartPointTouch(mSelectedPoint);
                            }
                        }
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mSelectedPoint >= 0 && mSelectedPoint < mProgress.size()) {
                    if (mDragEnabled && mDragging) {
                        int oldIndex = mSelectedPoint;
                        int progress = convertProgress(mProgress.get(mSelectedPoint));
                        mProgress.remove(mSelectedPoint);
                        mSelectedPoint = addPoint(progress);
                        if (mListener != null) {
//                            mProgress.set(mSelectedPoint, convertProgress(mProgress.get(mSelectedPoint)));
                            mListener.onStopPointTouch(oldIndex, mSelectedPoint);
                        }
                    }
                }
                mDragEnabled = false;
                mDragging = false;
                break;
            case MotionEvent.ACTION_UP:
                if (mSelectedPoint >= 0 && mSelectedPoint < mProgress.size()) {
                    if (mDragEnabled && mDragging) {
                        pointTouchEvent(event);
                        int oldIndex = mSelectedPoint;
                        int progress = convertProgress(mProgress.get(mSelectedPoint));
                        mProgress.remove(mSelectedPoint);
                        mSelectedPoint = addPoint(progress);
                        if (mListener != null) {
//                            mProgress.set(mSelectedPoint, convertProgress(mProgress.get(mSelectedPoint)));
                            mListener.onStopPointTouch(oldIndex, mSelectedPoint);
                        }
                    }
                }
                mDragEnabled = false;
                mDragging = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    private void moveSelectedProgress(int progress) {
        if (progress < mMin || progress > mMax) {
            throw new IllegalArgumentException(String.format("progress out of range [%d, %d]", mMin, mMax));
        }

    }

    private void pointTouchEvent(MotionEvent event) {
        if (mSelectedPoint < 0 && mSelectedPoint >= mProgress.size()) {
            return;
        }
        final int x = Math.round(event.getX());
        final int y = Math.round(event.getY());
        final int thumbWidth = getThumbWidth();
        final int extra = thumbWidth < mHintWidth ? mHintWidth : thumbWidth;
        final float lineWidth = getWidth() - getPaddingLeft() - getPaddingRight() - extra;
        final int left = getPaddingLeft() + extra/2;
        final int right = getWidth() - getPaddingRight() - extra/2;
        float scale;
        if (x <= left) {
            scale = 0.0f;
        } else if (x >= right) {
            scale = 1.0f;
        } else {
            scale = (x - left)/lineWidth;
        }
        final int progress = Math.round(mMin + scale * (mMax - mMin));
        if (progress == mProgress.get(mSelectedPoint)) {
            return;
        }
        mProgress.set(mSelectedPoint, progress);
        if (mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(mSelectedPoint, progress, true);
        } else {
            if (mRefreshDataRunnable == null) {
                mRefreshDataRunnable = new RefreshDataRunnable();
            }
            final RefreshData rd = RefreshData.obtain(mSelectedPoint, progress, true);
            mRefreshDataList.add(rd);
            if (mAttached && !mRefreshIsPosted) {
                post(mRefreshDataRunnable);
                mRefreshIsPosted = true;
            }
        }
    }

    private void doRefreshProgress(int idx, int progress, boolean fromUser)
    {
        if (mListener != null)
        {
            mListener.onPointProgressChanged(idx, progress, fromUser);
        }
        invalidate();
    }

    private static class RefreshData
    {
        private static int POOL_MAX = 8;
        private static final Pools.SynchronizedPool<RefreshData> sPool = new Pools.SynchronizedPool<>(POOL_MAX);

        private int index;
        private int progress;
        private boolean fromUser;

        public static RefreshData obtain(int index, int progress, boolean fromUser)
        {
            RefreshData rd = sPool.acquire();
            if (rd == null)
            {
                rd = new RefreshData();
            }

            rd.index = index;
            rd.progress = progress;
            rd.fromUser = fromUser;
            return rd;
        }

        public void recycle()
        {
            sPool.release(this);
        }
    }

    private class RefreshDataRunnable implements Runnable
    {
        @Override
        public void run()
        {
            synchronized (MultiPointSeekbar.this)
            {
                final int count = mRefreshDataList.size();
                for (int i = 0; i < count; i++)
                {
                    final RefreshData rd = mRefreshDataList.get(i);
                    doRefreshProgress(rd.index, rd.progress, rd.fromUser);
                    rd.recycle();
                }
                mRefreshDataList.clear();
                mRefreshIsPosted = false;
            }
        }
    }

    private boolean isPointTouched(int idx, float x, float y) {
        if (idx < 0 || idx >= mProgress.size()) {
            return false;
        }
        final int space = 5;
        int width = getThumbWidth();
        int height = getThumbHeight();
        int extra = width < mHintWidth ? mHintWidth : width;
        int lineWidth = getWidth() - getPaddingLeft() - getPaddingRight() - extra;
//        if (width < 100)
//        {
//            width = 100;
//
//        }
//        if (height < 100)
//        {
//            height = 100;
//        }
        int left = (int) (getPaddingLeft() + (extra - width)/ 2 + lineWidth * (mProgress.get(idx) - mMin) / (float) (mMax - mMin));
        int right = left + width;
        int top = getPaddingTop() + mHintHeight + mHintPadding;
        int bottom = top + height;
        return x >= left - space && x <= right + space && y >= top - space && y <= bottom + space;
    }

    private int getTouchedPoint(float x, float y)
    {
        int point = POINT_NONE;
        final int space = 20;
        int width = getThumbWidth();
        int height = getThumbHeight();
        int rx = width/2+space;
        int ry = height/2+space;
        int extra = width < mHintWidth ? mHintWidth : width;
        int lineWidth = getWidth() - getPaddingLeft() - getPaddingRight() - extra;
        int cy = getPaddingTop() + mHintHeight + mHintPadding + height/2;
//        int top = getPaddingTop() + mHintHeight + mHintPadding;
//        int bottom = top + height;
        int min = rx*rx + ry*ry;
        for (int i = 0; i < mProgress.size(); i++)
        {
            int progress = mProgress.get(i);
            int cx = (int) (getPaddingLeft() + extra/2 + lineWidth * (progress- mMin) / (float) (mMax - mMin));
            int d = (int) ((x - cx) * (x - cx) + (y - cy) * (y - cy));
            if (d < min) {
//                if (i == mSelectedPoint)
//                {
//                    return i;
//                }
                min = d;
                point = i;
            }
        }
        return point;
    }

    public void setGetTextImpl(GetTextImpl impl)
    {
        mGetTextImpl = impl;
    }

    public void setListener(Listener listener)
    {
        mListener = listener;
    }

    private void init()
    {
        mLinePaint = new Paint();
        mThumbPaint = new Paint();
        mHintPaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.FILL);
        mHintPaint.setTextAlign(Paint.Align.CENTER);
        mHintPaint.setStyle(Paint.Style.FILL);

        refreshHintSize();
    }

    private void refreshHintSize()
    {
        mHintPaint.setTextSize(mHintSize);
        if (TextUtils.isEmpty(mMaxLengthHint))
        {
            mMaxLengthHint = "00:00";
        }
        Rect rect = new Rect();
        mHintPaint.getTextBounds(mMaxLengthHint, 0, mMaxLengthHint.length(), rect);
        mHintWidth = (int) (mDpUnitSize * 8 + rect.width());
        mHintHeight = (int) (mHintWidth * 1.2f);
        mTextHeight = rect.height();
    }

    private int getThumbHeight()
    {
        if (mThumb != null)
        {
            return mThumb.getIntrinsicHeight();
        }
        return (int) (mDpUnitSize * 24);
    }

    private int getThumbWidth()
    {
        if (mThumb != null)
        {
            return mThumb.getIntrinsicWidth();
        }
        return (int) (mDpUnitSize * 24);
    }

    private void drawTimer(int cx, int cy, int r, Canvas canvas)
    {
        mThumbPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, r, mThumbPaint);
        Path path = new Path();
        path.moveTo(cx, cy - r/2);
        path.lineTo(cx, cy);
        path.lineTo(cx + r/2, cy);
        mThumbPaint.setColor(0xFF000000);
        canvas.drawPath(path, mThumbPaint);
    }

    private void drawLine(Canvas canvas, int w, int h)
    {
        int thumbWidth = getThumbWidth();
        int extra = thumbWidth < mHintWidth ? mHintWidth : thumbWidth;
        int lineHeight = (int) (mDpUnitSize * 4);
        int left = getPaddingLeft() + extra/2;
        int top = getPaddingTop() + mHintHeight + mHintPadding + (h - getPaddingTop() - getPaddingBottom() - mHintHeight - mHintPadding - lineHeight) / 2;
        int right = w - getPaddingRight() - extra/2;
        int bottom = top + lineHeight;
//        canvas.drawRoundRect(left, top, right, bottom, lineHeight/2, lineHeight/2, mLinePaint);
        canvas.drawRect(left, top, right, bottom, mLinePaint);
    }

    private void drawThumb(Canvas canvas, int w, int h, int idx) {
        if (idx < 0 || idx >= mProgress.size()) {
            throw new  IllegalArgumentException("idx out of range");
        }
        int progress = mProgress.get(idx);
        if (progress < mMin || progress > mMax) {
            throw new RuntimeException(String.format("progress out of range [%d, %d]", mMin, mMax));
        }
        Drawable thumb = mSelectedPoint == idx ? mSelectedThumb : mThumb;
        int thumbHeight = getThumbHeight();
        int thumbWidth = getThumbWidth();
        int extra = thumbWidth < mHintWidth ? mHintWidth : thumbWidth;
        float lineWidth = w - getPaddingLeft() - getPaddingRight() - extra;
        int left = (int) (getPaddingLeft() + lineWidth * (progress/mInterval) / ((mMax - mMin)/mInterval) + (extra - thumbWidth) / 2);
        int top = getPaddingTop() + mHintHeight + mHintPadding + (h - getPaddingTop() - getPaddingBottom() - mHintHeight - mHintPadding - thumbHeight)/2;
        int right = left + thumbWidth;
        int bottom = top + thumbHeight;
        if (thumb != null) {
            thumb.setBounds(left, top, right, bottom);
            final int saveCount = canvas.save();
            thumb.draw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            float cx = getPaddingLeft() + extra/2 + lineWidth * progress / (float) (mMax - mMin);
            float cy = getPaddingTop() + mHintHeight + mHintPadding + (h - getPaddingTop() - getPaddingBottom() - mHintHeight - mHintPadding)/2;
            mThumbPaint.setColor(mSelectedPoint == idx ? mSelectedThumbColor : mThumbColor);
            mThumbPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, mDpUnitSize * 12, mThumbPaint);
            mThumbPaint.setColor(0xFF000000);
            mThumbPaint.setStrokeWidth(4);
            canvas.drawLine(cx-2, cy, cx + mDpUnitSize*9, cy, mThumbPaint);
            canvas.drawLine(cx, cy+2, cx, cy - mDpUnitSize*9, mThumbPaint);
        }
    }

    private int convertProgress(int progress) {
        int m = (progress-mMin)%mInterval;
        progress -= m;
        return progress;
    }

    private String getHintText(int progress) {
        int value = convertProgress(progress);
        if (mGetTextImpl != null) {
            return mGetTextImpl.getText(value);
        }
        return "" + (value - mMin)*100/(mMax-mMin) + "%";
    }

    private void drawHint(Canvas canvas, int w, int h, int idx) {
        if (idx < 0 || idx >= mProgress.size()) {
            throw new IllegalArgumentException("idx out of range");
        }
        if (mSelectedPoint != idx || mHintMode == HINT_MODE_HIDE) {
            return;
        }
        int progress = mProgress.get(idx);
        if (progress < mMin || progress > mMax) {
            throw new RuntimeException(String.format("progress out of range [%d, %d]", mMin, mMax));
        }
        if (mHintMode == HINT_MODE_SELECTED || mDragging) {
            int thumbWidth = getThumbWidth();
            int extra = thumbWidth < mHintWidth ? mHintWidth : thumbWidth;
            int lineWidth = w - getPaddingLeft() - getPaddingRight() - extra;
            int left = (int) (getPaddingLeft() + extra / 2 + lineWidth * progress / (float) (mMax - mMin) - mHintWidth / 2);
            int top = getPaddingTop();
            int right = left + mHintWidth;
            int bottom = top + mHintHeight;
            Path path = new Path();
            RectF rectF = new RectF(left, top, right, top + mHintWidth);
            path.arcTo(rectF, 150, 240);
            path.lineTo((left + right) / 2, bottom);
            path.close();
            mHintPaint.setColor(mHintBackgroundColor);
            canvas.drawPath(path, mHintPaint);

            String text = getHintText(progress);
            if (text.length() > mMaxLengthHint.length()) {
                text = text.substring(0, mMaxLengthHint.length());
            }
            mHintPaint.setColor(mHintColor);
            canvas.drawText(text, (left + right) / 2, (top + bottom) / 2 + mTextHeight / 4, mHintPaint);
        }
    }

    public boolean isSelectedPointCoincide()
    {
        if (mSelectedPoint < 0 || mSelectedPoint >= mProgress.size())
        {
            return false;
        }
        for (int i = 0; i < mProgress.size(); i++)
        {
            if (i == mSelectedPoint)
            {
                continue;
            }
            if (mProgress.get(i) == mProgress.get(mSelectedPoint))
            {
                return true;
            }
        }
        return false;
    }

    public void setThumb(Drawable thumb)
    {
        final boolean needUpdate;
        if (mThumb != null && thumb != mThumb)
        {
            mThumb.setCallback(null);
            needUpdate = true;
        }
        else
        {
            needUpdate = false;
        }

        if (thumb != null)
        {
            thumb.setCallback(this);

            if (needUpdate && thumb.getIntrinsicWidth() != mThumb.getIntrinsicWidth()
                 && thumb.getIntrinsicHeight() != mThumb.getIntrinsicHeight())
            {
                requestLayout();
            }
        }

        mThumb = thumb;
        invalidate();

        if (needUpdate)
        {

            if (thumb != null && thumb.isStateful())
            {
                int[] state = getDrawableState();
                thumb.setState(state);
            }
        }
    }

    public Drawable getThumb()
    {
        return mThumb;
    }

    public synchronized int getPointCount()
    {
        return mProgress.size();
    }

    public synchronized int addPoint(int progress) {
        if (progress < mMin || progress > mMax || mProgress.size() >= POINT_COUNT_MAX) {
            return POINT_NONE;
        }
        int idx = 0;
        for (int i = 0; i < mProgress.size(); i++) {
            if (progress > mProgress.get(i)) {
                idx++;
            } else {
                break;
            }
        }
        mProgress.add(idx, progress);
        invalidate();
        return idx;
    }

    public synchronized void removePoint(int idx) {
        if (idx < 0 || idx >= mProgress.size()) {
            return;
        }
        mProgress.remove(idx);
        mSelectedPoint = POINT_NONE;
        invalidate();
    }

    public synchronized void setInterval(int interval)
    {
        if (interval <= 0 || interval >= mMax - mMin || ((mMax-mMin)%interval != 0))
        {
            return;
        }
        mInterval = interval;
    }

    public synchronized int getInterval()
    {
        return mInterval;
    }

    public synchronized int getLineColor()
    {
        return mLineColor;
    }

    public synchronized void setLineColor(int lineColor)
    {
        mLineColor = lineColor;
        invalidate();
    }

    public synchronized int getThumbColor()
    {
        return mThumbColor;
    }

    public synchronized void setThumbColor(int thumbColor)
    {
        mThumbColor = thumbColor;
        invalidate();
    }

    public synchronized int getSelectedThumbColor()
    {
        return mSelectedThumbColor;
    }

    public synchronized void setSelectedThumbColor(int selectedThumbColor)
    {
        mSelectedThumbColor = selectedThumbColor;
        invalidate();
    }

    public List<Integer> getProgress() {
        return mProgress;
    }

    public int getProgressByIndex(int idx) {
        if (idx < 0 || idx >= mProgress.size()) {
            throw new IllegalArgumentException("idx out of range");
        }
        return mProgress.get(idx);
    }

    public synchronized void setProgress(int[] progress) {
        if (progress == null || progress.length == 0 || progress.length > POINT_COUNT_MAX) {
            return;
        }
        for (int i = 0; i < progress.length; i++) {
            if (progress[i] < mMin || progress[i] > mMax) {
                return;
            }
        }
        Arrays.sort(progress);
        mProgress.clear();
        for (int val : progress) {
            mProgress.add(val);
        }
        invalidate();
    }

    public synchronized int setProgress(int idx, int progress) {
        if (idx < 0 || idx >= mProgress.size()) {
            throw new IllegalArgumentException("idx out of range");
        }
        if (progress < mMin || progress > mMax) {
            throw new IllegalArgumentException(String.format("progress out of range [%d:%d]", mMin, mMax));
        }
        if (mProgress.get(idx) != progress) {
            int newidx = 0;
            for (int i = 0; i < mProgress.size(); i++) {
                if (progress < mProgress.get(i)) {
                    newidx = i;
                } else {
                    newidx++;
                }
            }
            int prev = mMin;
            int next = mMax;
            if (idx > 0) {
                prev = mProgress.get(idx);
            }
            if (idx < mProgress.size() - 1) {
                next = mProgress.get(mProgress.size()-1);
            }
            if (progress < prev) {

            } else if (progress > next) {

            } else {

            }
            if (progress >= prev && progress < next) {
                mProgress.set(idx, progress);
            }
            invalidate();
            if (mListener != null) {
                mListener.onPointProgressChanged(idx, progress, false);
            }
        }
        return idx;
    }

    public String getMaxLengthHint()
    {
        return mMaxLengthHint;
    }

    public void setMaxLengthHint(String maxLengthHint)
    {
        mMaxLengthHint = maxLengthHint;
        refreshHintSize();
        invalidate();
    }

    public int getHintBackgroundColor()
    {
        return mHintBackgroundColor;
    }

    public void setHintBackgroundColor(int hintBackgroundColor)
    {
        mHintBackgroundColor = hintBackgroundColor;
        invalidate();
    }

    public int getHintPadding()
    {
        return mHintPadding;
    }

    public void setHintPadding(int hintPadding)
    {
        mHintPadding = hintPadding;
        invalidate();
    }

    public int getHintColor()
    {
        return mHintColor;
    }

    public void setHintColor(int hintColor)
    {
        mHintColor = hintColor;
        invalidate();
    }

    public int getHintSize()
    {
        return mHintSize;
    }

    public void setHintSize(int hintSize)
    {
        mHintSize = hintSize;
        invalidate();
    }

    public int getHintMode()
    {
        return mHintMode;
    }

    public void setHintMode(int hintMode)
    {
        mHintMode = hintMode;
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        if (max <= mMin) {
            throw new IllegalArgumentException("max must > min");
        }
        mMax = max;
        invalidate();
    }

    public int getMin() {
        return mMin;
    }

    public void setMin(int min) {
        if (min >= mMax) {
            throw new IllegalArgumentException("min must < max>");
        }
        mMin = min;
        invalidate();
    }

    public int getSelectedPoint() {
        return mSelectedPoint;
    }

    public void setSelectedPoint(int selectedPoint) {
        if (selectedPoint < 0 || selectedPoint >= mProgress.size()) {
            throw new IllegalArgumentException("selectedPoint out of range");
        }
        if (mSelectedPoint != selectedPoint) {
            mSelectedPoint = selectedPoint;
            invalidate();
            if (mListener != null) {
                mListener.onPointSelected(selectedPoint);
            }
        }
    }

    public void clearSelectedPoint() {
        mSelectedPoint = POINT_NONE;
        invalidate();
    }

    private void setThumbPos(int w, Drawable thumb, float scale, int offset)
    {
        int available = w - getPaddingLeft() - getPaddingRight();
        final int thumbWidth = thumb.getIntrinsicWidth();
        final int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbWidth;
        final int thumbPos = (int) (scale * available + 0.5f);

        final int top;
        final int bottom;
        if (offset == Integer.MIN_VALUE)
        {
            final Rect oldBounds = thumb.getBounds();
            top = oldBounds.top;
            bottom = oldBounds.bottom;
        }
        else
        {
            top = offset;
            bottom = offset + thumbHeight;
        }

        final int left = thumbPos;
        final int right = left + thumbWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Drawable background = getBackground();
            if (background != null)
            {
                final int offsetY = getPaddingTop();
            }
        }
        thumb.setBounds(left, top, right, bottom);
    }

    public interface GetTextImpl {
        String getText(int progress);
    }

    public interface Listener {
        void onPointCountChanged(int pointCount);

        void onPointSelected(int index);

//        void onMultiPointTouched(List<Integer> points);

        void onStartPointTouch(int index);

        void onStopPointTouch(int oldIndex, int newIndex);

        void onPointProgressChanged(int index, int progress, boolean fromUser);
    }
}
