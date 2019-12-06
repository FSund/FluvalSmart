package com.inledco.fluvalsmart.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.web.WebActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends BaseFragment
{
    private final String URL1 = "http://www.fluvalaquatics.com/";
    private TextView news_faq;
    private TextView news_troubleshooting;
    private TextView news_contactus;

    public NewsFragment ()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_news, container, false );

        initView( view );
        initEvent();
        initData();
        return view;
    }

    @Override
    protected void initView(View view)
    {
        news_faq = view.findViewById(R.id.news_faq);
        news_troubleshooting = view.findViewById(R.id.news_troubleshooting);
        news_contactus = view.findViewById(R.id.news_contactus);
        view.findViewById( R.id.news_web1 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View view )
            {
                Intent intent = new Intent( getContext(), WebActivity.class );
                intent.putExtra( "url", URL1 );
                startActivity( intent );
            }
        } );
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
}
