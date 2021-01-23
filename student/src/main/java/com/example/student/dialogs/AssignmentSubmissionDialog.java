package com.example.student.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;


import com.example.student.R;
import com.example.student.models.AssignmentSubmission;
import com.example.student.ui.assignments.AssignmentsFragment;
import com.example.student.utils.AssignmentsListAdapter;
import com.example.student.utils.FilePaths;
import com.example.student.utils.FileUtil;
import com.example.student.utils.UpdateListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.student.utils.AssignmentsListAdapter.*;

public class AssignmentSubmissionDialog extends DialogFragment {

    private static final String TAG = "AssignmentSubmissionDia";
    private TextView mCourseCode;
    private TextView mCourseTitle;
    private TextView mCreate;
    private TextView mFile_upload;
    private TextView mUpload_button;
    private ImageView mCancel;
    private Uri mUri;
    private String mCourseId;
    private String mAssignmentId;
    public static final int PICKFILE_REQUEST_CODE = 8352;
    public static final String COURSE_ID = "COURSE_ID";
    public static final String ASSIGNMENT_ID = "ASSIGNMENT_ID";
    private UpdateListener mListener;

    public void setListener(UpdateListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        Bundle bundle = getArguments();
        mCourseId = bundle.getString(COURSE_ID);
        mAssignmentId = bundle.getString(ASSIGNMENT_ID);
    }

    private void init() {
        mFile_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mUpload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FilePaths paths = new FilePaths();
                FileUtil fileUtil = new FileUtil();
                String path = fileUtil.getPath(getContext(),mUri);
                final File file = new File(mUri.getPath());
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child(paths.FIREBASE_ASSIGNMENTS_SUBMISSION_STORAGE)
                        .child(mCourseId)
                        .child(mAssignmentId)
                        .child(mAssignmentId+"_"+file.getName());

                UploadTask uploadTask = FirebaseStorage.getInstance().getReference()
                        .child(paths.FIREBASE_ASSIGNMENTS_SUBMISSION_STORAGE)
                        .child(mCourseId)
                        .child(mAssignmentId)
                        .child(mAssignmentId+"_"+file.getName())
                        .putFile(mUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child(getString(R.string.dbuser_node))
                                        .orderByKey()
                                        .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                DataSnapshot data = snapshot.getChildren().iterator().next();
                                                String student_id = data.getValue(AssignmentSubmission.class).getStudent_id();
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(getString(R.string.dbuser_node))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .child(getString(R.string.dbcourses_node))
                                                        .child(mCourseId)
                                                        .child(getString(R.string.dbassignments_submission_node))
                                                        .child(mAssignmentId)
                                                        .child(getString(R.string.dbstudent_id))
                                                        .setValue(student_id);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(getString(R.string.dbuser_node))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .child(getString(R.string.dbcourses_node))
                                                        .child(mCourseId)
                                                        .child(getString(R.string.dbassignments_submission_node))
                                                        .child(mAssignmentId)
                                                        .child(getString(R.string.dbsubmission_status))
                                                        .setValue(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS_SUBMIITED);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(getString(R.string.dbuser_node))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .child(getString(R.string.dbcourses_node))
                                                        .child(mCourseId)
                                                        .child(getString(R.string.dbassignments_submission_node))
                                                        .child(mAssignmentId)
                                                        .child(getString(R.string.dbsubmission_name))
                                                        .setValue(file.getName());

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(getString(R.string.dbuser_node))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .child(getString(R.string.dbcourses_node))
                                                        .child(mCourseId)
                                                        .child(getString(R.string.dbassignments_submission_node))
                                                        .child(mAssignmentId)
                                                        .child(getString(R.string.dbsubmission_url))
                                                        .setValue(uri.toString());
                                                Toast toast = Toast.makeText(getContext(), "Submission successful", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                mListener.updateList();
                                                dismiss();
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d(TAG, "onCancelled: "+error.getMessage());

                                            }
                                        });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "Upload is " + progress + "% done");
                        getDialog().hide();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(getContext(),"Submission unsuccessful",Toast.LENGTH_SHORT).show();
                    }
                });;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.assigment_upload_dialog,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFile_upload = getView().findViewById(R.id.file_upload);
        mUpload_button = getView().findViewById(R.id.upload);
        mCancel = getView().findViewById(R.id.delete);
        init();
    }

    public void getFile(){
        String[] mimeTypes =
                {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip", "application/vnd.android.package-archive","text/csv"};
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,PICKFILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            mUri = data.getData();
            Log.d(TAG, "onClick: uri: "+mUri);
            mFile_upload.setText(mUri.toString());
        }
    }
}
