package com.inledco.fluvalsmart.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/21.
 */

public class MainPageAdapter extends FragmentStatePagerAdapter
{
    private ArrayList< Fragment > mFrags;

    public MainPageAdapter ( FragmentManager fm, ArrayList< Fragment > frags )
    {
        super( fm );
        mFrags = frags;
    }

    @Override
    public Fragment getItem ( int position )
    {
        return mFrags == null ? null : mFrags.get( position );
    }

    @Override
    public int getCount ()
    {
        return mFrags == null ? 0 : mFrags.size();
    }

}
