package com.inledco.OkHttpManager;

import java.io.File;

/**
 * Created by liruya on 2017/5/6.
 */

public interface DownloadCallback
{
    void onError ();

    void onProgress ( long total, long current );

    void onSuccess ( File file );
}
