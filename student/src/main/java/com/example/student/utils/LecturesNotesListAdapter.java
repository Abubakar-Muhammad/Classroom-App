package com.example.student.utils;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.R;
import com.example.student.models.LectureNotes;
import com.example.student.ui.lecture_notes.LectureNotesFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LecturesNotesListAdapter extends RecyclerView.Adapter<LecturesNotesListAdapter.ViewHolder> {

    private static final String TAG = "LecturesNotesListAdapte";
    Context mContext;
    LayoutInflater mInflater;
    List<LectureNotes> mLectureNotesList;
    StorageReference mStorageReference;
    private String mCourseId;

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
        if (mLectureNotesList.get(position).getLecture_note_upload_status().equals(LectureNotesFragment.DONE)) {
            holder.description.setText(mLectureNotesList.get(position).getLecture_note_description());
            holder.filename.setText(mLectureNotesList.get(position).getLecture_note_name());
            String uploadTime = mLectureNotesList.get(position).getLecture_note_upload_time();
            if(uploadTime != null) {
                holder.uploadTime.setText(DateAndTimeConversion.newInstance().get12hrTime(uploadTime));
            }
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

    public void getLectureNotes(){
        FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(mContext.getString(R.string.dblecture_notes_node))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<LectureNotes> notesList = new ArrayList<>();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            LectureNotes lectureNote = dataSnapshot.getValue(LectureNotes.class);
                            notesList.add(lectureNote);
                        }
                        updateLectureNotes(notesList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView filename;
        TextView viewButton;
        ProgressBar downloadProgress;
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
                    String filePath = mLectureNotesList.get(getAdapterPosition()).getLecture_note_student_file_path();
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
            downloadProgress = itemView.findViewById(R.id.progressBar);
        }

        public void showMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, pop_menu);
            popupMenu.getMenuInflater().inflate(R.menu.lecture_note_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.download:
                            try {
                                downloadFile();
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                // ...handle the exception.
                            }
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
                FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbcourses_node))
                        .child(mCourseId)
                        .child(mContext.getString(R.string.dblecture_notes_node))
                        .orderByKey()
                        .equalTo(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                LectureNotes lectureNotes;
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
                                                    .child(mContext.getString(R.string.dblecture_note_student_file_path))
                                                    .setValue(file.getAbsolutePath());
                                            Toast toast = Toast.makeText(mContext,"Download done",Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER,0,0);
                                            toast.show();
                                            downloadProgress.setIndeterminate(false);
                                           getLectureNotes();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.getMessage());
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                                            Log.d(TAG, "onProgress: "+snapshot.getTotalByteCount());
                                        }
                                    });
//                                    final long ONE_MEGABYTE = 100 * 1024 * 1024;
//                                    fileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                        @Override
//                                        public void onSuccess(byte[] bytes) {
//                                            // Data for "images/island.jpg" is returns, use this as needed
//                                            try {
//                                                FileOutputStream fos = new FileOutputStream(file);
//                                                fos.write(bytes);
//                                                fos.close();
//                                                Log.d(TAG, "onSuccess: filePath"+filePath);
//                                            FirebaseDatabase.getInstance().getReference()
//                                                    .child(mContext.getString(R.string.dbcourses_node))
//                                                    .child(mCourseId)
//                                                    .child(mContext.getString(R.string.dblecture_notes_node))
//                                                    .child(mLectureNotesList.get(getAdapterPosition()).getLecture_note_id())
//                                                    .child(mContext.getString(R.string.dblecture_note_student_file_path))
//                                                    .setValue(file.getAbsolutePath());
//                                            Toast toast = Toast.makeText(mContext,"Download done",Toast.LENGTH_SHORT);
//                                            toast.setGravity(Gravity.CENTER,0,0);
//                                            toast.show();
//                                            downloadProgress.setIndeterminate(false);
//
//                                            } catch (Exception e) {
//                                                Log.d(TAG, "onSuccess: "+e.getMessage()+", "+e.getCause());
//                                                e.printStackTrace();
//                                            }
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception exception) {
//                                            // Handle any errors
//                                            Log.d(TAG, "onFailure: "+exception.getMessage()+", "+exception.getCause());
//                                        }
//                                    });
////                                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
////                                        @Override
////                                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
////                                            downloadProgress.setIndeterminate(true);
////                                        }
////                                    });
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
