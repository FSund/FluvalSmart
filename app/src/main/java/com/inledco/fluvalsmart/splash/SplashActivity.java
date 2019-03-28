package com.inledco.fluvalsmart.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;
import com.inledco.fluvalsmart.main.MainActivity;
import com.inledco.fluvalsmart.prefer.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private ImageView iv_logo;
    private ImageView iv_logo_center;
    private TextView tv_country;
    private TextView tv_language;
    private WheelPicker wp_country;
    private WheelPicker wp_language;
    private Button btn_enter;
    private CountDownTimer mCountDownTimer;

    List<String> countryList;
    List<String> languageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    protected void initView() {
        iv_logo = findViewById(R.id.iv_logo);
        iv_logo_center = findViewById(R.id.iv_logo_center);
        tv_country = findViewById(R.id.tv_country);
        tv_language = findViewById(R.id.tv_language);
        wp_country = findViewById(R.id.wp_country);
        wp_language = findViewById(R.id.wp_language);
        btn_enter = findViewById(R.id.btn_enter);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        //        Setting.initSetting( this );
        boolean hasSelect = Setting.hasSelectCountryLanguage(this);
        iv_logo.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        iv_logo_center.setVisibility(hasSelect ? View.VISIBLE : View.GONE);
        tv_country.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        tv_language.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        wp_country.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        wp_language.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        btn_enter.setVisibility(hasSelect ? View.GONE : View.VISIBLE);
        if (!hasSelect) {
            String[] counties = getResources().getStringArray(R.array.countries);
            countryList = Arrays.asList(counties);
            languageList = new ArrayList<>();
            languageList.add(getString(R.string.mode_auto));
            languageList.add(getString(R.string.setting_lang_english));
            languageList.add(getString(R.string.setting_lang_germany));
            languageList.add(getString(R.string.setting_lang_french));
            languageList.add(getString(R.string.setting_lang_spanish));
            languageList.add(getString(R.string.setting_lang_chinese));
            wp_country.setData(countryList);
            wp_language.setData(languageList);
            btn_enter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] ll = new String[]{Setting.KEY_LANGUAGE_AUTO,
                                                     Setting.KEY_LANGUAGE_ENGLISH,
                                                     Setting.KEY_LANGUAGE_GERMANY,
                                                     Setting.KEY_LANGUAGE_FRENCH,
                                                     Setting.KEY_LANGUAGE_SPANISH,
                                                     Setting.KEY_LANGUAGE_CHINESE};
                    SharedPreferences defaultSet = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
                    SharedPreferences.Editor editor = defaultSet.edit();
                    Setting.setCountry(SplashActivity.this, countryList.get(wp_country.getCurrentItemPosition()));
                    Setting.setLanguage(SplashActivity.this, ll[wp_language.getCurrentItemPosition()]);
                    Setting.setSelectCountryLanguage(SplashActivity.this);
                    Setting.changeAppLanguage(SplashActivity.this);
                    Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }
        else {
            mCountDownTimer = new CountDownTimer(1500, 1500) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            };
            mCountDownTimer.start();
        }
    }
}
