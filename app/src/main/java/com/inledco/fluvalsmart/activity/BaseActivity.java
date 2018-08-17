package com.inledco.fluvalsmart.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inledco.blemanager.LogUtil;
import com.inledco.fluvalsmart.prefer.Setting;

public abstract class BaseActivity extends AppCompatActivity
{
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        LogUtil.d( TAG, "onCreate: " );

        Setting.changeAppLanguage( BaseActivity.this );
    }

    @Override
    public void onConfigurationChanged ( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
    }

    @Override
    protected void onStart ()
    {
        super.onStart();
        LogUtil.d( TAG, "onStart: " );
    }

    @Override
    protected void onRestart ()
    {
        super.onRestart();
        LogUtil.d( TAG, "onRestart: " );
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        LogUtil.d( TAG, "onResume: " );
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        LogUtil.d( TAG, "onPause: " );
    }

    @Override
    protected void onStop ()
    {
        super.onStop();
        LogUtil.d( TAG, "onStop: " );
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        LogUtil.d( TAG, "onDestroy: " );
    }

    protected abstract void initView();
    protected abstract void initEvent();
    protected abstract void initData();
}
