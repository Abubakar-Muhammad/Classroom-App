package com.example.projectapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.SubmittedAssignmentsActivity;
import com.example.projectapplication.models.AssignmentSubmission;
import com.example.projectapplication.models.Assignments;
import com.example.projectapplication.models.Course;
import com.example.projectapplication.models.LectureNotes;
import com.example.projectapplication.models.User;
import com.example.projectapplication.ui.assignments.AssignmentsFragment;
import com.example.projectapplication.ui.lectures_notes.LectureNotesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.example.projectapplication.ui.assignments.AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS;

public class AssignmentsListAdapter extends RecyclerView.Adapter<AssignmentsListAdapter.ViewHolder> {
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ProgressBar progressBar = holder.mProgressBar;
        DateAndTimeConversion dateAndTimeConversion = new DateAndTimeConversion();
        String status = mAssignmentsList.get(position).getAssignment_upload_status();
        String due_date = mAssignmentsList.get(position).getAssignment_due_date();
        String description = mAssignmentsList.get(position).getAssignment_description();
        String file_name = mAssignmentsList.get(position).getAssignment_name();
        String path = mAssignmentsList.get(position).getAssignment_tutor_path();
        String uri = mAssignmentsList.get(position).getAssignment_uri();
        final String id = mAssignmentsList.get(position).getAssignment_id();
        String url = mAssignmentsList.get(position).getAssignment_url();
        holder.description.setText(description);
        holder.file_name.setText(file_name);
        if(due_date != null){
            String due_on = "Due on";
            holder.due_date.setText(due_on+": "+dateAndTimeConversion.get12hrAssigmentTime(due_date));
        }
        if(status.equals(LectureNotesFragment.START)){
//            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            FilePaths filePaths = new FilePaths();
            Uri file_uri = Uri.parse(uri);
            final StorageReference fileReference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(id+"_"+file_name);
            mUploadTask = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(id+"_"+file_name)
                    .putFile(file_uri);
            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: "+taskSnapshot.toString());
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mDatabaseReference.child(mContext.getString(R.string.dbcourses_node))
                                    .child(mCourseId)
                                    .child(mContext.getString(R.string.dbassignments_node))
                                    .child(id)
                                    .child(mContext.getString(R.string.dbassignment_url))
                                    .setValue(uri.toString());
                            mDatabaseReference.child(mContext.getString(R.string.dbcourses_node))
                                    .child(mCourseId)
                                    .child(mContext.getString(R.string.dbassignments_node))
                                    .child(id)
                                    .child(mContext.getString(R.string.dbassignment_upload_status))
                                    .setValue(LectureNotesFragment.DONE);
                            setStudentAssignments(id);
                            progressBar.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(mContext, "Upload done", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d(TAG, "Upload is " + progress + "% done");
                    progressBar.setProgress((int) progress, true);
                    if (snapshot.getUploadSessionUri() != null){
                        mDatabaseReference.child(mContext.getString(R.string.dbcourses_node))
                                .child(mCourseId)
                                .child(mContext.getString(R.string.dbassignments_node))
                                .child(id)
                                .child(mContext.getString(R.string.dbassignment_session_uri))
                                .setValue(snapshot.getUploadSessionUri().toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: "+e.getMessage());
                }
            });
        }
        else if(status.equals(LectureNotesFragment.DONE)){
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if(status.equals(LectureNotesFragment.PAUSED)){
            progressBar.setProgressDrawable(mContext.getDrawable(mDrawables[3]));
            progressBar.setIndeterminate(false);
        }
        else if(status.equals(LectureNotesFragment.CANCELLED)){
            progressBar.setProgressDrawable(mContext.getDrawable(mDrawables[2]));
            progressBar.setIndeterminate(false);
        }
        else if(status.equals(LectureNotesFragment.RESUME)){

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

    private void setStudentAssignments(final String assignmentId) {
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.db_users))
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Map<String, Object> objectMap = (Map<String, Object>) snap.getValue();
                            User user = new User();
                            user.setId(objectMap.get(mContext.getString(R.string.user_id)).toString());
                            user.setType(objectMap.get(mContext.getString(R.string.user_type)).toString());
                            if (user.getType().equals("Student")) {
                                Course course = new Course();
                                course.setCourse_id(mCourseId);
                                AssignmentSubmission assignmentSubmission = new AssignmentSubmission();
                                assignmentSubmission.setAssignment_id(assignmentId);
                                if (objectMap.get(mContext.getString(R.string.user_student_id)).toString().equals(null)) {
                                    return;
                                } else {
                                    assignmentSubmission.setUser_id(user.getId());
                                    assignmentSubmission.setStudent_id(objectMap.get(mContext.getString(R.string.user_student_id)).toString());
                                    assignmentSubmission.setSubmission_status(ASSIGNMENT_SUBMISSION_STATUS);
                                    Task task = FirebaseDatabase.getInstance().getReference()
                                            .child(mContext.getString(R.string.dbuser_node))
                                            .child(user.getId())
                                            .child(mContext.getString(R.string.dbcourses_node))
                                            .child(mCourseId)
                                            .child(mContext.getString(R.string.dbassignments_submission_node))
                                            .child(assignmentId)
                                            .setValue(assignmentSubmission);
                                    task.addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
//                                            Log.d(TAG, "onComplete: task exception"+task.getException().getMessage()+","+task.getException().getCause());
                                            Log.d(TAG, "onComplete: task complete");
                                            Log.d(TAG, "onComplete: " + task.isSuccessful());
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
        private void deleteStudentAssignments(final String assignmentId) {
            FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.db_users))
                    .orderByKey()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot snap: snapshot.getChildren()){
                                Map<String,Object> objectMap = (Map<String, Object>) snap.getValue();
                                User user = new User();
                                user.setId(objectMap.get(mContext.getString(R.string.user_id)).toString());
                                user.setType(objectMap.get(mContext.getString(R.string.user_type)).toString());
                                if(user.getType().equals("Student")) {
                                    Course course = new Course();
                                    course.setCourse_id(mCourseId);
                                    AssignmentSubmission assignmentSubmission = new AssignmentSubmission();
                                    assignmentSubmission.setAssignment_id(assignmentId);
                                    if (objectMap.get(mContext.getString(R.string.user_student_id)).toString().equals(null)){
                                        return;
                                    }
                                    else{

                                        assignmentSubmission.setStudent_id(objectMap.get(mContext.getString(R.string.user_student_id)).toString());
                                        assignmentSubmission.setSubmission_status(ASSIGNMENT_SUBMISSION_STATUS);
                                        Task task = FirebaseDatabase.getInstance().getReference()
                                                .child(mContext.getString(R.string.dbuser_node))
                                                .child(user.getId())
                                                .child(mContext.getString(R.string.dbcourses_node))
                                                .child(mCourseId)
                                                .child(mContext.getString(R.string.dbassignments_submission_node))
                                                .child(assignmentId)
                                                .removeValue();
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView due_date;
        TextView description;
        TextView file_name;
        TextView view_button;
        TextView view_submissions;
        ImageView popupmenu;
        ProgressBar mProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            due_date = itemView.findViewById(R.id.due_date);
            file_name = itemView.findViewById(R.id.name);
            view_button = itemView.findViewById(R.id.view_button);
            description = itemView.findViewById(R.id.description);
            view_submissions = itemView.findViewById(R.id.view_submission);
            popupmenu = itemView.findViewById(R.id.popupmenu);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            popupmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenu();
                }
            });
            view_submissions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, SubmittedAssignmentsActivity.class);
                    intent.putExtra(AssignmentsFragment.ASSIGNMENT_INTENT_EXTRA,mAssignmentsList.get(getAdapterPosition()).getAssignment_id());
                    intent.putExtra(AssignmentsFragment.COURSE_INTENT_EXTRA,mCourseId);
                    mContext.startActivity(intent);
                }
            });
            view_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick:viewButton ");
                    String filepath = mAssignmentsList.get(getAdapterPosition()).getAssignment_tutor_path();
                    File file = new File(filepath);
                    Log.d(TAG, "onClick: "+filepath);
                    Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri,mimetype);
                    mContext.startActivity(intent);
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
                        case R.id.cancel:
                            cancelUpload();
                            break;
                        case R.id.delete:
                            deleteFile();
                            break;
                        case R.id.download:
                            downloadFile();
                            break;
                        case R.id.resume:
                            continueUpload();
                            break;
                        case R.id.pause:
                            pauseUpload();
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
                                                    .child(mContext.getString(R.string.dbassignment_tutor_path))
                                                    .setValue(file.getAbsolutePath());
                                            Toast toast = Toast.makeText(mContext,"Download done",Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER,0,0);
                                            toast.show();
                                            final List<Assignments> assignmentsList = new ArrayList<>();
                                            FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                                                    .child(mCourseId)
                                                    .child(mContext.getString(R.string.dbassignments_node))
                                                    .orderByKey()
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                                                Log.d(TAG, "onDataChange: " + snapshot.getValue(Assignments.class));
                                                                Assignments assignment = singleSnapshot.getValue(Assignments.class);
                                                                assignmentsList.add(assignment);
                                                            }
                                                            updateAssignmentsList(assignmentsList);
                                                            Toast toast = Toast.makeText(mContext, "Upload cancelled", Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Log.d(TAG, "onCancelled: " + error.getMessage());
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.getMessage());
                                        }
                                    });
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

        private void cancelUpload() {
            FilePaths filePaths = new FilePaths();
            Uri uri = Uri.parse(mAssignmentsList.get(getAdapterPosition()).getAssignment_uri());
            StorageReference reference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name());
            final UploadTask uploadTask = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name())
                    .putFile(uri);
            if(!mUploadTask.isComplete()){
                //Upload is not complete yet, let's cancel
                Log.d(TAG, "cancelUpload: file upload canceled");
                boolean canBeCancelled = mUploadTask.cancel();
                if(canBeCancelled){
                    Log.d(TAG, "cancelUpload: "+canBeCancelled);
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                            .child(mContext.getString(R.string.dbassignment_upload_status))
                            .setValue(LectureNotesFragment.CANCELLED);
                    final List<Assignments> assignmentsList = new ArrayList<>();
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .orderByKey()
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + snapshot.getValue(Assignments.class));
                                        Assignments assignment = singleSnapshot.getValue(Assignments.class);
                                        assignmentsList.add(assignment);
                                    }
                                    updateAssignmentsList(assignmentsList);
                                    Toast toast = Toast.makeText(mContext, "Upload cancelled", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
                }
            }
            else{
                //Upload is complete, but user wanted to cancel. Let's delete the file
                reference.delete();
                // storageRef.delete(); // will delete all your files
            }
