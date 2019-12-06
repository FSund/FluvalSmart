package com.liruya.swiperecyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class ItemSwipeDragHelper<T extends BaseSwipeAdapter> {
    // swipe
    private OnSwipeItemTouchListener mSwipeItemTouchListener;

    // drag
    private final ItemTouchHelper.SimpleCallback mItemTouchCallback;

    private final ItemTouchHelper mItemTouchHelper;

    private boolean mDragEnable;

    private RecyclerView mRecyclerView;

    private T mAdapter;

    public ItemSwipeDragHelper() {
        mSwipeItemTouchListener = new OnSwipeItemTouchListener();

        mItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                if (mAdapter != null) {
                    int from = viewHolder.getAdapterPosition();
                    int to = viewHolder1.getAdapterPosition();
                    mAdapter.onMove(from, to);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setAlpha(0.9f);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (viewHolder != null) {
                    viewHolder.itemView.setAlpha(1.0f);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return (mAdapter != null) && mDragEnable;
            }
        };
        mItemTouchHelper = new ItemTouchHelper(mItemTouchCallback);
    }

    public boolean isDragEnable() {
        return (mAdapter != null) && mDragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        mDragEnable = dragEnable;
        if (mRecyclerView != null) {
            if (mDragEnable) {
                mRecyclerView.removeOnItemTouchListener(mSwipeItemTouchListener);
                mItemTouchHelper.attachToRecyclerView(mRecyclerView);
            } else {
                mRecyclerView.addOnItemTouchListener(mSwipeItemTouchListener);
                mItemTouchHelper.attachToRecyclerView(null);
            }
        }
    }

    public void closeOpened() {
        mSwipeItemTouchListener.close();
    }

    public void attachRecyclerView(RecyclerView recyclerView, T adapter) {
        if (recyclerView == null || adapter == null) {
            return;
        }
        mDragEnable = false;
        mRecyclerView = recyclerView;
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(mSwipeItemTouchListener);
        mItemTouchHelper.attachToRecyclerView(null);
    }

    public void dettachRecyclerView() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnItemTouchListener(mSwipeItemTouchListener);
            mItemTouchHelper.attachToRecyclerView(null);
        }
    }
}
