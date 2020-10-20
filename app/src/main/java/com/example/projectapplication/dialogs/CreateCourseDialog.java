package com.example.projectapplication.dialogs;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projectapplication.MainActivity;
import com.example.projectapplication.R;
import com.example.projectapplication.models.ChatMessage;
import com.example.projectapplication.models.Course;
import com.example.projectapplication.models.User;
import com.example.projectapplication.utils.DateAndTimeConversion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateCourseDialog extends DialogFragment {

    private static final String TAG = "CreateCourseDialog";
    private TextView mCourseCode;
    private TextView mCourseTitle;
    private TextView mCreate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCourse(mCourseCode.getText().toString(), mCourseTitle.getText().toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_course_dialog,container,false);
        mCourseCode = view.findViewById(R.id.course_code);
        mCourseTitle = view.findViewById(R.id.course_title);
        mCreate = view.findViewById(R.id.create_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void createCourse(final String code, String title){
        Log.d(TAG, "createCourse: creating course"+code);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String course_id = reference.child(getString(R.string.dbcourses_node)).push().getKey();

        List<User> userList = new ArrayList<User>();
        User user = new User();
        user.setId(FirebaseAuth.getInstance().getUid());
        userList.add(user);

        Course course = new Course();
        course.setCourse_code(code);
        course.setCourse_name(title);
        course.setCreator_id(FirebaseAuth.getInstance().getUid());
        course.setDate_created(Calendar.getInstance().getTime().toString());
        course.setCourse_id(course_id);
//        course.setUsers(userList);
        reference.child(getString(R.string.dbcourses_node))
                .child(course_id)
                .setValue(course)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: created course"+course_id);
                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.user_courses))
                                    .child(course_id)
                                    .setValue(course_id);
                            String messagedId = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbcourses_node))
                                    .child(course_id)
                                    .child(getString(R.string.dbchats_node))
                                    .push().getKey();
                            ChatMessage message = new ChatMessage();
                            message.setMessage("Welcome to the chat");
                            message.setMessage_timestamp(DateAndTimeConversion.newInstance().getTimestamp());
                            message.setMessage_id(messagedId);
                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbcourses_node))
                                    .child(course_id)
                                    .child(getString(R.string.dbchats_node))
                                    .child(messagedId)
                                    .setValue(message);

                            Toast.makeText(getActivity(), "Course created successfully", Toast.LENGTH_SHORT).show();
                            ((MainActivity)getActivity()).init();
                            getDialog().dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
            }
        });
    }

}
