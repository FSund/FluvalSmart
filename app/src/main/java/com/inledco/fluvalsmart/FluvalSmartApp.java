package com.inledco.fluvalsmart;

import android.app.Application;
import android.content.res.Configuration;

public class FluvalSmartApp extends Application {
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        Setting.changeAppLanguage(getApplicationContext());
    }
}
