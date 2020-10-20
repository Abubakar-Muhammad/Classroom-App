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
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.LectureNotes;
import com.example.projectapplication.ui.lectures_notes.LectureNotesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class LecturesNotesListAdapter extends RecyclerView.Adapter<LecturesNotesListAdapter.ViewHolder> {

    private static final String TAG = "LecturesNotesListAdapte";
    Context mContext;
    LayoutInflater mInflater;
    List<LectureNotes> mLectureNotesList;
    private int[] mDrawables = {R.drawable.ic_baseline_pause_24, R.drawable.ic_baseline_stop_24,
            R.drawable.ic_baseline_cloud_upload_24, R.drawable.ic_baseline_play_arrow_24};
    StorageReference mStorageReference;
    private String mCourseId;
    private UploadTask mUploadTask;

    public LecturesNotesListAdapter(Context context, List<LectureNotes> lectureNotesList, String courseId) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLectureNotesList = lectureNotesList;
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mCourseId = courseId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.lecture_note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.description.setText(mLectureNotesList.get(position).getLecture_note_description());
        holder.filename.setText(mLectureNotesList.get(position).getLecture_note_name());
        String uploadTime = mLectureNotesList.get(position).getLecture_note_upload_time();
        if(uploadTime != null) {
            holder.uploadTime.setText(DateAndTimeConversion.newInstance().get12hrTime(uploadTime));
        }
        if (mLectureNotesList.get(position).getLecture_note_upload_status().equals(LectureNotesFragment.START)) {
                holder.uploadProgress.setIndeterminate(true);
                Log.d(TAG, "onBindViewHolder: starting file upload ");
                FilePaths filePaths = new FilePaths();
                File file = new File(mLectureNotesList.get(position).getLecture_note_uri());
                Uri uri = Uri.parse(mLectureNotesList.get(position).getLecture_note_uri());
                final StorageReference fileReference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                        .child(mCourseId)
                        .child(mLectureNotesList.get(position).getLecture_note_id() + "_" + mLectureNotesList.get(position).getLecture_note_name());
            mUploadTask = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                   .child(mCourseId)
                   .child(mLectureNotesList.get(position).getLecture_note_id() + "_" + mLectureNotesList.get(position).getLecture_note_name())
                   .putFile(uri);
                mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: " + taskSnapshot.getBytesTransferred());
                        Task<Uri> uriTask = fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                                        .child(mCourseId)
                                        .child(mContext.getString(R.string.dblecture_notes_node))
                                        .child(mLectureNotesList.get(position).getLecture_note_id())
                                        .child(mContext.getString(R.string.dblecture_note_url))
                                        .setValue(uri.toString());
                                FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                                        .child(mCourseId)
                                        .child(mContext.getString(R.string.dblecture_notes_node))
                                        .child(mLectureNotesList.get(position).getLecture_note_id())
                                        .child(mContext.getString(R.string.dblecture_note_upload_time))
                                        .setValue(Calendar.getInstance().getTime().toString());
                                Toast toast = Toast.makeText(mContext, "Upload done", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                            }
                        });
                        FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                                .child(mCourseId)
                                .child(mContext.getString(R.string.dblecture_notes_node))
                                .child(mLectureNotesList.get(position).getLecture_note_id())
                                .child(mContext.getString(R.string.dblecture_note_upload_status))
                                .setValue(LectureNotesFragment.DONE);
                        holder.uploadProgress.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                Log.d(TAG, "Upload is " + progress + "% done");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    holder.uploadProgress.setProgress((int) progress, true);
                                }
                                Uri sessionUri = snapshot.getUploadSessionUri();
                                Log.d(TAG, "onProgress: " + sessionUri);
                                if (sessionUri != null) {
                                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                                            .child(mCourseId)
                                            .child(mContext.getString(R.string.dblecture_notes_node))
                                            .child(mLectureNotesList.get(position).getLecture_note_id())
                                            .child(mContext.getString(R.string.dblecture_note_session_uri))
                                            .setValue(sessionUri.toString());
                                }
                            }
                        });
        } else if (mLectureNotesList.get(position).getLecture_note_upload_status().equals(LectureNotesFragment.DONE)) {
            holder.uploadProgress.setVisibility(View.GONE);
        } else if (mLectureNotesList.get(position).getLecture_note_upload_status().equals(LectureNotesFragment.CANCELLED)) {
//                Log.d(TAG, "onBindViewHolder: cancelled ");
//                holder.uploadProgress.setVisibility(View.GONE);
                holder.uploadProgress.setProgressDrawable(mContext.getDrawable(mDrawables[2]));
                holder.uploadProgress.setIndeterminate(false);
                String value = mLectureNotesList.get(position).getLecture_note_description() + "\n file upload cancelled, you can resume it or delete it";
                Log.d(TAG, "onBindViewHolder: " + value);
//                holder.description.setText(value);
            }
