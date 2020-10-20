package com.example.projectapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.projectapplication.ui.SharedViewModel;
import com.example.projectapplication.ui.announcements.AnnouncementsFragment;
import com.example.projectapplication.ui.lectures_notes.LectureNotesFragment;
import com.example.projectapplication.ui.chat.ChatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

public class CourseActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "CourseActivity";
    private String mCourseId;
    SharedViewModel mSharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mCourseId = getIntent().getStringExtra(getString(R.string.intent_course));
//        Bundle bundle = new Bundle();
//        bundle.putString("courseId", mCourseId);


        getSupportActionBar().setTitle(getString(R.string.title_announcements));
//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        transaction.add(R.id.nav_host_fragment,AnnouncementsFragment.newInstance());
//        transaction.commit();


        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_announcements:
                        getSupportActionBar().setTitle(getString(R.string.title_announcements));
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,AnnouncementsFragment.newInstance())
                        .commitNow();
                        return true;
                    case R.id.navigation_lecture_notes:
                        getSupportActionBar().setTitle(getString(R.string.title_lecture_notes));
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, LectureNotesFragment.newInstance())
                                .commitNow();
                        return true;
                    case R.id.navigation_chats:
                        getSupportActionBar().setTitle(getString(R.string.title_chats));
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ChatsFragment.newInstance())
                                .commitNow();
                       return true;
                }
                return false;
            }
        });

        mSharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        mSharedViewModel.init();
        mSharedViewModel.sendData(mCourseId);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_announcements, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

//        NavController navController = navHostFragment.getNavController();

//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);
//        navController.setGraph(navController.getGraph(), bundle);


        setupAuthStateListener();
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
                startActivity(new Intent(CourseActivity.this,SettingsActivity.class));
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
        Intent intent = new Intent(CourseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(TAG, "checkAuthenticationState: user is authenticated");
//            Toast.makeText(getApplicationContext(),"Authenticated with"+user.getEmail(),Toast.LENGTH_LONG).show();
        }
        else {
            Log.d(TAG, "checkAuthenticationState: user is not authenticated");
            Toast.makeText(getApplicationContext(),"Account not verified, check your email for verification link",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CourseActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void setupAuthStateListener(){
       mAuthStateListener = new FirebaseAuth.AuthStateListener() {
           @Override
           public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               if(firebaseAuth.getCurrentUser() != null){
                       Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseAuth.getCurrentUser().getUid());
               }
               else{
                   Log.d(TAG, "onAuthStateChanged:signed_out");
                   Intent intent = new Intent(CourseActivity.this, LoginActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
                   finish();
               }
           }
       };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
    }
}