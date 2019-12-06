package com.inledco.fluvalsmart.impl;

import android.support.annotation.IdRes;

/**
 * Created by liruya on 2017/5/23.
 */

public interface SwipeItemListener {
    boolean onLongClick(int postion);

    void onClickContent(int position);

    void onClickAction(@IdRes int id, int position);
}
