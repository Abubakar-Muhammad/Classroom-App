package com.example.student.ui.lecture_notes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LectureNotesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LectureNotesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}