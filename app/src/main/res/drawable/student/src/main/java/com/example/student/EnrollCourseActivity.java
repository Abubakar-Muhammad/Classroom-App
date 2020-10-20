package com.example.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
//import android.widget.SearchView;


import com.example.student.models.Course;
import com.example.student.models.User;
import com.example.student.utils.CoursesListAdapter;
import com.example.student.utils.EnrollCoursesListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrollCourseActivity extends ParentActivity {

    private static final String TAG = "EnrollCourseActivity";
    RecyclerView mRecyclerView;
    EnrollCoursesListAdapter mAdapter;
    private List<Course> mCourseList;// = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_course_dialog);
        mRecyclerView = findViewById(R.id.recyclerview);
        init();
    }

    private void init() {
//        List<Course> myList = new ArrayList<>();
//        mAdapter = new EnrollCoursesListAdapter(this,myList);
        getCourses();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.bar_search);
       SearchView searchView = (SearchView) searchViewItem.getActionView();
//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void getCourses() {
        Log.d(TAG, "getCourses: querying courses from database");
        mCourseList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        reference.child(getString(R.string.db_users))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(getString(R.string.user_courses))
//                .orderByKey()
        reference.child(getString(R.string.dbcourses_node))
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot single : snapshot.getChildren()) {
                                        Course course = single.getValue(Course.class);
//                                        Course course = new Course();
//                                        Map<String, Object> objectMap = (Map<String, Object>) single.getValue();
//                                        course.setCourse_code(objectMap.get(getString(R.string.db_course_code)).toString());
//                                        course.setCourse_name(objectMap.get(getString(R.string.db_course_name)).toString());
//                                        course.setCreator_id(objectMap.get(getString(R.string.db_creator_id)).toString());
//                                        course.setDate_created(objectMap.get(getString(R.string.db_date_created)).toString());
//                                        course.setCourse_id(objectMap.get(getString(R.string.db_course_id)).toString());

//                                        ArrayList<User> users = new ArrayList<>();
//                                        for (DataSnapshot dataSnapshot : single.child(getString(R.string.db_users)).getChildren()) {
//                                            User user = new User();
//                                            user.setId(dataSnapshot.getValue(User.class).getId());
//                                            users.add(user);
//                                        }
                                        mCourseList.add(course);
                                    }

                                    setupCoursesList();
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
//                            Log.d(TAG, "onDataChange: " + singleSnapshot.getValue(String.class));
////                            HashMap<String,Object> dataSnapshot = (HashMap<String, Object>) singleSnapshot.getValue();
////                            mCourses = singleSnapshot.getValue(User.class).getCourses();
////                            mCourses.add(singleSnapshot.getValue(String.class));
//
//                            Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbcourses_node))
//                                    .orderByKey()
//                                    .equalTo(singleSnapshot.getValue(String.class));
//                            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    for (DataSnapshot single : snapshot.getChildren()) {
//                                        Course course = new Course();
//                                        Map<String, Object> objectMap = (Map<String, Object>) single.getValue();
//                                        course.setCourse_code(objectMap.get(getString(R.string.db_course_code)).toString());
//                                        course.setCourse_name(objectMap.get(getString(R.string.db_course_name)).toString());
//                                        course.setCreator_id(objectMap.get(getString(R.string.db_creator_id)).toString());
//                                        course.setDate_created(objectMap.get(getString(R.string.db_date_created)).toString());
//                                        course.setCourse_id(objectMap.get(getString(R.string.db_course_id)).toString());
//
//                                        ArrayList<User> users = new ArrayList<>();
//                                        for (DataSnapshot dataSnapshot : single.child(getString(R.string.db_users)).getChildren()) {
//                                            User user = new User();
//                                            user.setId(dataSnapshot.getValue(User.class).getId());
//                                            users.add(user);
//                                        }
//                                        mCourseList.add(course);
//                                    }
////                                    setupCoursesList();
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                    Log.d(TAG, "onCancelled: " + error.getMessage());
//                                }
//                            });
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//        setupCoursesList();
    }

    private void setupCoursesList() {
        Log.d(TAG, "setupCoursesList: setting up recyclerview" + mCourseList.size());
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EnrollCoursesListAdapter(this, mCourseList);
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();

    }
}