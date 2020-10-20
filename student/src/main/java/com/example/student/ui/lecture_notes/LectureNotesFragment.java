package com.example.student.ui.lecture_notes;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.R;
import com.example.student.models.LectureNotes;
import com.example.student.ui.SharedViewModel;
import com.example.student.utils.LecturesNotesListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

//import com.example.projectapplication.R;

public class LectureNotesFragment extends Fragment {

    private static final String TAG = "LectureNotesFragment";
    public static final String START = "START";
    public static final String PAUSED = "PAUSED";
    public static final String DONE = "DONE";
    public static final String RESUME = "DONE";
    public static final String CANCELLED = "CANCELED";
    private SharedViewModel mSharedViewModel;
    private String mCourseId;
    public static final int PICKFILE_REQUEST_CODE = 8352;
    RecyclerView mRecyclerView;
    DatabaseReference mDatabaseReference;
    StorageReference mStorageReference;
    List<LectureNotes> mLectureNotesList = new ArrayList<>();
    private LecturesNotesListAdapter mAdapter;

    public static LectureNotesFragment newInstance(){
        return new LectureNotesFragment();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lecturenotes, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mCourseId = s;
                getLectureNotes();
                setupRecyclerView();
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setupRecyclerView(){
        mRecyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.scrollToPosition(mLectureNotesList.size()-1);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new LecturesNotesListAdapter(getContext(),mLectureNotesList,mCourseId);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getLectureNotes(){
        Log.d(TAG, "getLectureNotes: getting lecture notes");
        mLectureNotesList.clear();
//        mLectureNotesList = new ArrayList<>();
        Query query = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dblecture_notes_node));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()){
                    LectureNotes lectureNotes = singleSnapshot.getValue(LectureNotes.class);
                    mLectureNotesList.add(lectureNotes);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });
    }

}