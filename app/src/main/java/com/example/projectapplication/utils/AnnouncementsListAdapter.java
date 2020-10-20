package com.example.projectapplication.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.Announcements;

import java.util.List;

public class AnnouncementsListAdapter extends RecyclerView.Adapter<AnnouncementsListAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Announcements> mList;


    public AnnouncementsListAdapter(Context context,List<Announcements> list){
        mContext = context;
        mList  =list;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.announcement_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(mList.get(position).getAnnouncement_title());
        holder.description.setText(mList.get(position).getAnnouncement_description());
        String time = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position).getAnnouncement_time());
        holder.time.setText(time);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
             title = itemView.findViewById(R.id.announcement_title);
             description = itemView.findViewById(R.id.announcement_description);
             time = itemView.findViewById(R.id.announcement_time);
        }
    }

}
