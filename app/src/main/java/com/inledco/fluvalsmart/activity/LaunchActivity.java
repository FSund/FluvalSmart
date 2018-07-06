package com.inledco.fluvalsmart.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.inledco.blemanager.BleManager;
import com.inledco.blemanager.BleStateListener;
import com.inledco.blemanager.LogUtil;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.prefer.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchActivity extends BaseActivity
{
    private ImageView iv_logo;
    private ImageView iv_logo_center;
    private TextView tv_country;
    private TextView tv_language;
    private WheelPicker wp_country;
    private WheelPicker wp_language;
    private Button btn_enter;
    private BleStateListener mBleStateListener;
    private CountDownTimer mCountDownTimer;

    List< String > countryList;
    List< String > languageList;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_launch );

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        BleManager.getInstance()
                  .getResultForBluetoothEnable( requestCode, resultCode );
    }

    @Override
    protected void initView ()
    {
        iv_logo = findViewById( R.id.iv_logo );
        iv_logo_center = findViewById( R.id.iv_logo_center );
        tv_country = findViewById( R.id.tv_country );
        tv_language = findViewById( R.id.tv_language );
        wp_country = findViewById( R.id.wp_country );
        wp_language = findViewById( R.id.wp_language );
        btn_enter = findViewById( R.id.btn_enter );
    }

    @Override
    protected void initEvent ()
    {

    }

    @Override
    protected void initData ()
    {
        Setting.initSetting( this );
        LogUtil.d( TAG, Setting.mHasSelect + ", " + Setting.mCountry + ", " + Setting.mLang );
        iv_logo.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        iv_logo_center.setVisibility( Setting.mHasSelect ? View.VISIBLE : View.GONE );
        tv_country.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        tv_language.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        wp_country.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        wp_language.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        btn_enter.setVisibility( Setting.mHasSelect ? View.GONE : View.VISIBLE );
        if ( !Setting.mHasSelect )
        {
            String[] counties = getResources().getStringArray( R.array.countries );
            countryList = Arrays.asList( counties );
            languageList = new ArrayList<>();
            languageList.add( getString( R.string.mode_auto ) );
            languageList.add( getString( R.string.setting_lang_english ) );
            languageList.add( getString( R.string.setting_lang_germany ) );
            languageList.add( getString( R.string.setting_lang_french ) );
            languageList.add( getString( R.string.setting_lang_spanish ) );
            languageList.add( getString( R.string.setting_lang_chinese ) );
            wp_country.setData( countryList );
            wp_language.setData( languageList );
            btn_enter.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick ( View v )
                {
                    final String[] ll = new String[]{ Setting.LANGUAGE_AUTO,
                                                      Setting.LANGUAGE_ENGLISH,
                                                      Setting.LANGUAGE_GERMANY,
                                                      Setting.LANGUAGE_FRENCH,
                                                      Setting.LANGUAGE_SPANISH,
                                                      Setting.LANGUAGE_CHINESE };
                    SharedPreferences defaultSet = PreferenceManager.getDefaultSharedPreferences( LaunchActivity.this );
                    SharedPreferences.Editor editor = defaultSet.edit();
                    Setting.mCountry = countryList.get( wp_country.getCurrentItemPosition() );
                    Setting.mLang = ll[wp_language.getCurrentItemPosition()];
                    Setting.mHasSelect = true;
                    editor.putString( Setting.SET_COUNTRY, Setting.mCountry );
                    editor.putString( Setting.SET_LANGUAGE, Setting.mLang );
                    editor.putBoolean( Setting.SET_COUNTRY_LANGUAGE_SELECTED, Setting.mHasSelect );
                    SharedPreferencesCompat.EditorCompat.getInstance()
                                                        .apply( editor );
                    Setting.changeAppLanguage( LaunchActivity.this );
                    Intent intent = new Intent( LaunchActivity.this, LaunchActivity.class );
                    intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity( intent );
                }
            } );
        }

        mBleStateListener = new BleStateListener()
        {
            @Override
            public void onBluetoothEnabled ()
            {
                if ( Setting.mHasSelect )
                {
                    mCountDownTimer.start();
                }
            }

            @Override
            public void onBluetoothDisabled ()
            {

            }

            @Override
            public void onBluetoothDenied ()
            {
                //                Snackbar.make( null, R.string.snackbar_bluetooth_denied, Snackbar.LENGTH_LONG ).show();
                Toast.makeText( LaunchActivity.this, R.string.snackbar_bluetooth_denied, Toast.LENGTH_LONG )
                     .show();
                if ( Setting.mHasSelect )
                {
                    mCountDownTimer.start();
                }
            }

            @Override
            public void onCoarseLocationGranted ()
            {

            }

            @Override
            public void onCoarseLocationDenied ()
            {

            }

            @Override
            public void onBleInitialized ()
            {

            }
        };
        BleManager.getInstance()
                  .setBleStateListener( mBleStateListener );
        mCountDownTimer = new CountDownTimer( 1500, 1500 )
        {
            @Override
            public void onTick ( long millisUntilFinished )
            {

            }

            @Override
            public void onFinish ()
            {
                startActivity( new Intent( LaunchActivity.this, MainActivity.class ) );
                finish();
            }
        };
        if ( BleManager.getInstance()
                       .checkBleSupported( this ) )
        {
            if ( BleManager.getInstance()
                           .isBluetoothEnabled() ||
                 ( Setting.mBleEnabled &&
                   BleManager.getInstance()
                             .autoOpenBluetooth() ) )
            {
                if ( Setting.mHasSelect )
                {
                    mCountDownTimer.start();
                }
            }
            else
            {
                BleManager.getInstance()
                          .requestBluetoothEnable( this );
            }
        }
        else
        {
            Toast.makeText( this, R.string.ble_no_support, Toast.LENGTH_SHORT )
                 .show();
            finish();
            return;
        }
    }
}
