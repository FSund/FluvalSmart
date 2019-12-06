package com.liruya.swiperecyclerview;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;

public interface SwipeItemListener {
    boolean onContentLongClick(RecyclerView.ViewHolder holder, int position);

    void onContentClick(RecyclerView.ViewHolder holder, int position);

    void onActionClick(RecyclerView.ViewHolder holder, int position, @IdRes int actionid);
}
