package com.inledco.fluvalsmart.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.prefer.Setting;
import com.inledco.fluvalsmart.web.WebActivity;

public class ContactusFragment extends BaseFragment implements View.OnClickListener {

    private Toolbar contactus_toolbar;

    private LinearLayout contactus_fluval;
    private LinearLayout contactus_facebook;
    private LinearLayout contactus_twitter;
    private LinearLayout contactus_instagram;
    private LinearLayout contactus_email;

    private boolean isJapanese;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contactus, container, false);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {        //
                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
            } else {
                //                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                //                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
            } else {
                return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    protected void initView(View view) {
        contactus_toolbar = view.findViewById(R.id.contactus_toolbar);
        contactus_fluval = view.findViewById(R.id.contactus_fluval);
        contactus_facebook = view.findViewById(R.id.contactus_facebook);
        contactus_twitter = view.findViewById(R.id.contactus_twitter);
        contactus_instagram = view.findViewById(R.id.contactus_instagram);
        contactus_email = view.findViewById(R.id.contactus_email);
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
        contactus_email.setOnClickListener(this);
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

        String lang = Setting.getLanguage(getContext());
        isJapanese = TextUtils.equals(lang, "ja");
        contactus_facebook.setVisibility(isJapanese ? View.GONE : View.VISIBLE);
        contactus_twitter.setVisibility(isJapanese ? View.GONE : View.VISIBLE);
        contactus_instagram.setVisibility(isJapanese ? View.GONE : View.VISIBLE);
        contactus_email.setVisibility(isJapanese ? View.GONE : View.VISIBLE);
    }

    private void startWebActivity(final String url) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("allow_open_in_browser", true);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void startEmail(final String email) {
        Uri uri = Uri.parse("mailto:" + email);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            case R.id.contactus_email:
                startEmail(getString(R.string.fluval_email));
                break;
        }
    }
}
