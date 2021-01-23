package com.example.projectapplication.ui.assignments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.AssignmentSubmission;
import com.example.projectapplication.models.Assignments;
import com.example.projectapplication.models.Course;
import com.example.projectapplication.models.LectureNotes;
import com.example.projectapplication.models.User;
import com.example.projectapplication.ui.SharedViewModel;
import com.example.projectapplication.ui.lectures_notes.LectureNotesFragment;
import com.example.projectapplication.utils.AssignmentsListAdapter;
import com.example.projectapplication.utils.DateAndTimeConversion;
import com.example.projectapplication.utils.FileUtil;
import com.example.projectapplication.utils.LecturesNotesListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.sql.Time;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssignmentsFragment extends Fragment {

    private SharedViewModel mSharedViewModel;
    private String mCourseId;
    private RecyclerView mRecyclerView;
    private View mDialogView;
    private FloatingActionButton mFab;
    private AlertDialog mUploadDialog;
    private TextView mDescription;
    private TextView mFile_upload_name;
    private TextView mUpload_button;
    private ImageView mCancel;
    private EditText mDue_date;
    DatabaseReference mDatabaseReference;
    StorageReference mStorageReference;
    Uri mSelectedFileUri;
    private static final String TAG = "AssignmentsFragment";
    public static final int PICKFILE_REQUEST_CODE = 8352;
    private List<Assignments> mAssignmentsList = new ArrayList<>();
    private AssignmentsListAdapter mAdapter;
    public static final String ASSIGNMENT_SUBMISSION_STATUS = "Not Submitted";
    public static final String ASSIGNMENT_INTENT_EXTRA = "com.example.projectapplication.ui.assignments.intent_extra_assignment_id";
    public static final String COURSE_INTENT_EXTRA = "com.example.projectapplication.ui.assignments.intent_extra_course_id";


    public static AssignmentsFragment newInstance(){
       return new AssignmentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assignments,container,false);
        mDialogView = inflater.inflate(R.layout.assigment_upload_dialog,container,false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mFab = view.findViewById(R.id.fab);
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mCourseId = s;
                getAssignments();
                setupRecyclerView();
            }
        });
        setupDialog();
        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUploadDialog.show();
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }
    private void setupDialog(){
        mDescription = mDialogView.findViewById(R.id.lecture_note_description);
        mFile_upload_name = mDialogView.findViewById(R.id.file_upload);
        mUpload_button = mDialogView.findViewById(R.id.upload);
        mCancel = mDialogView.findViewById(R.id.delete);
        mDue_date = mDialogView.findViewById(R.id.due_date);
        mDue_date.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d-MM-YYYY HH:mm", Locale.US);
//                sdf.applyPattern("EEE, d MMM YYYY HH mm");
                final Calendar calendar = Calendar.getInstance();
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
                        final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.set(year,month,day,hour,minute);
                                Log.d(TAG, "onTimeSet: "+sdf.format(calendar1.getTime())+','+year+" date:");
                                mDue_date.setText(sdf.format(calendar1.getTime()));
                            }
                        }, hour, minute, true);
                        timePickerDialog.setTitle("Choose Time");
                        timePickerDialog.show();
                    }
                }, year, month, day);
                datePickerDialog.setTitle("Choose date");
                datePickerDialog.show();
            }
        });
        mFile_upload_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile();
            }
        });
        mUpload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadAssignment();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUploadDialog.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(mDialogView);
        mUploadDialog = builder.create();
    }

    private void uploadAssignment(){
        FileUtil fileUtil = new FileUtil();
        String path = fileUtil.getPath(getContext(),mSelectedFileUri);
        File file = new File(mSelectedFileUri.getPath());
        Assignments assignment = new Assignments();
        assignment.setAssignment_name(file.getName());
        assignment.setAssignment_uri(mSelectedFileUri.toString());
        assignment.setAssignment_description(mDescription.getText().toString());
        assignment.setAssignment_tutor_path(path);
        assignment.setAssignment_upload_status(LectureNotesFragment.START);
        assignment.setAssignment_due_date(mDue_date.getText().toString());
        final String assignmentId = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbassignments_node)).push().getKey();
        assignment.setAssignment_id(assignmentId);
        mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbassignments_node))
                .child(assignmentId)
                .setValue(assignment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: "+task.toString()+",task success:"+task.isSuccessful());
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(),"File upload is starting",Toast.LENGTH_SHORT).show();
                            mUploadDialog.dismiss();
//                            setStudentAssignments(assignmentId);
                            getAssignments();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(getContext(), "There was an error uploading. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setStudentAssignments(final String assignmentId) {
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.db_users))
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap: snapshot.getChildren()){
                            Map<String,Object> objectMap = (Map<String, Object>) snap.getValue();
                            User user = new User();
                            user.setId(objectMap.get(getString(R.string.user_id)).toString());
                            user.setType(objectMap.get(getString(R.string.user_type)).toString());
                            if(user.getType().equals("Student")) {
                                Course course = new Course();
                                course.setCourse_id(mCourseId);
                                AssignmentSubmission assignmentSubmission = new AssignmentSubmission();
                                assignmentSubmission.setAssignment_id(assignmentId);
                                if (objectMap.get(getString(R.string.user_student_id)).toString().equals(null)){
                                    return;
                            }
                                else{
                                    Log.d(TAG, "onDataChange: user Id:"+user.getId());
                                    assignmentSubmission.setStudent_id(objectMap.get(getString(R.string.user_student_id)).toString());
                                    assignmentSubmission.setUser_id(user.getId());
                                    assignmentSubmission.setSubmission_status(ASSIGNMENT_SUBMISSION_STATUS);
                                    Task task = FirebaseDatabase.getInstance().getReference()
                                            .child(getString(R.string.dbuser_node))
                                            .child(user.getId())
                                            .child(getString(R.string.dbcourses_node))
                                            .child(mCourseId)
                                            .child(getString(R.string.dbassignments_submission_node))
                                            .child(assignmentId)
                                            .setValue(assignmentSubmission);
                                    task.addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
//                                            Log.d(TAG, "onComplete: task exception"+task.getException().getMessage()+","+task.getException().getCause());
                                            Log.d(TAG, "onComplete: task complete");
                                            Log.d(TAG, "onComplete: "+task.isSuccessful());
                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setupRecyclerView(){
        mRecyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.scrollToPosition(mAssignmentsList.size()-1);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new AssignmentsListAdapter(mAssignmentsList,getContext(),mCourseId);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
    }

    private void getAssignments(){
        Log.d(TAG, "getAssignments: getting Assigngments");
        mAssignmentsList.clear();
        Query query = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbassignments_node));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()){
                   Assignments assignment = singleSnapshot.getValue(Assignments.class);
                    mAssignmentsList.add(assignment);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });
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
        if( requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            Log.d(TAG, "onActivityResult: "+uri);
            mSelectedFileUri = uri;
            mFile_upload_name.setText(uri.getPath());
            File file = new File(uri.getPath());
            Log.d(TAG, "onActivityResult: file path"+file.toString());
        }
    }
}
