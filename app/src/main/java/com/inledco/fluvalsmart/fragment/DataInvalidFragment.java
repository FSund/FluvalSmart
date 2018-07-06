package com.inledco.fluvalsmart.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.blemanager.BleManager;
import com.inledco.fluvalsmart.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataInvalidFragment extends BaseFragment
{
    private String mAddress;
    private TextView data_invalid_msg;

    private OnRetryClickListener mListener;

    public DataInvalidFragment ()
    {
        // Required empty public constructor
    }

    public static DataInvalidFragment newInstance( String address )
    {
        DataInvalidFragment frag = new DataInvalidFragment();
        Bundle bundle = new Bundle();
        bundle.putString( "address", address );
        frag.setArguments( bundle );
        return frag;
    }

    @Override
    public void onAttach ( Context context )
    {
        super.onAttach( context );
        mListener = (OnRetryClickListener) context;
    }

    @Override
    public void onDetach ()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate ( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mAddress = getArguments().getString( "address" );
    }

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.fragment_data_invalid, container, false );

        initView( view );
        initEvent();
        initData();
        return view;
    }

    @Override
    protected void initView ( View view )
    {
        data_invalid_msg = view.findViewById( R.id.data_invalid_msg );
    }

    @Override
    protected void initEvent ()
    {
        data_invalid_msg.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View v )
            {
                mListener.onRetryClick();
            }
        } );
    }

    @Override
    protected void initData ()
    {
        if ( !BleManager.getInstance().isConnected( mAddress ) )
        {
            data_invalid_msg.setText( R.string.msg_disconnected );
            return;
        }
        if ( BleManager.getInstance().isDataValid( mAddress ) )
        {
            data_invalid_msg.setText( R.string.msg_get_data_failed );
        }
    }

    public interface OnRetryClickListener
    {
        void onRetryClick();
    }
}
