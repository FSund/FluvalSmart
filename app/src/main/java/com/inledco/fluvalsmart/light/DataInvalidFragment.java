package com.inledco.fluvalsmart.light;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseFragment;
import com.inledco.fluvalsmart.web.WebActivity;
import com.liruya.tuner168blemanager.BleManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataInvalidFragment extends BaseFragment {
    private String mAddress;
    private TextView data_invalid_msg;
    private ImageButton data_invalid_tip;

    private OnRetryClickListener mListener;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static DataInvalidFragment newInstance(String address) {
        DataInvalidFragment frag = new DataInvalidFragment();
        Bundle bundle = new Bundle();
        bundle.putString("address", address);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnRetryClickListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddress = getArguments().getString("address");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_invalid, container, false);

        initView(view);
        initEvent();
        initData();
        return view;
    }

    @Override
    protected void initView(View view) {
        data_invalid_msg = view.findViewById(R.id.data_invalid_msg);
        data_invalid_tip = view.findViewById(R.id.data_invalid_tip);

        data_invalid_msg.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bluetooth_disabled_grey_500_48dp, 0, 0);
    }

    @Override
    protected void initEvent() {
        data_invalid_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRetryClick();
            }
        });
        data_invalid_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFAQ();
            }
        });
    }

    @Override
    protected void initData() {
        if (!BleManager.getInstance()
                       .isConnected(mAddress))
        {
            data_invalid_msg.setText(R.string.msg_disconnected);
            return;
        }
        if (BleManager.getInstance()
                      .isDataValid(mAddress))
        {
            data_invalid_msg.setText(R.string.msg_get_data_failed);
        }
    }

    private void showFAQ() {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra("url", "file:///android_asset/faq/" + getString(R.string.faq_path));
        startActivity(intent);
    }

    public interface OnRetryClickListener {
        void onRetryClick();
    }
}
