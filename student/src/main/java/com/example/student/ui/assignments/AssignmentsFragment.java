package com.example.student.ui.assignments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.R;
import com.example.student.models.Assignments;
import com.example.student.ui.SharedViewModel;
import com.example.student.ui.lecture_notes.LectureNotesFragment;
import com.example.student.utils.AssignmentsListAdapter;
import com.example.student.utils.DateAndTimeConversion;
import com.example.student.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Result;

import static com.example.student.utils.AssignmentsListAdapter.*;


public class AssignmentsFragment extends Fragment {

    private SharedViewModel mSharedViewModel;
    private String mCourseId;
    private RecyclerView mRecyclerView;
    DatabaseReference mDatabaseReference;
    StorageReference mStorageReference;
    private static final String TAG = "AssignmentsFragment";
    private List<Assignments> mAssignmentsList = new ArrayList<>();
    private AssignmentsListAdapter mAdapter;
    public static final String ASSIGNMENT_SUBMISSION_STATUS_NOT_SUBMIITED = "Not Submitted";
    public static final String ASSIGNMENT_SUBMISSION_STATUS_SUBMIITED = "Submitted";



    public static AssignmentsFragment newInstance(){
       return new AssignmentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assignments,container,false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mCourseId = s;
                getAssignments();
                setupRecyclerView();
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setupRecyclerView(){
        mRecyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.scrollToPosition(mAssignmentsList.size()-1);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new AssignmentsListAdapter(mAssignmentsList,getContext(),mCourseId);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getAssignments(){
        Log.d(TAG, "getAssignments: getting Assigngments");
        mAssignmentsList.clear();
        Query query = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbassignments_node));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()){
                   Assignments assignment = singleSnapshot.getValue(Assignments.class);
                    mAssignmentsList.add(assignment);
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
