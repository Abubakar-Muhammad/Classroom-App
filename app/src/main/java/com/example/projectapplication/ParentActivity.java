package com.example.projectapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParentActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "ParentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupAuthStateListener();
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
            Intent intent = new Intent(getParent(), LoginActivity.class);
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
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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