//            }
        }

        public void pauseUpload(){
            FilePaths filePaths = new FilePaths();
            Uri uri = Uri.parse(mAssignmentsList.get(getAdapterPosition()).getAssignment_uri());
            StorageReference reference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name());
            final UploadTask uploadTask = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name())
                    .putFile(uri);

            if(!mUploadTask.isComplete()){
                //Upload is not complete yet, let's cancel
                Log.d(TAG, "cancelUpload: file upload canceled");
                boolean canBePaused = mUploadTask.pause();
                if(canBePaused){
                    Log.d(TAG, "cancelUpload: "+canBePaused);
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                            .child(mContext.getString(R.string.dbassignment_upload_status))
                            .setValue(LectureNotesFragment.PAUSED);
                    final List<Assignments> assignmentsList = new ArrayList<>();
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .orderByKey()
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + snapshot.getValue(Assignments.class));
                                        Assignments assignment = singleSnapshot.getValue(Assignments.class);
                                        assignmentsList.add(assignment);
                                    }
                                    updateAssignmentsList(assignmentsList);
                                    Toast toast = Toast.makeText(mContext, "Upload paused", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
                }
            }
            else{
                reference.delete();
            }
        }

        public void deleteFile() {
            Log.d(TAG, "deleteFile: deleting file");
            FilePaths filePaths = new FilePaths();
            StorageReference reference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name());
            reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                            .removeValue();
                    deleteStudentAssignments(mAssignmentsList.get(getAdapterPosition()).getAssignment_id());
                    mAssignmentsList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    Toast toast = Toast.makeText(mContext,"File deleted",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                }
            });
        }

        public void continueUpload(){
            mProgressBar.setIndeterminate(true);
            FilePaths filePaths = new FilePaths();
            Uri uri =  Uri.parse(mAssignmentsList.get(getAdapterPosition()).getAssignment_uri());
            Uri sessionUri = Uri.parse(mAssignmentsList.get(getAdapterPosition()).getAssignment_session_uri());
            final StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name());
            mUploadTask = mStorageReference.child(filePaths.FIREBASE_ASSIGNMENTS_STORAGE)
                    .child(mCourseId)
                    .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id() + "_" + mAssignmentsList.get(getAdapterPosition()).getAssignment_name())
                    .putFile(uri,new StorageMetadata.Builder().build(),sessionUri);
            mUploadTask.resume();
            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                            .child(mContext.getString(R.string.dbassignment_url))
                            .setValue(storageReference.getDownloadUrl().toString());
//                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
//                            .child(mCourseId)
//                            .child(mContext.getString(R.string.dbassignments_node))
//                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
//                            .child(mContext.getString(R.string.dblecture_note_upload_time))
//                            .setValue(Calendar.getInstance().getTime().toString());
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .child(mAssignmentsList.get(getAdapterPosition()).getAssignment_id())
                            .child(mContext.getString(R.string.dbassignment_upload_status))
                            .setValue(LectureNotesFragment.DONE);

                    final List<Assignments> assignmentsList = new ArrayList<>();
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dbassignments_node))
                            .orderByKey()
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + snapshot.getValue(Assignments.class));
                                        Assignments assignment = singleSnapshot.getValue(Assignments.class);
                                        assignmentsList.add(assignment);
                                    }
                                    updateAssignmentsList(assignmentsList);
                                    Toast toast = Toast.makeText(mContext, "Upload success", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: "+e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d(TAG, "Upload is " + progress + "% done");
                    mProgressBar.setProgress((int) progress);
                }
            });
        }

    }
}
