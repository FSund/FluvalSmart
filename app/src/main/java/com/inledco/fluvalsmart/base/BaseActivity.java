package com.inledco.fluvalsmart.base;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.inledco.fluvalsmart.prefer.Setting;

public abstract class BaseActivity extends AppCompatActivity
{
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Setting.changeAppLanguage(BaseActivity.this);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res != null) {
            Configuration config = res.getConfiguration();
            if (config != null && config.fontScale != 1) {
                config.fontScale = 1;
            }
        }
        return res;
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

    protected void replaceFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                   .replace(layout, fragment, fragment.getClass().getSimpleName())
                                   .commitAllowingStateLoss();
    }

    protected void addFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .add(layout, fragment, fragment.getClass().getSimpleName())
                                   .commitAllowingStateLoss();
    }

    protected void addFragmentToStack(@IdRes int layout, @NonNull final Fragment fragment) {
        String name = fragment.getClass().getSimpleName();
        getSupportFragmentManager().beginTransaction()
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .add(layout, fragment, name).addToBackStack(name)
                                   .commitAllowingStateLoss();
    }

    protected abstract void initView();
    protected abstract void initEvent();
    protected abstract void initData();
}
