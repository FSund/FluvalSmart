package com.inledco.OkHttpManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liruya on 2017/5/4.
 */

public abstract class HttpCallback<T> implements Callback
{
    private static final int HTTP_FAILURE_CODE = 100;
    private static final int HTTP_REPONSE_CODE_OK = 200;

    private Gson mGson;
    private Type mType;

    public HttpCallback ()
    {
        mGson = new Gson();
        Type superClass = getClass().getGenericSuperclass();
        if ( superClass instanceof ParameterizedType )
        {
            mType = ( (ParameterizedType) superClass ).getActualTypeArguments()[0];
        }
    }

    @Override
    public void onFailure ( Call call, IOException e )
    {
        onError( HTTP_FAILURE_CODE, null );
    }

    @Override
    public void onResponse ( Call call, Response response ) throws IOException
    {
        if ( response.isSuccessful() )
        {
            String json = response.body().string();
            if ( json == null )
            {
                onError( response.code(), null );
                return;
            }
            if ( mType != null )
            {
                try
                {
                    T result = mGson.fromJson( json, mType );
                    if ( result != null )
                    {
                        onSuccess( result );
                    }
                    else
                    {
                        onError( HTTP_FAILURE_CODE, null );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    onError( response.code(), "Net error." );
                }
            }
        }
        else
        {
            onError( response.code(), "Net error." );
        }
    }

    public abstract void onError( int code, String msg );

    public abstract void onSuccess( T result );
}
