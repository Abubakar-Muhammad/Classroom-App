package com.example.projectapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectapplication.dialogs.ChangePhotoDialog;
import com.example.projectapplication.models.User;
import com.example.projectapplication.utils.FilePaths;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener {

    private FirebaseAuth mAuth;
    private static final String TAG = "SettingsActivity";
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    DatabaseReference mReference;
    private EditText mEmail;
    private EditText mName;
    private EditText mPhone;
    private ImageView mProfileImage;
    private ProgressBar mProgressBar;

//    private static final String DOMAIN_NAME = "tabian.ca";
    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;

    //vars
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mProfileImage = findViewById(R.id.profile_image);
        mProgressBar = findViewById(R.id.progressBar);

        Button save = findViewById(R.id.save);
        TextView change_password = findViewById(R.id.chang_password_button);

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: sending password reset link");
                sendResetPasswordLink();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Save button clicked");
                if(!mEmail.getText().toString().equals(mAuth.getCurrentUser().getEmail())){
                    if(!isEmpty(mEmail.getText().toString()) && !isEmpty(password.getText().toString())){
                        updateEmail(mEmail.getText().toString(),password.getText().toString());
                    }
                    else {
                        Toast.makeText(SettingsActivity.this, "Email and Current Password Fields Must be Filled to Save", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!isEmpty(mPhone.getText().toString())){
                    mReference.child(getString(R.string.dbuser_node))
                            .child(mAuth.getCurrentUser().getUid())
                            .child(getString(R.string.user_phone))
                            .setValue(mPhone.getText().toString());
                }
                if(!isEmpty(mName.getText().toString())){
                    mReference.child(getString(R.string.dbuser_node))
                            .child(mAuth.getCurrentUser().getUid())
                            .child(getString(R.string.user_name))
                            .setValue(mName.getText().toString());
                }
                /*
                ------ Upload the New Photo -----
                 */
                if(mSelectedImageUri != null){
                    uploadNewPhoto(mSelectedImageUri);
                }else if(mSelectedImageBitmap  != null){
                    uploadNewPhoto(mSelectedImageBitmap);
                }
                Toast.makeText(SettingsActivity.this, "saved", Toast.LENGTH_SHORT).show();

            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(mStoragePermissions){
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(),"change photo dialog");
//                }else{
//                    verifyStoragePermissions();
//                }
            }
        });

        setupAuthStateListener();
        setDetails();
//        verifyStoragePermissions();

    }


    private void updateEmail(final String email, String password){
        Log.d(TAG, "updateEmail: updating email");
        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(),password);
        if(!mAuth.getCurrentUser().getEmail().equals(email)){
            if(isDomainEmail(email)){
                mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: reauthenticate success.");
                        if(task.isSuccessful()){
                            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if(task.getResult().getSignInMethods().size() == 0){
                                        mAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d(TAG, "onComplete: email update successful");
                                                    Toast.makeText(SettingsActivity.this, "Email updated succesfully", Toast.LENGTH_LONG).show();
                                                    sendVerificationEmail();
                                                    mAuth.signOut();
                                                }
                                                else{
                                                    Log.d(TAG, "onComplete: Could not update email.");
                                                    Toast.makeText(SettingsActivity.this, "unable to update email", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: error"+e.getMessage());
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(SettingsActivity.this, "The email already exists", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: "+e.getMessage());
                                }
                            });
                        }
                        else {
                            Log.d(TAG, "onComplete: Incorrect Password");
                            Toast.makeText(SettingsActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "“unable to update email”", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Toast.makeText(this, "Invalid domain", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "No changes were made", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendResetPasswordLink(){
        FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Password Reset Email sent.");
                            Toast.makeText(SettingsActivity.this, "Sent Password Reset Link to Email",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d(TAG, "onComplete: No user associated with that email.");

                            Toast.makeText(SettingsActivity.this, "No User Associated with that Email.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(SettingsActivity.this, "Couldn't Verification Send Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void setDetails(){
        Log.d(TAG, "setCurrentEmail: setting current email to EditText field");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            Log.d(TAG, "setCurrentEmail: user is NOT null.");

            String email = user.getEmail();

            Log.d(TAG, "setCurrentEmail: got the email: " + email);

            mReference.child(getString(R.string.dbuser_node))
                    .orderByKey()
                    .equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for(DataSnapshot single : snapshot.getChildren()){
                        Log.d(TAG, "onDataChange: (QUERY METHOD 1) found user: "+snapshot.getValue(User.class).toString());
//                                + single.getValue(User.class).toString());
                        Map<String,Object> usermap = (Map<String, Object>) snapshot.getChildren().iterator().next().getValue();
                        User user = new User();
                        user.setName(usermap.get(getString(R.string.user_name)).toString());
                        user.setPhone(usermap.get(getString(R.string.user_phone)).toString());
                        user.setProfile_image(usermap.get(getString(R.string.user_profile)).toString());
                        mName.setText(user.getName());
                        mPhone.setText(user.getPhone());
                        ImageLoader.getInstance().displayImage(user.getProfile_image(), mProfileImage);
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            mEmail.setText(email);
        }
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
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
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
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
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

    private boolean isEmpty(String text){return text.equals("");}

    private boolean isDomainEmail(String email){
        int index  = email.indexOf("@");
        if(email.substring(index+1).equals("gmail.com")){
            return true;
        }
        return false;
    }

    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);

            ImageLoader.getInstance().displayImage(imagePath.toString(), mProfileImage);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);

            mProfileImage.setImageBitmap(bitmap);
        }
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    SettingsActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);

                }
                break;
        }
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageUri***
     * @param imageUri
     */
    public void uploadNewPhoto(Uri imageUri){
        /*
            upload a new profile photo to firebase storage
         */
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageBitmap***
     * @param imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        /*
            upload a new profile photo to firebase storage
         */
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
            Toast.makeText(SettingsActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(SettingsActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(SettingsActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            hideDialog();
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){
        showDialog();
        FilePaths filePaths = new FilePaths();
//specify where the photo will be stored
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image"); //just replace the old image with the new one

        if(mBytes.length/MB < MB_THRESHHOLD) {

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en") //see nodes below
                    /*
                    Make sure to use proper language code ("English" will cause a crash)
                    I actually submitted this as a bug to the Firebase github page so it might be
                    fixed by the time you watch this video. You can check it out at https://github.com/firebase/quickstart-unity/issues/116
                     */
                    .setCustomMetadata("Mitch's special meta data", "JK nothing special here")
                    .setCustomMetadata("location", "Iceland")
                    .build();
            //if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
//                    final Uri[] firebaseURL = {Uri.parse("")};
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadPhotoUrl) {
//                            Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL[0].toString());
                            //Now play with downloadPhotoUrl
                            //Store data into Firebase Realtime Database
                            Uri firebaseURL = downloadPhotoUrl;
                            Toast.makeText(SettingsActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                            FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.dbuser_node))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.user_profile))
                                    .setValue(firebaseURL.toString());

                            hideDialog();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SettingsActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();

                    hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(SettingsActivity.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }

                }
            })
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}