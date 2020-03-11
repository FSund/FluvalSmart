package com.inledco.fluvalsmart.prefer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.inledco.fluvalsmart.R;

import java.util.Locale;

/**
 * Created by Administrator on 2016/10/19. 保存在本地的参数数据类
 */

public class Setting {
    public static final String KEY_AUTO_TURNON_BLE = "AUTO_TURNON_BLE";
    public static final String KEY_EXIT_TURNOFF_BLE = "EXIT_TURNOFF_BLE";
    public static final String KEY_COUNTRY_LANGUAGE_SELECTED = "IS_COUNTRY_LANGUAGE_SELECTED";
    public static final String KEY_COUNTRY = "COUNTRY";
    public static final String KEY_LANGUAGE = "LANGUAGE";
    public static final String KEY_SCAN_TIP = "scan_tip";
    public static final String KEY_UPGRADE_TIP = "upgrade_tip";
    public static final String KEY_EMAIL_TIP = "email_tip";

//    private static final String KEY_TESTMODE = "testmode";

//    public static boolean showRssi() {
//        return true;
//    }
//
//    public static boolean forceUpdate() {
//        return false;
//    }

//    public static boolean isTestMode(@NonNull Context context) {
//        return true;
//    }
//
//    public static void setTestMode(@NonNull Context context, boolean b) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sp.edit()
//                                            .putBoolean(KEY_TESTMODE, b);
//        editor.commit();
//    }

    public static boolean isAutoTurnonBle(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_AUTO_TURNON_BLE, false);
        }
        return false;
    }

    public static void setAutoTurnonBle(@NonNull Context context, boolean b) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putBoolean(KEY_AUTO_TURNON_BLE, b);
            editor.commit();
        }
    }

    public static boolean isExitTurnoffBle(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_EXIT_TURNOFF_BLE, false);
        }
        return false;
    }

    public static void setExitTurnoffBle(@NonNull Context context, boolean b) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putBoolean(KEY_EXIT_TURNOFF_BLE, b);
            editor.commit();
        }
    }

    public static boolean hasSelectCountryLanguage(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_COUNTRY_LANGUAGE_SELECTED, false);
        }
        return false;
    }

    public static void setSelectCountryLanguage(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putBoolean(KEY_COUNTRY_LANGUAGE_SELECTED, true);
            editor.commit();
        }
    }

    public static boolean hasScanTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_SCAN_TIP, false);
        }
        return false;
    }

    public static void setScanTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putBoolean(KEY_SCAN_TIP, true);
            editor.commit();
        }
    }

    public static boolean hasUpgradeTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_UPGRADE_TIP, false);
        }
        return false;
    }

    public static void setUpgradeTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putBoolean(KEY_UPGRADE_TIP, true);
            editor.commit();
        }
    }

    public static String getCountry(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getString(KEY_COUNTRY, "");
        }
        return "";
    }

    public static void setCountry(@NonNull Context context, String c) {
        if (context != null && !TextUtils.isEmpty(c)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putString(KEY_COUNTRY, c);
            editor.commit();
        }
    }

    public static String getLanguage(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getString(KEY_LANGUAGE, "");
        }
        return "";
    }

    public static void setLanguage(@NonNull Context context, String c) {
        if (context != null && !TextUtils.isEmpty(c)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit()
                                                .putString(KEY_LANGUAGE, c);
            editor.commit();
        }
    }

    public static Locale getLocale(@NonNull Context context) {
        if (context == null) {
            return null;
        }
        String lang = getLanguage(context);
        Locale[] locales = new Locale[] {null,
                                         Locale.ENGLISH,
                                         Locale.GERMANY,
                                         Locale.FRENCH,
                                         new Locale("es", "ES"),
                                         Locale.SIMPLIFIED_CHINESE,
                                         Locale.JAPANESE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = LocaleList.getDefault();
            if (localeList != null && localeList.size() > 0) {
                locales[0] = localeList.get(0);
            }
        } else {
            locales[0] = Locale.getDefault();
        }
        String[] keys = context.getResources().getStringArray(R.array.language_keys);
        if (keys.length != locales.length) {
            return locales[0];
        }
        for (int i = 0; i < keys.length; i++) {
            if (TextUtils.equals(lang, keys[i])) {
                return locales[i];
            }
        }
        return locales[0];
    }

    @TargetApi (Build.VERSION_CODES.N)
    public static Context updateResources(@NonNull Context context) {
        if (context == null) {
            return null;
        }
        Resources resources = context.getResources();
        Locale locale = getLocale(context);// getSetLocale方法是获取新设置的语言

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    public static void changeAppLanguage(@NonNull Context context) {
        if (context == null) {
            return;
        }
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        Locale locale = getLocale(context);
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        }
        res.updateConfiguration(config, dm);
    }

    public static boolean hasShowEmailTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getBoolean(KEY_EMAIL_TIP, false);
        }
        return false;
    }

    public static void setShowEmailTip(@NonNull Context context) {
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(KEY_EMAIL_TIP, true);
            editor.apply();
        }
    }
}
