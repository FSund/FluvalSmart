package com.inledco.fluvalsmart.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.web.WebActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends BaseFragment
{
    private final String GOOGLE_DOC_URL = "https://docs.google.com/gview?embedded=true&url=";
    private final String UM_URL = "https://www.fluvalaquatics.com/fluvalsmart";
    private final String UPGRADE_GUIDE_LINK = "https://www.fluvalaquatics.com/fluvalsmartfirmwareupdate/";

    private TextView news_faq;
    private TextView news_troubleshooting;
    private TextView news_um;
    private TextView news_instruction;
    private TextView news_contactus;

    public NewsFragment ()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        initView(view);
        initEvent();
        initData();
        return view;
    }

    @Override
    protected void initView(View view)
    {
        news_faq = view.findViewById(R.id.news_faq);
        news_troubleshooting = view.findViewById(R.id.news_troubleshooting);
        news_um = view.findViewById(R.id.news_um);
        news_instruction = view.findViewById(R.id.news_instruction);
        news_contactus = view.findViewById(R.id.news_contactus);
    }

    @Override
    protected void initEvent ()
    {
        news_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "file:///android_asset/faq/" + getString(R.string.faq_path);
                startWebActivity(url);
            }
        });

        news_troubleshooting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "file:///android_asset/troubleshooting/" + getString(R.string.troubleshooting_path);
                startWebActivity(url);
            }
        });

        news_um.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(Setting.getLanguage(getContext()), "ja")) {
                    startWebActivity(GOOGLE_DOC_URL + getString(R.string.user_manual_url), true);
                } else {
                    startWebActivity(UM_URL, true);
                }
            }
        });

        news_instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebActivity(UPGRADE_GUIDE_LINK, true);
            }
        });

        news_contactus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.activity_main, new ContactusFragment());
            }
        });
    }

    @Override
    protected void initData () {

    }

    private void startWebActivity(@NonNull final String url) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void startWebActivity(@NonNull final String url, final boolean allowOpenInBrowser) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("allow_open_in_browser", allowOpenInBrowser);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}
