package com.inledco.fluvalsmart.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.splash.SplashActivity;
import com.inledco.fluvalsmart.view.CustomDialogBuilder;
import com.inledco.fluvalsmart.web.WebActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends BaseFragment {
    private final String GOOGLE_DOC_URL = "https://docs.google.com/gview?embedded=true&url=";
    private final String UPGRADE_GUIDE_LINK = "https://www.fluvalaquatics.com/fluvalsmartfirmwareupdate/";
    private SwitchCompat setting_auth_ble;
    private SwitchCompat setting_exit_close_ble;
    private TextView setting_lang;
    private LinearLayout setting_item_lang;
    private TextView setting_profile;
    private TextView setting_um;
    private TextView setting_instruction;
    private LinearLayout setting_ll_version;
    private TextView setting_version;
    private TextView setting_about;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    protected void initView(View view) {
        setting_about = view.findViewById(R.id.setting_about);
        setting_ll_version = view.findViewById(R.id.setting_ll_version);
        setting_version = view.findViewById(R.id.setting_version);
        setting_um = view.findViewById(R.id.setting_um);
        setting_instruction = view.findViewById(R.id.setting_instruction);
        setting_profile = view.findViewById(R.id.setting_profile);
        setting_item_lang = view.findViewById(R.id.setting_item_lang);
        setting_lang = view.findViewById(R.id.setting_lang);
        setting_exit_close_ble = view.findViewById(R.id.setting_exit_close_ble);
        setting_auth_ble = view.findViewById(R.id.setting_auth_ble);
    }

    @Override
    protected void initData() {
        setting_version.setText(getVersion());
        setting_auth_ble.setChecked(Setting.isAutoTurnonBle(getContext()));
        setting_exit_close_ble.setChecked(Setting.isExitTurnoffBle(getContext()));
        String lang = Setting.getLanguage(getContext());
        String[] keys = getResources().getStringArray(R.array.language_keys);
        String[] langs = getResources().getStringArray(R.array.languages);
        String lang_text = langs[0];
        for (int i = 0; i < langs.length && i < keys.length; i++) {
            if (TextUtils.equals(lang, keys[i])) {
                lang_text = langs[i];
                break;
            }
        }
        setting_lang.setText(lang_text);

        //        Resources resources = getContext().getResources();
        //        DisplayMetrics dm = resources.getDisplayMetrics();
        //        Configuration config = resources.getConfiguration();
        // 应用用户选择语言
        //        config.setLocale( Locale.getDefault() );
        //        resources.updateConfiguration(config, dm);
    }

    @Override
    protected void initEvent() {
        setting_item_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLangDialog();
            }
        });

        setting_auth_ble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Setting.setAutoTurnonBle(getContext(), b);
            }
        });

        setting_exit_close_ble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Setting.setExitTurnoffBle(getContext(), b);
            }
        });
        setting_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        setting_um.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebActivity(GOOGLE_DOC_URL + getString(R.string.user_manual_url));
            }
        });

        setting_instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebActivity(UPGRADE_GUIDE_LINK);
            }
        });
    }

    private String getVersion() {
        PackageManager pm = getContext().getPackageManager();
        if (pm == null) {
            return "";
        }
        try {
            PackageInfo pi = pm.getPackageInfo(getContext().getPackageName(), 0);
            if (pi == null || TextUtils.isEmpty(pi.versionName)) {
                return "";
            }
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void showLangDialog() {
        int idx = 0;
        final String[] sl = new String[]{Setting.getLanguage(getContext())};
        final String[] ll = getResources().getStringArray(R.array.language_keys);
        final CharSequence[] langs = getResources().getStringArray(R.array.languages);
        final CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme);
        builder.setTitle(R.string.setting_language);
        for (int i = 0; i < ll.length; i++) {
            if (sl[0].equals(ll[i])) {
                idx = i;
                break;
            }
        }
        builder.setSingleChoiceItems(langs, idx, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                sl[0] = ll[position];
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.equals(sl[0], Setting.getLanguage(getContext()))) {
                    Setting.setLanguage(getContext(), sl[0]);
                    Setting.changeAppLanguage(getContext());
                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showAboutDialog() {
        CustomDialogBuilder builder = new CustomDialogBuilder(getContext(), R.style.DialogTheme);
        builder.setTitle(R.string.setting_about);
        builder.setMessage(R.string.user_about_msg);
        builder.setPositiveButton(R.string.dialog_ok, null);
        builder.show();
        //        AlertDialog dialog = builder.create();
        //        dialog.setCanceledOnTouchOutside( false );
        //        dialog.show();
    }

    private void startWebActivity(@NonNull final String url) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("allow_open_in_browser", true);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}
