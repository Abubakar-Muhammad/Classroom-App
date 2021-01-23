package com.example.projectapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.AssignmentSubmission;
import com.example.projectapplication.models.Assignments;
import com.example.projectapplication.models.User;
import com.example.projectapplication.ui.assignments.AssignmentsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubmittedAssignmentsListAdapter extends RecyclerView.Adapter<SubmittedAssignmentsListAdapter.ViewHolder> {

    private static final String TAG = "SubmittedAssignmentsLis";
    private List<AssignmentSubmission> mSubmissionList;
    private Context mContext;
    private LayoutInflater mInflater;
    private String mCourseId;

    public SubmittedAssignmentsListAdapter(List<AssignmentSubmission> submissionList, Context context,String courseId) {
        mSubmissionList = submissionList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCourseId = courseId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.submitted_assignment_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.student_id.setText(mSubmissionList.get(position).getStudent_id());
        holder.file_name.setText(mSubmissionList.get(position).getSubmission_name());
        Log.d(TAG, "onBindViewHolder: "+mSubmissionList.get(position).getUser_id());
        holder.student_name.setText(mSubmissionList.get(position).getUser_id());
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbuser_node))
                .orderByKey()
                .equalTo(mSubmissionList.get(position).getUser_id())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: getting User name");
                        Map<String,Object> objectMap = (Map<String, Object>) snapshot.getChildren().iterator().next().getValue();
                        String name = objectMap.get(mContext.getString(R.string.user_name)).toString();
                        holder.student_name.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: "+error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mSubmissionList.size();
    }

    public void updateData(List<AssignmentSubmission> submissions){
        mSubmissionList = submissions;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView file_name;
        TextView student_name;
        TextView student_id;
        TextView view_button;
        ImageView pop_up;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.filename);
            student_id = itemView.findViewById(R.id.student_id);
            student_name = itemView.findViewById(R.id.student_name);
            view_button = itemView.findViewById(R.id.view_button);
            view_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String filepath = mSubmissionList.get(getAdapterPosition()).getSubmission_location();
                    if (filepath!=null) {
                        File file = new File(filepath);
                        Log.d(TAG, "onClick: "+filepath);
                        if (file.exists()) {
                            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(uri,mimetype);
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
            pop_up = itemView.findViewById(R.id.popup);
            pop_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenu();
                }
            });
        }

        public void showMenu() {
            Log.d(TAG, "showMenu: pop up");
            PopupMenu popupMenu = new PopupMenu(mContext, pop_up);
            popupMenu.getMenuInflater().inflate(R.menu.submission_options, popupMenu.getMenu());
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

                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.db_users))
                        .orderByKey()
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d(TAG, "onDataChange: getting Users");
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                    final String userId = map.get(mContext.getString(R.string.user_id)).toString();
                                    String userType = map.get(mContext.getString(R.string.user_type)).toString();
                                    if (userType.equals("Student")) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child(mContext.getString(R.string.db_users))
                                                .child(userId)
                                                .child(mContext.getString(R.string.dbcourses_node))
                                                .child(mCourseId)
                                                .child(mContext.getString(R.string.dbassignments_submission_node))
                                                .orderByKey()
                                                .equalTo(mSubmissionList.get(getAdapterPosition()).getAssignment_id())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Log.d(TAG, "onDataChange: getting Submissions");
                                                        String status = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class).getSubmission_status();
                                                        AssignmentSubmission assignmentSubmission = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class);
                                                        if (!status.equals(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS)) {
                                                            final String filePath = fileUtil.getStorageDirForAssignmentSubmissions(assignmentSubmission.getSubmission_name());
                                                            final File file = new File(filePath);
                                                            StorageReference fileReference = FirebaseStorage.getInstance().getReference().child(filePaths.FIREBASE_ASSIGNMENTS_SUBMISSION_STORAGE)
                                                                    .child(mCourseId)
                                                                    .child(assignmentSubmission.getAssignment_id())
                                                                    .child(assignmentSubmission.getAssignment_id() + "_" + assignmentSubmission.getSubmission_name());
                                                            if (file != null) {
                                                                fileReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                        Log.d(TAG, "onSuccess: " + taskSnapshot.getTotalByteCount());
                                                                        Uri uri = Uri.fromFile(file);
                                                                        Log.d(TAG, "onSuccess: " + uri.getPath() + ", filePath" + filePath);
                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                .child(mContext.getString(R.string.db_users))
                                                                                .child(userId)
                                                                                .child(mContext.getString(R.string.dbcourses_node))
                                                                                .child(mCourseId)
                                                                                .child(mContext.getString(R.string.dbassignments_submission_node))
                                                                                .child(mSubmissionList.get(getAdapterPosition()).getAssignment_id())
                                                                                .child(mContext.getString(R.string.dbsubmission_location))
                                                                                .setValue(filePath);
                                                                        updateSubmissions();
                                                                        Toast toast = Toast.makeText(mContext, "Download done", Toast.LENGTH_SHORT);
                                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                                        toast.show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d(TAG, "onCancelled: error " + error.getMessage());
                                                    }
                                                });
                                    }
                                }
                            }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "onCancelled: error " + error.getMessage());
                                    }
                                });

            } else {
                Toast.makeText(mContext, "External Storage is not available", Toast.LENGTH_SHORT).show();
            }
        }
        public void updateSubmissions(){
            final List<AssignmentSubmission> submissionList = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.db_users))
                    .orderByKey()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "onDataChange: getting Users");
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                Map<String, Object> map = (Map<String,Object>) dataSnapshot.getValue();
                                String userId = map.get(mContext.getString(R.string.user_id)).toString();
                                String userType = map.get(mContext.getString(R.string.user_type)).toString();
                                if(userType.equals("Student")){
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(mContext.getString(R.string.db_users))
                                            .child(userId)
                                            .child(mContext.getString(R.string.dbcourses_node))
                                            .child(mCourseId)
                                            .child(mContext.getString(R.string.dbassignments_submission_node))
                                            .orderByKey()
                                            .equalTo(mSubmissionList.get(getAdapterPosition()).getAssignment_id())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    String status = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class).getSubmission_status();
                                                    AssignmentSubmission assignmentSubmission = snapshot.getChildren().iterator().next().getValue(AssignmentSubmission.class);
                                                    if(!status.equals(AssignmentsFragment.ASSIGNMENT_SUBMISSION_STATUS)){
                                                        Log.d(TAG, "onDataChange: adding submission items");
                                                        submissionList.add(assignmentSubmission);
                                                        updateData(submissionList);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d(TAG, "onCancelled: error "+error.getMessage());
                                                }
                                            });
                                }
                            }

//                        mAdapter.updateData(mAssignmentSubmissions);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "onCancelled: error "+error.getMessage());
                        }
                    });
        }
    }

}
