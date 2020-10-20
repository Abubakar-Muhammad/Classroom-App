package com.example.projectapplication.ui.lectures_notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.LectureNotes;
import com.example.projectapplication.ui.SharedViewModel;
import com.example.projectapplication.utils.FileUtil;
import com.example.projectapplication.utils.LecturesNotesListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LectureNotesFragment extends Fragment {

    private static final String TAG = "LectureNotesFragment";
    public static final String START = "START";
    public static final String PAUSED = "PAUSED";
    public static final String DONE = "DONE";
    public static final String RESUME = "DONE";
    public static final String CANCELLED = "CANCELED";
    private SharedViewModel mSharedViewModel;
    private String mCourseId;
    public static final int PICKFILE_REQUEST_CODE = 8352;
    EditText mDescription;
    TextView mFileUploadName;
    TextView mUploadButton;
    ImageView mCancel;
    private View mDialogView;
    FloatingActionButton mFab;
    private AlertDialog mUploadDialog;
    RecyclerView mRecyclerView;
    Uri mSelectedFileUri;
    DatabaseReference mDatabaseReference;
    StorageReference mStorageReference;
    List<LectureNotes> mLectureNotesList = new ArrayList<>();
    private LecturesNotesListAdapter mAdapter;

    public static LectureNotesFragment newInstance(){
        return new LectureNotesFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lecturenotes, container, false);
        mDialogView = inflater.inflate(R.layout.lecture_note_upload_dialog,container,false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mCourseId = s;
                getLectureNotes();
                setupRecyclerView();
            }
        });
        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUploadDialog.show();
            }
        });
       setupUploadDialog();
       mDatabaseReference = FirebaseDatabase.getInstance().getReference();
       mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setupUploadDialog(){
        mDescription = mDialogView.findViewById(R.id.lecture_note_description);
        mFileUploadName = mDialogView.findViewById(R.id.file_upload);
        mUploadButton = mDialogView.findViewById(R.id.upload);
        mCancel = mDialogView.findViewById(R.id.delete);
        mFileUploadName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile();
            }
        });
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                File file = new File(mSelectedFileUri.getPath());
//                Uri uri = mSelectedFileUri;
//                String mime = uri.toString();
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(uri);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                intent.setDataAndType(uri,mime);
//                startActivity(intent);
                uploadFile();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUploadDialog.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(mDialogView);
        mUploadDialog = builder.create();
    }

    private void uploadFile() {
        FileUtil fileUtil = new FileUtil();
        String path = fileUtil.getPath(getContext(),mSelectedFileUri);
        File file = new File(mSelectedFileUri.getPath());
        LectureNotes lectureNote = new LectureNotes();
        lectureNote.setLecture_note_name(file.getName());
        lectureNote.setLecture_note_uri(mSelectedFileUri.toString());
//        lectureNote.setLecture_note_path(file.getAbsolutePath());
        lectureNote.setLecture_note_tutor_file_path(path);
        lectureNote.setLecture_note_description(mDescription.getText().toString());
        lectureNote.setLecture_note_upload_status(START);
        String lectureNoteId = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dblecture_notes_node)).push().getKey();
        lectureNote.setLecture_note_id(lectureNoteId);
        mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dblecture_notes_node))
                .child(lectureNoteId)
                .setValue(lectureNote)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: "+task.toString()+",task success:"+task.isSuccessful());
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(),"File upload is starting",Toast.LENGTH_SHORT).show();
                            mUploadDialog.dismiss();
                            getLectureNotes();
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

    private void setupRecyclerView(){
        mRecyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.scrollToPosition(mLectureNotesList.size()-1);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new LecturesNotesListAdapter(getContext(),mLectureNotesList,mCourseId);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getLectureNotes(){
        Log.d(TAG, "getLectureNotes: getting lecture notes");
        mLectureNotesList.clear();
//        mLectureNotesList = new ArrayList<>();
        Query query = mDatabaseReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dblecture_notes_node));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()){
                    LectureNotes lectureNotes = singleSnapshot.getValue(LectureNotes.class);
                    mLectureNotesList.add(lectureNotes);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            Log.d(TAG, "onActivityResult: "+uri);
            mSelectedFileUri = uri;
            mFileUploadName.setText(uri.getPath());
            File file = new File(uri.getPath());
            Log.d(TAG, "onActivityResult: file path"+file.toString());
        }
    }
}