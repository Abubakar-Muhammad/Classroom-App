package com.example.student.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrollCoursesListAdapter extends RecyclerView.Adapter<EnrollCoursesListAdapter.RecyclerViewHolder> implements Filterable {
    LayoutInflater mInflater;
    Context mContext;
    List<Course> mCourseList;
    List<Course> mFilterCourseList;
    int mId=-1;
    private static final String TAG = "CoursesListAdapter";

    public EnrollCoursesListAdapter(Context context, List<Course> courses){
        mInflater = LayoutInflater.from(context);
        this.mCourseList = courses;
        mContext = context;
        mFilterCourseList = new ArrayList<>(courses);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.course_item_search,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: binding data"+position);
            mId =position;
            holder.title.setText(mCourseList.get(position).getCourse_name());
            holder.code.setText(mCourseList.get(position).getCourse_code());
            holder.date.setText(mCourseList.get(position).getDate_created());
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

    private Filter coursesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Course> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mFilterCourseList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Course course : mFilterCourseList) {
                    if (course.getCourse_name().toLowerCase().contains(filterPattern)) {
                        filteredList.add(course);
                    }
                }
            }
//                for (Course course : mFilterCourseList) {
//                    final String[] creator = new String[1];
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//                    Query query = reference.child(mContext.getString(R.string.db_users))
//                            .orderByKey()
//                            .equalTo((course.getCreator_id()));
//                    query.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            Map<String, Object> user = (Map<String, Object>) snapshot.getChildren().iterator().next().getValue();
//                            creator[0] = user.get(mContext.getString(R.string.user_name)).toString();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    if (creator[0].toLowerCase().contains(filterPattern)) {
////                        filteredList.add(course);
////                    }
//                    }
//                }
//                for (Course course : mFilterCourseList){
//                    if(course.getCourse_code().toLowerCase().contains(filterPattern)){
//                        filteredList.add(course);
//                    }
//            }
//            }
            Log.d(TAG, "performFiltering: "+filteredList.size());
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            Log.d(TAG, "publishResults: "+charSequence.toString());
            Log.d(TAG, "publishResults: "+filterResults.values.toString());
            mCourseList.clear();
            mCourseList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        //        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            Log.d(TAG, "publishResults: "+filterResults.values.toString());
//            mCourseList.clear();
//            mCourseList.addAll((List) filterResults.values);
//            notifyDataSetChanged();
//        }
    };

    @Override
    public Filter getFilter() {
        return coursesFilter;
    }



    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView code;
        TextView title;
        TextView date ;
        TextView creator;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.course_code);
            title = itemView.findViewById(R.id.course_title);
            date = itemView.findViewById(R.id.date_created);
            creator = itemView.findViewById(R.id.tutor);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mId = getAdapterPosition();
                    Log.d(TAG, "onClick: starting course activity");
//                    Intent intent = new Intent(mContext, CourseActivity.class);
//                    intent.putExtra(mContext.getString(R.string.intent_course),mId);
//                    mContext.startActivity(intent);
                    if(mId!=-1){
//                        if(mCourseList.get(mId).getCreator_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                    .setCancelable(false)
                                    .setTitle("Course enrollment")
                                    .setMessage("Are you sure you want to enroll this course")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(mContext.getString(R.string.db_users))
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child(mContext.getString(R.string.user_courses))
                                                    .child(mCourseList.get(mId).getCourse_id())
                                                    .setValue(mCourseList.get(mId).getCourse_id());
                                            Intent intent = new Intent(mContext, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                    }
                }
            });

        }
    }

}
