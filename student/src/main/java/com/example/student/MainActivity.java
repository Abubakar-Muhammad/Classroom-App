package com.example.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.models.Course;
import com.example.student.models.User;
import com.example.student.utils.CoursesListAdapter;
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
import java.util.List;
import java.util.Map;

public class MainActivity extends ParentActivity {

    private static final String TAG = "MainActivity";
    private List<Course> mCourseList;
    private CoursesListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<String> mCourses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,EnrollCourseActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        init();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case  R.id.account_settings :
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            case R.id.sign_out :
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Log.d(TAG, "signOut: signing user out");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void init(){
        getCourses();
    }

    private void getCourses() {
        Log.d(TAG, "getCourses: querying courses from database");
        mCourseList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.db_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.user_courses))
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot singleSnapshot : snapshot.getChildren()){
                            Log.d(TAG, "onDataChange: "+singleSnapshot.getValue(Course.class));
//                            HashMap<String,Object> dataSnapshot = (HashMap<String, Object>) singleSnapshot.getValue();
//                            mCourses = singleSnapshot.getValue(User.class).getCourses();
//                            mCourses.add(singleSnapshot.getValue(String.class));
                            Course courses = new Course();
                            Map<String,Object> map = (Map<String, Object>) singleSnapshot.getValue();
                            courses.setCourse_id(map.get(getString(R.string.db_course_id)).toString());
                            Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbcourses_node))
                                    .orderByKey()
                                    .equalTo(courses.getCourse_id());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot single:snapshot.getChildren()){
                                        Course course = new Course();
                                        Map<String,Object> objectMap = (Map<String, Object>) single.getValue();
                                        course.setCourse_code(objectMap.get(getString(R.string.db_course_code)).toString());
                                        course.setCourse_name(objectMap.get(getString(R.string.db_course_name)).toString());
                                        course.setCreator_id(objectMap.get(getString(R.string.db_creator_id)).toString());
                                        course.setDate_created(objectMap.get(getString(R.string.db_date_created)).toString());
                                        course.setCourse_id(objectMap.get(getString(R.string.db_course_id)).toString());

                                        ArrayList<User> users = new ArrayList<>();
                                        for(DataSnapshot dataSnapshot : single.child(getString(R.string.db_users)).getChildren()){
                                            User user = new User();
                                            user.setId(dataSnapshot.getValue(User.class).getId());
                                            users.add(user);
                                        }
                                        mCourseList.add(course);
                                    }
                                    setupCoursesList();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: "+error.getMessage());
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setupCoursesList(){
        Log.d(TAG, "setupCoursesList: setting up recyclerview"+mCourseList.size());
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CoursesListAdapter(this,mCourseList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

}