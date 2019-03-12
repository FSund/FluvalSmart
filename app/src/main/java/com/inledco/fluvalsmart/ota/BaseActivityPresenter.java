package com.inledco.fluvalsmart.ota;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class BaseActivityPresenter<T extends AppCompatActivity>
{
    private WeakReference<T> mView;

    public BaseActivityPresenter(T t)
    {
        mView = new WeakReference<>( t );
    }

    protected T getView()
    {
        return mView.get();
    }

    protected boolean isViewExist()
    {
        return mView.get() != null;
    }

    protected Context getContext()
    {
        if ( mView.get() != null )
        {
            return mView.get();
        }
        return null;
    }

    protected String getString( @StringRes int resid )
    {
        if ( getContext() != null )
        {
            return getContext().getString( resid );
        }
        return null;
    }

    protected void runOnUiThread(Runnable runnable)
    {
        if ( mView.get() != null )
        {
            mView.get().runOnUiThread( runnable );
        }
    }
}
