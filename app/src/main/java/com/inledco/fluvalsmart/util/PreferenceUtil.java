package com.inledco.fluvalsmart.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 参数保存
 * Created by liruya on 2016/8/25.
 */
public class PreferenceUtil
{
    /**
     * 储存对象到SharedPreference
     * @param context
     * @param fileName    文件名
     * @param object        对象
     * @param key
     */
    public static void setObjectToPrefer( Context context, String fileName, Object object, String key)
    {
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        if ( object == null )
        {
            SharedPreferences.Editor editor = sp.edit().remove( key );
            editor.apply();
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try
        {
            oos = new ObjectOutputStream( baos );
            oos.writeObject( object );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
        String objectStr = new String( Base64.encode( baos.toByteArray(), Base64.DEFAULT ));
        try
        {
            baos.close();
            oos.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = sp.edit().putString( key, objectStr );
        editor.apply();
    }

    /**
     * 从指定文件获取对象
     * @param context
     * @param fileName   文件名
     * @param key
     * @return
     */
    public static Object getObjectFromPrefer(Context context, String fileName, String key)
    {
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        String objectStr = sp.getString( key, "" );
        if ( objectStr == null || objectStr.equals( "" ) )
        {
            return  null;
        }
        byte[] objBytes = Base64.decode( objectStr.getBytes(), Base64.DEFAULT );
        ByteArrayInputStream bais = new ByteArrayInputStream( objBytes );
        try
        {
            ObjectInputStream ois = new ObjectInputStream( bais );
            Object object = ois.readObject();
            bais.close();
            ois.close();
            return object;
        } catch ( IOException e )
        {
            e.printStackTrace();
        } catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从指定文件获取所有对象
     * @param fileName
     * @return
     */
    public static <T> ArrayList<T> getAllObjectFromPrefer ( Context context, String fileName )
    {
        ArrayList<T> objects = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        for ( String key : sp.getAll().keySet() )
        {
            Object object = getObjectFromPrefer( context, fileName, key );
            objects.add( (T) object );
        }
        return objects;
    }

    /**
     * 从指定文件获取所有对象
     * @param fileName
     * @return
     */
    public static <T> HashMap<String, T> getAllObjectMapFromPrefer ( Context context, String fileName )
    {
        HashMap<String, T> objects = new HashMap<>();
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        for ( String key : sp.getAll()
                                       .keySet() )
        {
            Object object = getObjectFromPrefer( context, fileName, key );
            objects.put( key, (T) object );
        }
        return objects;
    }

    /**
     * 从指定文件获取所有键值
     * @param fileName
     * @return
     */
    public static Set<String> getAllKeyFromPrefer ( Context context, String fileName )
    {
        if ( context == null || TextUtils.isEmpty( fileName ) )
        {
            return null;
        }
        return context.getSharedPreferences( fileName, Context.MODE_PRIVATE ).getAll().keySet();
    }

    /**
     * 删除文件中储存的对象
     * @param context
     * @param fileName
     * @param key
     */
    public static void deleteObjectFromPrefer( Context context, String fileName, String key)
    {
        if ( context == null || TextUtils.isEmpty( fileName ) || TextUtils.isEmpty( key ) )
        {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        if ( sp.contains( key ) )
        {
            SharedPreferences.Editor editor = sp.edit().remove( key );
            editor.apply();
        }
    }

    public static void saveByteArray(Context context, String fileName, byte[] array, String key )
    {
        if ( context == null || TextUtils.isEmpty( fileName ) || array == null || array.length == 0 || TextUtils.isEmpty( key ) )
        {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        JSONArray jsonArray = new JSONArray();
        for ( byte b : array )
        {
            jsonArray.put( (int) b );
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString( key, jsonArray.toString() );
        editor.apply();
    }

    public static byte[] readByteArray(Context context, String fileName, String key)
    {
        if ( context == null || TextUtils.isEmpty( fileName ) || TextUtils.isEmpty( key ) )
        {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences( fileName, Context.MODE_PRIVATE );
        try
        {
            JSONArray jsonArray = new JSONArray(sp.getString( key, "[]" ));
            byte[] array = new byte[jsonArray.length()];
            for ( int i = 0; i < array.length; i++ )
            {
                array[i] = (byte) jsonArray.getInt( i );
            }
            return array;
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