//                holder.description.setText(mLectureNotesList.get(position).getLecture_note_description()+"\n file upload cancelled, you can resume it or delete it");
        else if (mLectureNotesList.get(position).getLecture_note_upload_status().equals(LectureNotesFragment.PAUSED)) {
            holder.uploadProgress.setProgressDrawable(mContext.getDrawable(mDrawables[3]));
            holder.uploadProgress.setIndeterminate(false);
        }
    }

    @Override
    public int getItemCount() {
        return mLectureNotesList.size();
    }

    public void updateLectureNotes(List<LectureNotes> lectureNotes){
        if(mLectureNotesList != null){
            mLectureNotesList.clear();
        }
        mLectureNotesList = lectureNotes;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView filename;
        TextView viewButton;
        ProgressBar uploadProgress;
        ImageView pop_menu;
        TextView uploadTime;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            filename = itemView.findViewById(R.id.file_name);
            viewButton = itemView.findViewById(R.id.view_button);
            pop_menu = itemView.findViewById(R.id.popupmenu);
            uploadTime = itemView.findViewById(R.id.upload_time);
            pop_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenu();
                }
            });

            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: viewButton");
                    String filePath = mLectureNotesList.get(getAdapterPosition()).getLecture_note_tutor_file_path();
                    File file = new File(filePath);
                    Log.d(TAG, "onClick: "+filePath);
                    Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setData(uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri,mimetype);
                    mContext.startActivity(intent);
//                    FirebaseDatabase.getInstance().getReference()
//                            .child(mContext.getString(R.string.dbcourses_node))
//                            .child(mCourseId)
//                            .child(mContext.getString(R.string.dblecture_notes_node))
//                            .orderByKey()
//                            .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
////                            .child(mContext.getString(R.string.dblecture_note_path))
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(LectureNotes.class));
//                                    LectureNotes lectureNotes = snapshot.getChildren().iterator().next().getValue(LectureNotes.class);
//                                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() +"/Classroom/Documents/"+lectureNotes.getLecture_note_name());
////                                    Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary"+lectureNotes.getLecture_note_name());
//                                    String mime = uri.toString();
//                                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setData(uri);
////                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                                  intent.setDataAndType(uri,mime);
//                                    mContext.startActivity(intent);
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                    Log.d(TAG, "onCancelled: "+error.getMessage());
//                                }
//                            });
                }
            });
            uploadProgress = itemView.findViewById(R.id.progressBar);
        }

        public void showMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, pop_menu);
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
                        .child(mContext.getString(R.string.dblecture_notes_node))
                        .orderByKey()
                        .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                LectureNotes lectureNotes = new LectureNotes();
                                Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(LectureNotes.class));
                                lectureNotes = snapshot.getChildren().iterator().next().getValue(LectureNotes.class);
                                final String filePath = fileUtil.getStorageDir(lectureNotes.getLecture_note_name());
                                final File file = new File(filePath);
                                StorageReference fileReference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                                        .child(mCourseId)
                                        .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name());
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
                                                    .child(mContext.getString(R.string.dblecture_notes_node))
                                                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                                                    .child(mContext.getString(R.string.dblecture_note_tutor_file_path))
                                                    .setValue(file.getAbsolutePath());
                                            Toast toast = Toast.makeText(mContext,"Download done",Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER,0,0);
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
            Uri uri = Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_uri());
//            Uri sessionUri = Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_session_uri());
            StorageReference reference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name());
            final UploadTask uploadTask = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name())
                    .putFile(uri);
//            .putFile(uri,new StorageMetadata.Builder().build(),sessionUri);
//            if (uploadTask.isInProgress()) {
            if(!mUploadTask.isComplete()){
                //Upload is not complete yet, let's cancel
                Log.d(TAG, "cancelUpload: file upload canceled");
                boolean canBeCancelled = mUploadTask.cancel();
                if(canBeCancelled){
                    Log.d(TAG, "cancelUpload: "+canBeCancelled);
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .child(mContext.getString(R.string.dblecture_note_upload_status))
                            .setValue(LectureNotesFragment.CANCELLED);
                    final List<LectureNotes> lectureNotesList = new ArrayList<>();
                    LectureNotes lectureNotes;
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .orderByKey()
//                            .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    LectureNotes lectureNotes = new LectureNotes();
//                                    Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(LectureNotes.class));
//                                    lectureNotes = snapshot.getChildren().iterator().next().getValue(LectureNotes.class);
//                                    lectureNotesList.add(lectureNotes);
//                                    notifyItemChanged(getAdapterPosition());
                                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + snapshot.getValue(LectureNotes.class));
                                        LectureNotes lectureNotes = singleSnapshot.getValue(LectureNotes.class);
                                        lectureNotesList.add(lectureNotes);
                                    }
                                    updateLectureNotes(lectureNotesList);
                                    Toast toast = Toast.makeText(mContext, "Upload cancelled", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
//                                    uploadProgress.setProgressDrawable(mContext.getDrawable(mDrawables[2]));
//                                    uploadProgress.setIndeterminate(false);
                                    }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
                }
//                uploadTask.addOnCanceledListener(new OnCanceledListener() {
//                    @Override
//                    public void onCanceled() {
//                        Log.d(TAG, "onCanceled: canceled ");
////                        uploadProgress.setVisibility(View.INVISIBLE);
//
//                    }
//                });
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
            Uri uri = Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_uri());
