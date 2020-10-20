package com.example.projectapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectapplication.dialogs.PasswordResetDialog;
import com.example.projectapplication.dialogs.ResendVerificationDialog;
import com.example.projectapplication.models.User;
import com.example.projectapplication.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private EditText mEmail;
    private EditText mPassword;
    private Button mSignIn;
    private TextView mRegister;
    private TextView mForgotPassword;
    private TextView mResendVerification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setupAuthStateListener();
    }

    private void init() {
        mEmail = findViewById(R.id.email_address);
        mPassword = findViewById(R.id.password);
        mSignIn = findViewById(R.id.signin);
        mRegister = findViewById(R.id.register_button);
        mForgotPassword = findViewById(R.id.forgot_password);
        mResendVerification = findViewById(R.id.resend_verification);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordResetDialog dialog = new PasswordResetDialog();
                dialog.show(getSupportFragmentManager(),"password_reset_dialog");
            }
        });

        mResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResendVerificationDialog dialog = new ResendVerificationDialog();
                dialog.show(getSupportFragmentManager(), "resend_verification_dialog");
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(mEmail.getText().toString()) && !isEmpty(mPassword.getText().toString())){
                    if(mPassword.getText().toString().length()>6){
                        login(mEmail.getText().toString(),mPassword.getText().toString());
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"there is an empty field",Toast.LENGTH_LONG).show();
                }
            }
        });

        UniversalImageLoader imageLoader = new UniversalImageLoader(getApplication());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void login(String email,String password){
        Log.d(TAG, "login() called with: email = [" + email + "], password = [" + password + "]");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "onComplete: "+task.getResult().toString());
                if(task.isSuccessful()){
//                    Toast.makeText(getApplicationContext(),"login successful",Toast.LENGTH_LONG).show();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child(getString(R.string.dbuser_node))
                            .orderByKey()
                            .equalTo(FirebaseAuth.getInstance().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getChildren().iterator().next().getValue(User.class);
                                    if(user.getType().equals("Tutor")){
                                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "This is the tutor application, please login with the student application", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: "+error.getMessage());
                                }
                            });
//                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"unable to login",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                Toast.makeText(getApplicationContext(),"unable to login",Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isEmpty(String text){return text.equals("");}

    private void setupAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    if(user.isEmailVerified()){
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Email is not Verified\nCheck your Inbox", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
                else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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