package com.example.projectapplication.ui.announcements;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.Announcements;
import com.example.projectapplication.ui.SharedViewModel;
import com.example.projectapplication.utils.AnnouncementsListAdapter;
import com.example.projectapplication.utils.DateAndTimeConversion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnnouncementsFragment extends Fragment {

    private AnnouncementsViewModel homeViewModel;
    private FloatingActionButton mFab;
    private String mCourseId= null;
    private static final String TAG = "AnnouncementsFragment";
    private SharedViewModel mSharedViewModel;
    DatabaseReference mReference;
    private AlertDialog mAlertDialog;
    private RecyclerView mRecyclerView;
    private List<Announcements> mAnnouncementsList;
    private AnnouncementsListAdapter mAdapter;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
//        Bundle bundle = getArguments();
//        mCourseId = bundle.getString("courseId");
        final View root = inflater.inflate(R.layout.fragment_announcements, container, false);
        mRecyclerView = root.findViewById(R.id.recyclerview);
        mFab = root.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: "+mCourseId);
                View dialogView = inflater.inflate(R.layout.new_announcement_dialog,container,false);
                final EditText announcementHeading = dialogView.findViewById(R.id.announcement_heading);
                final EditText announcementContent = dialogView.findViewById(R.id.announcement_content);
                TextView submit = dialogView.findViewById(R.id.submit);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setView(dialogView);
                mAlertDialog = builder.create();
                mAlertDialog.show();
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createNewAnnouncement(announcementHeading.getText().toString(),announcementContent.getText().toString());
                    }
                });
            }
        });

        return root;
    }

    public static AnnouncementsFragment newInstance(){
        return new AnnouncementsFragment();
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

    public void createNewAnnouncement(String title, String content){
        Announcements announcements = new Announcements();
        announcements.setAnnouncement_title(title);
        announcements.setAnnouncement_description(content);
        announcements.setAnnouncement_time(Calendar.getInstance().getTime().toString());
        mReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbannouncements_node));
        final String announcementId = mReference.push().getKey();
        mReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbannouncements_node))
                .child(announcementId)
                .setValue(announcements)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: "+"Announcement inserted"+announcementId);
                            mAlertDialog.hide();
                            Snackbar.make(getView(),"announcement inserted successfully",Snackbar.LENGTH_SHORT).show();
                            getAnnouncements();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Snackbar.make(getView(),"announcement insertion failed",Snackbar.LENGTH_SHORT).show();
                        mAlertDialog.hide();
                    }
                });

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
//        for(int i=0;i<mAnnouncementsList.size();i++){
//            if(i !=0){
//                Date date = DateAndTimeConversion.dateConversion(mAnnouncementsList.get(i).getAnnouncement_time());
//                Date previousDate = DateAndTimeConversion.dateConversion(mAnnouncementsList.get(i-1).getAnnouncement_time());
//                if(date.after(previousDate)){
//                    mAnnouncementsList.get(i).setNewDate(true);
////                notifyItemChanged(position);
//                }
//                else{
//                    mAnnouncementsList.get(i).setNewDate(false);
//                }
//            }
//            else if(i ==0){
//                mAnnouncementsList.get(i).setNewDate(true);
////            notifyItemChanged(position);
//            }
//        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        int position = layoutManager.findFirstVisibleItemPosition();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AnnouncementsListAdapter(getActivity(),mAnnouncementsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getLayoutManager().scrollToPosition(mAnnouncementsList.size()-1);
    }

}