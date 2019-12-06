package com.inledco.fluvalsmart.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class SwipeItemViewHolder extends RecyclerView.ViewHolder {

    public SwipeItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract int getActionViewWidth();

    public abstract View getContentView();
}
