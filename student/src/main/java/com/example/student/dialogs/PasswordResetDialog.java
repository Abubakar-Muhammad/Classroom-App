package com.example.student.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

//import com.example.projectapplication.R;
import com.example.student.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetDialog extends DialogFragment {

    private EditText mEmail;
    private FirebaseAuth mAuth;
    private static final String TAG = "PasswordResetDialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reset_password_dialog,container,false);
        mEmail = view.findViewById(R.id.email);
        TextView confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(mEmail.getText().toString())){
                    sendPasswordReset(mEmail.getText().toString());
                }
                else {
                    Toast.makeText(getContext(), "Fill out the password field", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    private void sendPasswordReset(String email) {
        Log.d(TAG, "sendPasswordReset: sending password reset link");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: onComplete called");
                Toast.makeText(getContext(),"Password reset link sent to your email",Toast.LENGTH_LONG).show();
                getDialog().dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: error"+e.getMessage());
            }
        });
        mAuth.signOut();
    }

    private boolean isEmpty(String text){return text.equals("");}



}
