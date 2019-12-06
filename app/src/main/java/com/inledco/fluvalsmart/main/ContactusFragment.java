package com.inledco.fluvalsmart.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.web.WebActivity;

public class ContactusFragment extends BaseFragment implements View.OnClickListener {

    private Toolbar contactus_toolbar;

    private LinearLayout contactus_fluval;
    private LinearLayout contactus_facebook;
    private LinearLayout contactus_twitter;
    private LinearLayout contactus_instagram;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contactus, container, false);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    protected void initView(View view) {
        contactus_toolbar = view.findViewById(R.id.contactus_toolbar);
        contactus_fluval = view.findViewById(R.id.contactus_fluval);
        contactus_facebook = view.findViewById(R.id.contactus_facebook);
        contactus_twitter = view.findViewById(R.id.contactus_twitter);
        contactus_instagram = view.findViewById(R.id.contactus_instagram);
    }

    @Override
    protected void initEvent() {
        contactus_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        contactus_fluval.setOnClickListener(this);
        contactus_facebook.setOnClickListener(this);
        contactus_twitter.setOnClickListener(this);
        contactus_instagram.setOnClickListener(this);
    }

    @Override
    protected void initData() {
//        contactus_fluval.setText(getClickableSpan(getString(R.string.fluval_url)));
//        contactus_facebook.setText(getClickableSpan(getString(R.string.facebook_url)));
//        contactus_twitter.setText(getClickableSpan(getString(R.string.twitter_url)));
//        contactus_instagram.setText(getClickableSpan(getString(R.string.instagram_url)));
//
//        contactus_fluval.setMovementMethod(LinkMovementMethod.getInstance());
//        contactus_facebook.setMovementMethod(LinkMovementMethod.getInstance());
//        contactus_twitter.setMovementMethod(LinkMovementMethod.getInstance());
//        contactus_instagram.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void startWebActivity(final String url) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("allow_open_in_browser", true);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    //设置超链接文字
//    private SpannableString getClickableSpan(final String text) {
//        SpannableString spanStr = new SpannableString(text);
//        //设置下划线文字
//        spanStr.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        //设置文字的单击事件
//        spanStr.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                startWebActivity(text);
//            }
//        }, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        //设置文字的前景色
//        spanStr.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return spanStr;
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contactus_fluval:
                startWebActivity(getString(R.string.fluval_url));
                break;
            case R.id.contactus_facebook:
                startWebActivity(getString(R.string.facebook_url));
                break;
            case R.id.contactus_twitter:
                startWebActivity(getString(R.string.twitter_url));
                break;
            case R.id.contactus_instagram:
                startWebActivity(getString(R.string.instagram_url));
                break;
        }
    }
}
