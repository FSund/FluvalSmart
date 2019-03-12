package com.inledco.fluvalsmart.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

public class BaseViewModel<T> extends ViewModel {
    private final String TAG = "BaseViewModel";

    private T mData;
    private final MutableLiveData<T> mLiveData = new MutableLiveData<>();

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }

    public void observe(LifecycleOwner owner, Observer<T> observer) {
        mLiveData.observe(owner, observer);
    }

    public void postValue() {
        mLiveData.postValue(mData);
    }

    public void postValue(T data) {
        mLiveData.postValue(data);
    }

    public void setValue() {
        mLiveData.setValue(mData);
    }

    public void setValue(T data) {
        mLiveData.setValue(data);
    }
}
