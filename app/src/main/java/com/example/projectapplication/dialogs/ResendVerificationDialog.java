package com.example.projectapplication.dialogs;

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

import com.example.projectapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResendVerificationDialog extends DialogFragment {

    FirebaseAuth mAuth;
    private static final String TAG = "ResendVerificationDialo";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.resend_verification_email_dialog,container,false);

        TextView confirmButton = view.findViewById(R.id.confirm);
        TextView cancelButton = view.findViewById(R.id.delete);
        final EditText email = view.findViewById(R.id.email);
        final EditText password = view.findViewById(R.id.password);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: called");
                if(!isEmpty(email.getText().toString()) && !isEmpty(password.getText().toString())){
                    resendVerificationEmail(email.getText().toString(),password.getText().toString());
                }
                else {
                    Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
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

    private void resendVerificationEmail(String email,String password) {
        Log.d(TAG, "resendVerificationEmail: resending email verification");
        AuthCredential credential = EmailAuthProvider.getCredential(email,password);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: reauthenticating user");
                    sendVerificationEmail();
                    mAuth.signOut();
                    getDialog().dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: error"+e.getMessage());
                Toast.makeText(getContext(),"Invalid Credentials.\nReset password and try again", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });

    }

    private void sendVerificationEmail(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       Toast.makeText(getActivity(), "Email verification sent", Toast.LENGTH_SHORT).show();
                   }
                   else {
                       Toast.makeText(getActivity(), "Couldn't send email verification", Toast.LENGTH_SHORT).show();
                   }
                }
            });
        }
    }

    private boolean isEmpty(String text){return text.equals("");}

}
