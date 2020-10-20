package com.example.student.ui.announcements;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.R;
import com.example.student.models.Announcements;
import com.example.student.ui.SharedViewModel;
import com.example.student.utils.AnnouncementsListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import com.example.projectapplication.R;

public class AnnouncementsFragment extends Fragment {

    private String mCourseId= null;
    private static final String TAG = "AnnouncementsFragment";
    private SharedViewModel mSharedViewModel;
    DatabaseReference mReference;
    private AlertDialog mAlertDialog;
    private RecyclerView mRecyclerView;
    private List<Announcements> mAnnouncementsList;
    private AnnouncementsListAdapter mAdapter;

    public static AnnouncementsFragment newInstance(){
        return new AnnouncementsFragment();
    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
//        Bundle bundle = getArguments();
//        mCourseId = bundle.getString("courseId");
        final View root = inflater.inflate(R.layout.fragment_announcements, container, false);
        mRecyclerView = root.findViewById(R.id.recyclerview);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(TAG, "onChanged: "+s);
                mCourseId = s;
                init();
            }
        });
        mReference = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "onViewCreated: "+ mCourseId);
//        mAnnouncementsList = new ArrayList<>();
//        init();
    }


    private void init(){
        getAnnouncements();
    }

    public void getAnnouncements(){
        mAnnouncementsList = new ArrayList<>();
        Query query = mReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbannouncements_node))
                .orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                    Announcements announcements = singleSnapshot.getValue(Announcements.class);
                    mAnnouncementsList.add(announcements);
                }
                setupRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AnnouncementsListAdapter(getActivity(),mAnnouncementsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getLayoutManager().scrollToPosition(mAnnouncementsList.size()-1);

    }

}