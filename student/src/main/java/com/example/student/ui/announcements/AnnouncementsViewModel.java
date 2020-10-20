package com.example.student.ui.announcements;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnnouncementsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mCourseId;


    public AnnouncementsViewModel() {
//        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        mCourseId = new MutableLiveData<>();
//        mCourseId.observe();
    }

//    public void init(){
//        mCourseId = new MutableLiveData<>();
//    }

    public void sendData(String courseId){
        mCourseId.setValue(courseId);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<String> getCourseId(){
        return mCourseId;
    }

}