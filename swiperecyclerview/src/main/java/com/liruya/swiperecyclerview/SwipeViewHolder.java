package com.liruya.swiperecyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SwipeViewHolder extends RecyclerView.ViewHolder {
    public final SwipeLayout swipeItemView;
    public SwipeViewHolder(@NonNull SwipeLayout view) {
        super(view);
        swipeItemView = view;
    }

    public void close() {
        swipeItemView.close();
    }

    public boolean isTouchDownOnAction(float x) {
        return swipeItemView.isTouchDownOnAction(x);
    }

    protected final View getContentView() {
        return swipeItemView.getContentView();
    }

    protected final View getActionView() {
        return swipeItemView.getActionView();
    }

    protected final int getActionViewWidth() {
        return swipeItemView.getActionViewWidth();
    }

    protected final int getSwipeMode() {
        return swipeItemView.getSwipeMode();
    }
}
