package com.example.projectapplication.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.Announcements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnnouncementsListAdapter extends RecyclerView.Adapter<AnnouncementsListAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Announcements> mList;
    private static final String TAG = "AnnouncementsListAdapte";

    public final static int DATE_TYPE = 0;
    public final static int TIME_TYPE =1;


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
        if (position != 0) {
            Log.d(TAG, "onBindViewHolder: date: "+mList.get(position).getAnnouncement_time()+" previous: "+mList.get(position-1).getAnnouncement_time());
            processDate(holder.date, mList.get(position).getAnnouncement_time(), mList.get(position - 1).getAnnouncement_time(), false);
        } else {
            processDate(holder.date, mList.get(position).getAnnouncement_time(), null, true);
        }
        holder.title.setText(mList.get(position).getAnnouncement_title());
        holder.description.setText(mList.get(position).getAnnouncement_description());
        String time = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position).getAnnouncement_time());
        Date date = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        try {
            date = format.parse(mList.get(position).getAnnouncement_time());
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm a");
        holder.time.setText(timeformat.format(calendar.getTime()));
    }
//        if(holder.getItemViewType() == TIME_TYPE){
//            ViewHolder viewHolder = (ViewHolder) holder;
//            viewHolder.title.setText(mList.get(position).getAnnouncement_title());
//            viewHolder.description.setText(mList.get(position).getAnnouncement_description());
//            String time = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position).getAnnouncement_time());
//            viewHolder.time.setText(time.split(" ")[3]+time.split(" ")[4]);
//        }
//        else if(holder.getItemViewType() == DATE_TYPE){
//            mList.get(position).setNewDate(true);
//
//            String time = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position).getAnnouncement_time());
//            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
//            Date date = null;
//            try {
//                date = format.parse(mList.get(position).getAnnouncement_time());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//            String day = sdf.format(date);
//            DateViewHolder dateViewHolder = (DateViewHolder) holder;
////            dateViewHolder.bindData(position,day);
//        holder.title.setText(mList.get(position).getAnnouncement_title());
//        holder.description.setText(mList.get(position).getAnnouncement_description());
//        String time = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position).getAnnouncement_time());
//        String previousTime = DateAndTimeConversion.newInstance().get12hrTime(mList.get(position-1).getAnnouncement_time());
//        Date date = DateAndTimeConversion.dateConversion(time);
//        Date previousDate = DateAndTimeConversion.dateConversion(previousTime);
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        String day = sdf.format(date);
//        String previous =  sdf.format(previousDate);

    @Override
    public int getItemCount() {
        return mList.size();
    }



     class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView time;
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.announcement_title);
            description = itemView.findViewById(R.id.announcement_description);
            time = itemView.findViewById(R.id.announcement_time);
            date = itemView.findViewById(R.id.date);
        }

    }


    private void processDate(@NonNull TextView tv, String dateAPIStr
            , String dateAPICompareStr
            , boolean isFirstItem) {

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        if (isFirstItem) {
            //first item always got date/today to shows
            //and overkill to compare with next item flow
            Date dateFromAPI = null;
            try {
                dateFromAPI = format.parse(dateAPIStr);
                if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("today");
                else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                    tv.setText("yesterday");
                else tv.setText(f.format(dateFromAPI));
                tv.setIncludeFontPadding(false);
                tv.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                e.printStackTrace();
                tv.setVisibility(View.GONE);
            }
        } else {
            if (!dateAPIStr.equalsIgnoreCase(dateAPICompareStr)) {
                try {
                    Date dateFromAPI = format.parse(dateAPIStr);
                    Date dateAPICompare = format.parse(dateAPICompareStr);
                    String datevalue = f.format(dateFromAPI);
                    String datecompare = f.format(dateAPICompare);
                    Date dateValue = f.parse(datevalue);
                    Date dateCompare = f.parse(datecompare);
                    if(dateValue.compareTo(dateCompare) !=0){
                        if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("today");
                        else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                            tv.setText("yesterday");
                        else tv.setText(f.format(dateFromAPI));
                        tv.setIncludeFontPadding(false);
                        tv.setVisibility(View.VISIBLE);
                    }
                    else {
                        tv.setVisibility(View.GONE);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    tv.setVisibility(View.GONE);
                }
            } else {
                tv.setVisibility(View.GONE);
            }
        }

    }

}
