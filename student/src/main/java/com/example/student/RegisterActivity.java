package com.example.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.student.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private Button mRegister;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        mEmail = findViewById(R.id.email_address);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirm_password);
        mRegister = findViewById(R.id.register);
        mSpinner = findViewById(R.id.user_type);

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,new String[]{"Tutor","Student"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(mEmail.getText().toString()) &&
                        !isEmpty(mPassword.getText().toString()) &&
                        !isEmpty(mConfirmPassword.getText().toString())){
                    if(mPassword.getText().toString().equals(mConfirmPassword.getText().toString())){
                        if(isDomainEmail(mEmail.getText().toString())){
                            if(!(mPassword.getText().toString().length() <5)){
                                registerUser(mEmail.getText().toString(),mPassword.getText().toString());
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"The password is less than five characters",Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"The email must be a gmail email",Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                        Toast.makeText(getApplicationContext(),"The passwords are not equal",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"You must fill all fields",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void registerUser(final String email, String password){
        Log.d(TAG, "registerUser() called with: email = [" + email + "], password = [" + password + "]");
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "onComplete: "+task.getResult().toString());
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_LONG).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();

                    User user = new User();
                    user.setName(email.substring(0, email.indexOf("@")));
                    user.setEmail(email);
                    user.setPhone("");
                    user.setType(mSpinner.getSelectedItem().toString());
                    user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    user.setProfile_image("");
                    user.setStudent_id("");
                    FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbuser_node))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseAuth.getInstance().signOut();

                                    //redirect the user to the login screen
                                    redirectLoginScreen();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "something went wrong.", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();

                            //redirect the user to the login screen
                            redirectLoginScreen();
                        }
                    });

//                    firebaseAuth.signOut();
//                    redirectLoginScreen();
                }
                else {
                    Toast.makeText(getApplicationContext(),"registration not successful",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage() );
                Toast.makeText(getApplicationContext(),"unable to register",Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isEmpty(String text){return text.equals("");}

    private boolean isDomainEmail(String email){
        int index  = email.indexOf("@");
        if(email.substring(index+1).equals("gmail.com")){
            return true;
        }
         return false;
    }

    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}