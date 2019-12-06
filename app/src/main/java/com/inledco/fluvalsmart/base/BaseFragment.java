package com.inledco.fluvalsmart.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.fluvalsmart.prefer.Setting;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {
    protected final String TAG = this.getClass()
                                     .getSimpleName();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Setting.changeAppLanguage(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void replaceFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                     .replace(layout, fragment, fragment.getClass().getSimpleName())
                     .commitAllowingStateLoss();
    }

    protected void addFragment(@IdRes int layout, @NonNull final Fragment fragment) {
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                     .add(layout, fragment, fragment.getClass().getSimpleName())
                     .commitAllowingStateLoss();
    }

    protected void addFragmentToStack(@IdRes int layout, @NonNull final Fragment fragment) {
        String name = fragment.getClass().getSimpleName();
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                     .add(layout, fragment, name)
                     .addToBackStack(name)
                     .commitAllowingStateLoss();
    }

    protected abstract void initView(View view);

    protected abstract void initEvent();

    protected abstract void initData();
}
