package com.inledco.fluvalsmart.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inledco.fluvalsmart.prefer.Setting;

public abstract class BaseActivity extends AppCompatActivity
{
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

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
    }

    @Override
    protected void onRestart ()
    {
        super.onRestart();
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
    }

    @Override
    protected void onStop ()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.attachBaseContext(Setting.updateResources(newBase));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    protected abstract void initView();
    protected abstract void initEvent();
    protected abstract void initData();
}
