package com.liruya.swiperecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

public abstract class SimpleSwipeAdapter<T, VH extends SwipeViewHolder> extends BaseSwipeAdapter<VH> {
    protected List<T> mList;

    public SimpleSwipeAdapter(@NonNull Context context, List<T> list) {
        super(context);
        mList = list;
    }

    protected T getItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
