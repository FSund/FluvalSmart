package com.inledco.fluvalsmart.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class FaqFragment extends BaseFragment {
    private Toolbar faq_toolbar;
    private TextView faq_tv_msg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        initView(view);
        initEvent();
        return view;
    }

    @Override
    protected void initView(View view) {
        faq_toolbar = view.findViewById(R.id.faq_toolbar);
        faq_tv_msg = view.findViewById(R.id.faq_tv_msg);
    }

    @Override
    protected void initEvent() {
        faq_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Override
    protected void initData() {

    }
}
