package com.example.student.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.CourseActivity;
import com.example.student.R;
import com.example.student.dialogs.AssignmentSubmissionDialog;
import com.example.student.models.AssignmentSubmission;
import com.example.student.models.Assignments;
import com.example.student.models.LectureNotes;
import com.example.student.ui.assignments.AssignmentsFragment;
import com.example.student.ui.lecture_notes.LectureNotesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignmentsListAdapter extends RecyclerView.Adapter<AssignmentsListAdapter.ViewHolder> implements UpdateListener {
    List<Assignments> mAssignmentsList;
    Context mContext;
    String mCourseId;
    LayoutInflater mInflater;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    private int[] mDrawables = {R.drawable.ic_baseline_pause_24, R.drawable.ic_baseline_stop_24,
            R.drawable.ic_baseline_cloud_upload_24, R.drawable.ic_baseline_play_arrow_24};
    private UploadTask mUploadTask;
    private static final String TAG = "AssignmentsListAdapter";
    private View mDialogView;

    public AssignmentsListAdapter(List<Assignments> assignmentsList, Context context, String courseId) {
        mAssignmentsList = assignmentsList;
        mContext = context;
        mCourseId = courseId;
        mInflater = LayoutInflater.from(context);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.assignment_item,parent,false);
        mDialogView = mInflater.inflate(R.layout.assigment_upload_dialog,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        DateAndTimeConversion dateAndTimeConversion = new DateAndTimeConversion();
        String status = mAssignmentsList.get(position).getAssignment_upload_status();
        String due_date = mAssignmentsList.get(position).getAssignment_due_date();
        String description = mAssignmentsList.get(position).getAssignment_description();
        String file_name = mAssignmentsList.get(position).getAssignment_name();
        if(status.equals(LectureNotesFragment.DONE)){
            if(due_date != null){
                String due_on = "Due on";
                holder.due_date.setText(due_on+": "+dateAndTimeConversion.get12hrAssigmentTime(due_date));
            }
            FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.dbuser_node))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(mContext.getString(R.string.dbcourses_node))
                    .child(mCourseId)
                    .child(mContext.getString(R.string.dbassignments_submission_node))
                    .orderByKey()
                    .equalTo(mAssignmentsList.get(position).getAssignment_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getChildren().iterator().hasNext()){
                                String submission_status = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class).getSubmission_status();
                                holder.status.setText(submission_status);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            holder.description.setText(description);
            holder.file_name.setText(file_name);
        }
    }

    @Override
    public int getItemCount() {
        return mAssignmentsList.size();
    }

    public void updateAssignmentsList(List<Assignments> assignments){
        if(mAssignmentsList != null){
            mAssignmentsList.clear();
        }
        mAssignmentsList = assignments;
        notifyDataSetChanged();
    }

    public void getAssignments(){
        FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(mContext.getString(R.string.dbassignments_node))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Assignments> assignmentsList = new ArrayList<>();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Assignments assignment = dataSnapshot.getValue(Assignments.class);
                            assignmentsList.add(assignment);
                        }
                        updateAssignmentsList(assignmentsList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void updateList() {
        getAssignments();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView due_date;
        TextView description;
        TextView file_name;
        TextView view_button;
        TextView submitAssignment;
        ImageView popupmenu;
        TextView status;
        ProgressBar mProgressBar;
        Uri mUri;

        public ViewHolder(@NonNull final View itemView)  {
            super(itemView);
            due_date = itemView.findViewById(R.id.due_date);
            file_name = itemView.findViewById(R.id.name);
            view_button = itemView.findViewById(R.id.view_button);
            description = itemView.findViewById(R.id.description);
            submitAssignment = itemView.findViewById(R.id.submit);
            popupmenu = itemView.findViewById(R.id.popupmenu);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            status = itemView.findViewById(R.id.status);
            popupmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenu();
                }
            });
            submitAssignment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean dateComparison = DateAndTimeConversion.dueDatePassed(mAssignmentsList.get(getAdapterPosition()).getAssignment_due_date());
                    if(!dateComparison){
                        if(status.getText().toString().equals(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS_SUBMIITED)){
                            Toast toast = Toast.makeText(mContext,"You have already submitted",Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                        else if(status.getText().toString().equals(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS_NOT_SUBMIITED)){
                            Bundle bundle = new Bundle();
                            bundle.putString(AssignmentSubmissionDialog.COURSE_ID,mCourseId);
                            bundle.putString(AssignmentSubmissionDialog.ASSIGNMENT_ID,mAssignmentsList.get(getAdapterPosition()).getAssignment_id());
                            AssignmentSubmissionDialog assignmentSubmissionDialog = new AssignmentSubmissionDialog();
                            assignmentSubmissionDialog.setArguments(bundle);
                            assignmentSubmissionDialog.setListener(AssignmentsListAdapter.this);
                            CourseActivity activity = (CourseActivity) mContext;
                            assignmentSubmissionDialog.show(activity.getSupportFragmentManager(),"assignment submission dialog");
                        }
                    }
                    else {
                        Toast toast = Toast.makeText(mContext,"The submission due date has passed",Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }
            });
            view_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick:viewButton ");
                    String filePath = mAssignmentsList.get(getAdapterPosition()).getAssignment_student_path();
                    if(filePath!= null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            Log.d(TAG, "onClick: " + filePath);
                            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setData(uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(uri, mimetype);
                            mContext.startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(mContext, "The file does not exist. Download the file first", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                    else {
                        Toast toast = Toast.makeText(mContext, "The file does not exist. Download the file first", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
            });
        }

        public void showMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, popupmenu);
            popupMenu.getMenuInflater().inflate(R.menu.lecture_note_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.download:
                            downloadFile();
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        public void downloadFile() {
            final FilePaths filePaths = new FilePaths();
            final FileUtil fileUtil = new FileUtil();
            if (fileUtil.isExternalStorageAvailable() || fileUtil.isExternalStorageReadable()) {
                final String[] fileName = new String[1];
                FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                        .child(mCourseId)
                        .child(mContext.getString(R.string.dbassignments_node))
                        .orderByKey()
                        .equalTo(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Assignments assignment = new Assignments();
                                Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(Assignments.class));
                                assignment = snapshot.getChildren().iterator().next().getValue(Assignments.class);
                                final String filePath = fileUtil.getStorageDirForAssignment(assignment.getAssignment_name());
                                final File file = new File(filePath);
                                StorageReference fileReference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                                        .child(mCourseId)
                                        .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name());
                                if (file != null) {
                                    fileReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Log.d(TAG, "onSuccess: " + taskSnapshot.getTotalByteCount());
                                            Uri uri = Uri.fromFile(file);
                                            Log.d(TAG, "onSuccess: "+uri.getPath()+", filePath"+filePath);
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(mContext.getString(R.string.dbcourses_node))
                                                    .child(mCourseId)
                                                    .child(mContext.getString(R.string.dbassignments_node))
                                                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                                                    .child(mContext.getString(R.string.dbassignment_student_path))
                                                    .setValue(file.getAbsolutePath());
                                            Toast toast = Toast.makeText(mContext,"Download done",Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER,0,0);
                                            toast.show();
                                            mProgressBar.setIndeterminate(false);
                                            getAssignments();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.getMessage());
                                        }
                                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                                                    Log.d(TAG, "onProgress: "+snapshot.getTotalByteCount());
                                                    mProgressBar.setIndeterminate(true);
                                                    mProgressBar.setVisibility(View.VISIBLE);
                                                }
                                            });;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        });
            } else {
                Toast.makeText(mContext, "External Storage is not available", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
