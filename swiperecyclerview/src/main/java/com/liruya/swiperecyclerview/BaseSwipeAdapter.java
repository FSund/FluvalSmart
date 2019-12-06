package com.liruya.swiperecyclerview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseSwipeAdapter<VH extends SwipeViewHolder> extends RecyclerView.Adapter<VH> {
    private Context mContext;
    private SwipeItemListener mSwipeItemListener;

    public final void setSwipeItemListener(SwipeItemListener listener) {
        mSwipeItemListener = listener;
    }

    public BaseSwipeAdapter(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @LayoutRes int layoutid = getLayoutResID(viewType);
        if (layoutid != 0) {
            View view = LayoutInflater.from(mContext).inflate(layoutid, parent, false);
            if (view instanceof SwipeLayout) {
                return createSwipeViewHolder((SwipeLayout) view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, final int position) {
        bindSwipeViewHolder(holder, position);
        holder.swipeItemView.setOnSwipeClickListener(new SwipeLayout.OnClickListener() {
            @Override
            public boolean onContentLongClick() {
                if (mSwipeItemListener != null) {
                    return mSwipeItemListener.onContentLongClick(holder, position);
                }
                return false;
            }

            @Override
            public void onContentClick() {
                if (mSwipeItemListener != null) {
                    mSwipeItemListener.onContentClick(holder, position);
                }
            }

            @Override
            public void onActionClick(int actionid) {
                if (mSwipeItemListener != null) {
                    mSwipeItemListener.onActionClick(holder, position, actionid);
                }
            }
        });
    }

    protected abstract @LayoutRes int getLayoutResID(int viewType);

    protected abstract VH createSwipeViewHolder(SwipeLayout swipeLayout);

    protected abstract void bindSwipeViewHolder(@NonNull VH holder, final int position);

    protected abstract void onMove(int from, int to);
}
