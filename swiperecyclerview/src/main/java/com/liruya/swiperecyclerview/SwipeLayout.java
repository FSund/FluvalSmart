package com.liruya.swiperecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class SwipeLayout extends RelativeLayout {
    private static final String TAG = "SwipeLayout";

    public static final int SWIPE_MODE_DISALED = 0;
    public static final int SWIPE_MODE_COVER = 1;
    public static final int SWIPE_MODE_SCROLL = 2;

    /**
     * swipeMode: 0-Disabled  1-Cover  2-Scroll
     */
    private int mSwipeMode;

    /**
     * contentLayoutResID: create custom layout as content
     */
    private @LayoutRes int mContentLayoutResID;

    /**
     * actionLayoutResID: create custom layout as action
     */
    private @LayoutRes int mActionLayoutResID;

    private View mContentView;
    private View mActionView;

//    private Scroller mScroller;

    private OnClickListener mOnClickListener;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        mSwipeMode = a.getInt(R.styleable.SwipeLayout_swipeMode, SWIPE_MODE_DISALED);
        mContentLayoutResID = a.getResourceId(R.styleable.SwipeLayout_contentLayout, 0);
        mActionLayoutResID = a.getResourceId(R.styleable.SwipeLayout_actionLayout, 0);
        a.recycle();

        initView(context);
    }

    private void initView(@NonNull Context context) {
//        mScroller = new Scroller(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        //加载contentView
        mContentView = inflater.inflate(mContentLayoutResID, this, false);
        //如果contentView为空,视图异常 退出
        if (mContentView == null) {
            throw new RuntimeException("inflate contentView failed or invalid contentLayoutRes");
        }
        //触摸事件需要使能点击
        mContentView.setClickable(true);
        LayoutParams lpc = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpc.addRule(ALIGN_PARENT_LEFT);
        lpc.addRule(ALIGN_PARENT_RIGHT);
        mContentView.setLayoutParams(lpc);

        mContentView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnClickListener != null) {
                    return mOnClickListener.onContentLongClick();
                }
                return false;
            }
        });
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onContentClick();
                }
            }
        });

        //Swipe模式为Cover或Scroll 加载actionView 否则不加载actionView
        if (mSwipeMode == SWIPE_MODE_COVER || mSwipeMode == SWIPE_MODE_SCROLL) {
            mActionView = inflater.inflate(mActionLayoutResID, this, false);
        }
        //如果actionView为空,Swipe模式Disabled 只加载contentView
        if (mActionView == null) {
            addView(mContentView);
        } else {
            if (mContentView.getId() == View.NO_ID) {
                throw new RuntimeException("contentView has no id");
            }
            if (mActionView.getId() == View.NO_ID) {
                throw new RuntimeException("actionView has no id");
            }
            //触摸事件需要使能点击
            mActionView.setClickable(true);
            LayoutParams lpa = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            //Cover模式 contentView 覆盖在 actionView上方
            if (mSwipeMode == SWIPE_MODE_COVER) {
//                lpa.addRule(mSwipeDirection == SWIPE_DIRECTION_RIGHT ? ALIGN_PARENT_LEFT : ALIGN_PARENT_RIGHT);
                lpa.addRule(ALIGN_PARENT_RIGHT);
                lpa.addRule(ALIGN_TOP, mContentView.getId());
                lpa.addRule(ALIGN_BOTTOM, mContentView.getId());
                lpa.setMargins(0, 0, 0, 0);
                mActionView.setLayoutParams(lpa);
                //先添加actionView 后添加contentView
                addView(mActionView);
                addView(mContentView);
            }   //Scoll模式  actionView toRightOf/toLeftOf contetnView,左右滚动时显示actionView
            else if (mSwipeMode == SWIPE_MODE_SCROLL) {
//                lpa.addRule(mSwipeDirection == SWIPE_DIRECTION_RIGHT ? LEFT_OF : RIGHT_OF, mContentView.getId());
                lpa.addRule(RIGHT_OF, mContentView.getId());
                lpa.addRule(ALIGN_TOP, mContentView.getId());
                lpa.addRule(ALIGN_BOTTOM, mContentView.getId());
                //marginLeft/marginRight不能为0 否则无法显示
                lpa.setMargins(1, 0, 1, 0);
                mActionView.setLayoutParams(lpa);
                //先添加contentView 后添加actionView
                addView(mContentView);
                addView(mActionView);
            }

            if (mActionView instanceof ViewGroup) {
                ViewGroup actonGroup = (ViewGroup) mActionView;
                for (int i = 0; i < actonGroup.getChildCount(); i++) {
                    actonGroup.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnClickListener != null) {
                                mOnClickListener.onActionClick(v.getId());
                            }
                        }
                    });
                }
            } else {
                mActionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onActionClick(v.getId());
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public int getContentViewWidth() {
        return mContentView == null ? 0 : mContentView.getWidth();
    }

    public int getActionViewWidth() {
        return mActionView == null ? 0 : mActionView.getWidth();
    }

    public View getContentView() {
        return mContentView;
    }

    public View getActionView() {
        return mActionView;
    }

    public int getSwipeMode() {
        return mSwipeMode;
    }

    public boolean canSwipe() {
        return (getActionViewWidth() > 0);
    }

    public boolean isTouchDownOnAction(float x) {
        return x > getWidth() - getActionViewWidth();
    }

    public void close() {
        if (mContentView != null) {
            if (mSwipeMode == SWIPE_MODE_COVER) {
                mContentView.setTranslationX(0);
                invalidate();
            } else if (mSwipeMode == SWIPE_MODE_SCROLL) {
//                mScroller.startScroll(getScrollX(), 0, 0 - getScrollX(), 0);
                setScrollX(0);
                invalidate();
            }
        }
    }

//    @Override
//    public void computeScroll() {
//        super.computeScroll();
//        if (mScroller.computeScrollOffset()) {
//            scrollTo(mScroller.getCurrX(), 0);
//            invalidate();
//        }
//    }

    public void setOnSwipeClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public interface OnClickListener {
        boolean onContentLongClick();

        void onContentClick();

        void onActionClick(@IdRes int actionid);
    }
}
