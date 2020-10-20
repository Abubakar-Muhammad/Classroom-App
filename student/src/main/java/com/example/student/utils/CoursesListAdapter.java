package com.example.student.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.CourseActivity;
import com.example.student.MainActivity;
import com.example.student.R;
import com.example.student.models.Course;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class CoursesListAdapter extends RecyclerView.Adapter<CoursesListAdapter.RecyclerViewHolder>  {
    LayoutInflater mInflater;
    Context mContext;
    List<Course> mCourseList;
    int mId=-1;
    private static final String TAG = "CoursesListAdapter";

    public CoursesListAdapter(Context context, List<Course> courses){
        mInflater = LayoutInflater.from(context);
        mCourseList = courses;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.course_item,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: binding data"+position);
            mId =position;
            String dateCreated = DateAndTimeConversion.newInstance().getMonthANdYear(mCourseList.get(position).getDate_created());
            holder.title.setText(mCourseList.get(position).getCourse_name());
            holder.code.setText(mCourseList.get(position).getCourse_code());
            holder.date.setText(dateCreated);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.db_users))
                                .orderByKey()
                                .equalTo(mCourseList.get(position).getCreator_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String,Object> user = (Map<String, Object>) snapshot.getChildren().iterator().next().getValue();
                String createdby = "created by"+user.get(mContext.getString(R.string.user_name));
//                String createdby = "created by"+snapshot.getChildren().iterator().next().getValue(User.class).getName();
                holder.creator.setText(createdby);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView code;
        TextView title;
        TextView date ;
        TextView creator;
        ImageView delete;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.course_code);
            title = itemView.findViewById(R.id.course_title);
            date = itemView.findViewById(R.id.date_created);
            creator = itemView.findViewById(R.id.tutor);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: starting course activity");
                    Intent intent = new Intent(mContext, CourseActivity.class);
                    intent.putExtra(mContext.getString(R.string.intent_course),mCourseList.get(getAdapterPosition()).getCourse_id());
                    mContext.startActivity(intent);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: deleting course");
                    if(mId !=-1){
                        mId = getAdapterPosition();
//                        if(mCourseList.get(mId).getCreator_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                    .setCancelable(false)
                                    .setTitle("Course deletion")
                                    .setMessage("Are you sure you want to delete this course")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//                                            reference.child(mContext.getString(R.string.dbcourses_node))
//                                                    .child(mCourseList.get(mId).getCourse_id())
//                                                    .removeValue();
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(mContext.getString(R.string.db_users))
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child(mContext.getString(R.string.user_courses))
                                                    .child(mCourseList.get(mId).getCourse_id())
                                                    .removeValue();
                                            Intent intent = new Intent(mContext,MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            mContext.startActivity(intent);
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                            builder.create().show();
//                        }
//                        else {
//                            Toast.makeText(mContext, "You did not create this course", Toast.LENGTH_SHORT).show();
//                        }

                    }
                }
            });
        }
    }

}
