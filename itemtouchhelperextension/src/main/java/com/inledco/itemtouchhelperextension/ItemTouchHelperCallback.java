package com.inledco.itemtouchhelperextension;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by liruya on 2017/5/23.
 */

public class ItemTouchHelperCallback extends ItemTouchHelperExtension.SimpleCallback {

    private boolean mSwipeEnable;
    private boolean mDragEnable;

    public ItemTouchHelperCallback() {
        super(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
        mSwipeEnable = true;
        mDragEnable = false;
    }

    public void setSwipeEnable(boolean swipeEnable) {
        mSwipeEnable = swipeEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        mDragEnable = dragEnable;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return mSwipeEnable;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mDragEnable;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof SwipeItemViewHolder && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            SwipeItemViewHolder holder = (SwipeItemViewHolder) viewHolder;
            if (dX < 0 - holder.getActionWidth()) {
                dX = 0 - holder.getActionWidth();
            }
            holder.getContentView().setTranslationX(dX);
            return;
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
