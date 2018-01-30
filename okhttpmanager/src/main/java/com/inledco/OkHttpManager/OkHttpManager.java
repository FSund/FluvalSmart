package com.inledco.OkHttpManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpManager
{
    private static final String TAG = "OkHttpManager";

    public static final MediaType JSON = MediaType.parse( "application/json; charset=utf-8" );

    private static final OkHttpClient mHttpClient = new OkHttpClient();

    /**
     * @param url
     * @param headers new Headers.Builder().add( key, value );
     * @param callback
     */
    public static void get ( String url, Headers headers, HttpCallback callback )
    {
        Request request;
        if ( headers == null )
        {
            request = new Request.Builder().url( url )
                                           .build();
        }
        else
        {
            request = new Request.Builder().url( url )
                                           .headers( headers )
                                           .build();
        }
        mHttpClient.newCall( request )
                   .enqueue( callback );
    }

    /**
     * post request
     *
     * @param url post address
     * @param headers new Headers.Builder().add( key, value );
     * @param body new  FormBody.Builder().add( key, value );
     * @param callback
     */
    public static void post ( String url, Headers headers, RequestBody body, HttpCallback callback )
    {
        Request request;
        if ( headers == null )
        {
            request = new Request.Builder().url( url )
                                           .post( body )
                                           .build();
        }
        else
        {
            request = new Request.Builder().url( url )
                                           .headers( headers )
                                           .post( body )
                                           .build();
        }
        mHttpClient.newCall( request )
                   .enqueue( callback );
    }

    /**
     * @param url
     * @param headers new Headers.Builder().add( key, value );
     * @param json
     * @param callback
     */
    public static void post ( String url, Headers headers, String json, HttpCallback callback )
    {
        RequestBody body = RequestBody.create( JSON, json );
        Request request;
        if ( headers == null )
        {
            request = new Request.Builder().url( url )
                                           .post( body )
                                           .build();
        }
        else
        {
            request = new Request.Builder().url( url )
                                           .headers( headers )
                                           .post( body )
                                           .build();
        }

        mHttpClient.newCall( request )
                   .enqueue( callback );
    }

    /**
     * @param url
     * @param file
     * @param callback
     */
    public static void download ( String url, final File file, final DownloadCallback callback )
    {
        if ( file.exists() )
        {
            if ( callback != null )
            {
                callback.onSuccess( file );
            }
            return;
        }
        Request request = new Request.Builder().url( url )
                                               .build();
        mHttpClient.newCall( request )
                   .enqueue( new Callback()
                   {
                       @Override
                       public void onFailure ( Call call, IOException e )
                       {
                           if ( callback != null )
                           {
                               callback.onError();
                           }
                       }

                       @Override
                       public void onResponse ( Call call, Response response ) throws IOException
                       {
                           if ( response.isSuccessful() )
                           {
                               InputStream is = null;
                               FileOutputStream fos = null;
                               byte[] buf = new byte[1024];
                               int len;
                               try
                               {
                                   long total = response.body()
                                                        .contentLength();
                                   long current = 0;
                                   is = response.body()
                                                .byteStream();
                                   fos = new FileOutputStream( file );
                                   while ( ( len = is.read( buf ) ) != -1 )
                                   {
                                       current += len;
                                       fos.write( buf, 0, len );
                                       if ( callback != null )
                                       {
                                           callback.onProgress( total, current );
                                       }
                                   }
                                   fos.flush();
                                   if ( callback != null )
                                   {
                                       callback.onSuccess( file );
                                   }
                               }
                               catch ( Exception e )
                               {
                                   if ( callback != null )
                                   {
                                       callback.onError();
                                   }
                               }
                               finally
                               {
                                   try
                                   {
                                       if ( is != null )
                                       {
                                           is.close();
                                       }
                                       if ( fos != null )
                                       {
                                           fos.close();
                                       }
                                   }
                                   catch ( Exception e )
                                   {

                                   }
                               }
                           }
                       }
                   } );
    }
}