//            Uri sessionUri = Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_session_uri());
            StorageReference reference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name());
            final UploadTask uploadTask = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name())
                    .putFile(uri);
//            .putFile(uri,new StorageMetadata.Builder().build(),sessionUri);
//            if (uploadTask.isInProgress()) {
            if(!mUploadTask.isComplete()){
                //Upload is not complete yet, let's cancel
                Log.d(TAG, "cancelUpload: file upload canceled");
                boolean canBePaused = mUploadTask.pause();
                if(canBePaused){
                    Log.d(TAG, "cancelUpload: "+canBePaused);
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .child(mContext.getString(R.string.dblecture_note_upload_status))
                            .setValue(LectureNotesFragment.PAUSED);
                    final List<LectureNotes> lectureNotesList = new ArrayList<>();
                    LectureNotes lectureNotes;
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .orderByKey()
//                            .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    LectureNotes lectureNotes = new LectureNotes();
//                                    Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(LectureNotes.class));
//                                    lectureNotes = snapshot.getChildren().iterator().next().getValue(LectureNotes.class);
//                                    lectureNotesList.add(lectureNotes);
//                                    notifyItemChanged(getAdapterPosition());
                                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + snapshot.getValue(LectureNotes.class));
                                        LectureNotes lectureNotes = singleSnapshot.getValue(LectureNotes.class);
                                        lectureNotesList.add(lectureNotes);
                                    }
                                    updateLectureNotes(lectureNotesList);
                                    Toast toast = Toast.makeText(mContext, "Upload paused", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
//                                    uploadProgress.setProgressDrawable(mContext.getDrawable(mDrawables[2]));
//                                    uploadProgress.setIndeterminate(false);
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
            StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id() + "_" + mLectureNotesList.get(getAdapterPosition()).getLecture_note_name());
            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .removeValue();
                    mLectureNotesList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    Toast toast = Toast.makeText(mContext,"File deleted",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    LecturesNotesListAdapter adapter = new LecturesNotesListAdapter(mContext,mLectureNotesList,mCourseId);
//                    adapter.notifyDataSetChanged();
//                    notifyItemRemoved(mLectureNotesList.size()-1);
//                    notifyItemRemoved(getAdapterPosition());
                }
            });
        }


        public void continueUpload(){
            uploadProgress.setIndeterminate(true);
            FilePaths filePaths = new FilePaths();
            Uri uri =  Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_uri());
            Uri sessionUri = Uri.parse(mLectureNotesList.get(getAdapterPosition()).getLecture_note_session_uri());
            final StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id()+"_"+mLectureNotesList.get(getAdapterPosition()).getLecture_note_name());
            mUploadTask = mStorageReference.child(filePaths.FIREBASE_LECTURE_NOTES_STORAGE)
                    .child(mCourseId)
                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id()+"_"+mLectureNotesList.get(getAdapterPosition()).getLecture_note_name())
                    .putFile(uri,new StorageMetadata.Builder().build(),sessionUri);
            mUploadTask.resume();
            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .child(mContext.getString(R.string.dblecture_note_url))
                            .setValue(storageReference.getDownloadUrl().toString());
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .child(mContext.getString(R.string.dblecture_note_upload_time))
                            .setValue(Calendar.getInstance().getTime().toString());
                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .child(mContext.getString(R.string.dblecture_note_upload_status))
                            .setValue(LectureNotesFragment.DONE);

                    FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                            .child(mCourseId)
                            .child(mContext.getString(R.string.dblecture_notes_node))
                            .orderByKey()
//                            .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<LectureNotes> notesList = new ArrayList<>();
//                                    LectureNotes lectureNotes;
//                                    Log.d(TAG, "onDataChange: " + snapshot.getChildren().iterator().next().getValue(LectureNotes.class));
//                                    lectureNotes = snapshot.getChildren().iterator().next().getValue(LectureNotes.class);
//                                    mLectureNotesList.set(getAdapterPosition(),lectureNotes);
//                                    notifyDataSetChanged();
                                    for(DataSnapshot singleSnapshot: snapshot.getChildren()){
                                        Log.d(TAG, "onDataChange: " + singleSnapshot.getValue(LectureNotes.class));
                                        LectureNotes lectureNotes = singleSnapshot.getValue(LectureNotes.class);
                                        notesList.add(lectureNotes);
                                    }
                                    updateLectureNotes(notesList);
//                                    notifyItemChanged(getAdapterPosition());
                                    Toast toast = Toast.makeText(mContext, "Upload successful", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
//                                    uploadProgress.setProgressDrawable(mContext.getDrawable(mDrawables[2]));
//                                    uploadProgress.setIndeterminate(false);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
//                uploadTask.addOnCanceledListener(new OnCanceledListener() {
//                    @Override
//                    public void onCanceled() {
//                        Log.d(TAG, "onCanceled: canceled ");
////                        uploadProgress.setVisibility(View.INVISIBLE);
//
//                    }
//                });

//                    uploadProgress.setIndeterminate(false);
//                    uploadProgress.setVisibility(View.GONE);
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
                    uploadProgress.setProgress((int) progress);
                }
            });
        }

}
}
