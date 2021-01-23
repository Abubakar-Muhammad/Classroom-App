package com.example.projectapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.projectapplication.models.AssignmentSubmission;
import com.example.projectapplication.models.Assignments;
import com.example.projectapplication.models.User;
import com.example.projectapplication.ui.assignments.AssignmentsFragment;
import com.example.projectapplication.utils.SubmittedAssignmentsListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SubmittedAssignmentsActivity extends AppCompatActivity implements ChartValuesUpdate {

    ChartValuesUpdate mChartValuesUpdate;
    private static final String TAG = "SubmittedAssignmentsAct";
    TextView mDescription;
    RecyclerView mRecyclerView;
    PieChart mPieChart;
    String mAssignmentId;
    String mCourseId;
    List<AssignmentSubmission> mAssignmentSubmissions = new ArrayList<>();;
    private SubmittedAssignmentsListAdapter mAdapter;
    float mStudentCount =0f;
    float mSubmissionCount =0f;
    int mCount =0;
    int mStudentSubmissionCount =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submitted_assignments);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDescription = findViewById(R.id.description);
        mRecyclerView = findViewById(R.id.recyclerview);
//        mPieChart = findViewById(R.id.piechart);
        mAssignmentId = getIntent().getStringExtra(AssignmentsFragment.ASSIGNMENT_INTENT_EXTRA);
        mCourseId = getIntent().getStringExtra(AssignmentsFragment.COURSE_INTENT_EXTRA);
        getAssignmentSubmissions();
        setupRecyclerView();
        mChartValuesUpdate = this;
    }

    public void setupRecyclerView(){
        mAdapter = new SubmittedAssignmentsListAdapter(mAssignmentSubmissions,this,mCourseId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    public void getAssignmentSubmissions(){
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbassignments_node))
                .orderByKey()
                .equalTo(mAssignmentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String description = snapshot.getChildren().iterator().next().getValue(Assignments.class).getAssignment_description();
                        mDescription.setText(description);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.db_users))
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: getting Users");
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Map<String, Object> map = (Map<String,Object>) dataSnapshot.getValue();
                            String userId = map.get(getString(R.string.user_id)).toString();
                            String userType = map.get(getString(R.string.user_type)).toString();
                            Log.d(TAG, "onDataChange: userType"+userType);
                            if(userType.equals("Student")){
                                FirebaseDatabase.getInstance().getReference()
                                        .child(getString(R.string.db_users))
                                        .child(userId)
                                        .child(getString(R.string.dbcourses_node))
                                        .orderByKey()
                                        .equalTo(mCourseId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Log.d(TAG, "onDataChange: "+mCount);
                                                for(DataSnapshot dataSnapshot1 :snapshot.getChildren()){
                                                    mChartValuesUpdate.updateStudentCount(1);
                                                }
                                                if(mStudentSubmissionCount ==0){

                                                }
                                                else {
                                                    updateChart();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference()
                                        .child(getString(R.string.db_users))
                                        .child(userId)
                                        .child(getString(R.string.dbcourses_node))
                                        .child(mCourseId)
                                        .child(getString(R.string.dbassignments_submission_node))
                                        .orderByKey()
                                        .equalTo(mAssignmentId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                mStudentCount = +1.0f;
                                                Log.d(TAG, "onDataChange: Student count" + mStudentCount);
                                                Log.d(TAG, "onDataChange: getting Submissions");
                                                String status = null;
                                                AssignmentSubmission assignmentSubmission;
                                                if (snapshot.exists()) {
                                                    try {
                                                        status = Objects.requireNonNull(snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class)).getSubmission_status();
                                                    } finally {
                                                        if (status != null) {
                                                            assignmentSubmission = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class);
                                                            if (!status.equals(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS)) {
                                                                Log.d(TAG, "onDataChange: adding submission items");
                                                                mAssignmentSubmissions.add(assignmentSubmission);
                                                                mChartValuesUpdate.updateSubmissionCount(1);
//                                                                mSubmissionCount = +1.0f;
//                                                                mStudentSubmissionCount = +1;

                                                                Log.d(TAG, "onDataChange: submission count" + mSubmissionCount);
                                                            }
                                                        }
                                                        updateChart();
                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d(TAG, "onCancelled: error "+error.getMessage());
                                            }
                                        });

//                                updateChart();
                            }
                        }

//                        mAdapter.updateData(mAssignmentSubmissions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: error "+error.getMessage());
                    }
                });
        Log.d(TAG, "getAssignmentSubmissions: mCount"+mCount);
//        updateChart();
    }
//    void setupPiechart(){
//        Log.d(TAG, "setupPiechart: submission count"+Float.toString(mSubmissionCount));
//        Log.d(TAG, "setupPiechart: student count"+Float.toString(mStudentCount));
//        mPieChart.addSlice(new PieChart.Slice(getResources().getColor(R.color.assignment_done),mSubmissionCount,getDrawable(R.drawable.ic_baseline_assignment_turned_in_24)));
//        mPieChart.addSlice(new PieChart.Slice(getResources().getColor(R.color.assignment_not_done),mStudentCount,getDrawable(R.drawable.ic_baseline_assignment_late_24)));
//    }


    private void updateChart(){
        // Update the text in a center of the chart:
        TextView numberOfCals = findViewById(R.id.number);
        numberOfCals.setText(String.valueOf(mStudentSubmissionCount) + " / " + mCount);

        // Calculate the slice size and update the pie chart:
        ProgressBar pieChart = findViewById(R.id.stats_progressbar);
        double d = (double) mStudentSubmissionCount / (double) mCount;
        int progress = (int) (d * 100);
        pieChart.setProgress(progress);
    }

    @Override
    public void updateStudentCount(int count) {
        mCount = mCount +count;
//        updateChart();
    }

    @Override
    public void updateSubmissionCount(int count) {
        mStudentSubmissionCount = mStudentSubmissionCount + count;
    }




}
interface ChartValuesUpdate{
    void updateStudentCount(int count);
    void updateSubmissionCount(int count);
}