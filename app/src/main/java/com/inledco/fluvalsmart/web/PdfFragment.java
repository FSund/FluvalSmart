package com.inledco.fluvalsmart.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;

public class PdfFragment extends BaseFragment {

    private PDFView pdfView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_pdf, container, false);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    protected void initView(View view) {
        pdfView = view.findViewById(R.id.pdf_view);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {

    }
}
