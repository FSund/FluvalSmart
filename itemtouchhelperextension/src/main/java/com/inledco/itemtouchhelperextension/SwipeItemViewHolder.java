package com.inledco.itemtouchhelperextension;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liruya on 2017/5/23.
 */

public abstract class SwipeItemViewHolder extends RecyclerView.ViewHolder implements Extension
{
    public SwipeItemViewHolder ( View itemView )
    {
        super( itemView );
    }
}
