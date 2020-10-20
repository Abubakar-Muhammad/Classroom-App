package com.example.projectapplication.ui;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<String> mCourseId;

    public void init(){
       mCourseId = new MutableLiveData<>();
    }

    public void sendData(String courseId){
        mCourseId.setValue(courseId);
    }

    public LiveData<String> getCourseId(){
        return mCourseId;
    }


}
