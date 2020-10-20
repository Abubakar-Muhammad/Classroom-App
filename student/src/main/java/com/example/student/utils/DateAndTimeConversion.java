package com.example.student.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAndTimeConversion  {
    public static DateAndTimeConversion newInstance(){
        return new DateAndTimeConversion();
    }
    public String get12hrTime(String datetime){
        String dateTimeSplit[] = datetime.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dateTimeSplit[0]);
        stringBuilder.append(", ");
        stringBuilder.append(dateTimeSplit[1]);
        stringBuilder.append(" ");
        stringBuilder.append(dateTimeSplit[2]);
        stringBuilder.append(" ");
        stringBuilder.append(dateTimeSplit[5]);
        stringBuilder.append(" ");
        String[] time = dateTimeSplit[3].split(":");
        String hr = time[0];
        String min = time[1];
//            int hrtime = Integer.parseInt(hr) % 12;
//        String _12hr = ((Integer.parseInt(hr) % 12 == 0) ?"0"+hr:""+hr) + ":" + min + " " + ((Integer.parseInt(hr) >= 12) ? "PM" : "AM");
//        String _12hr = Integer.parseInt(hr)%12 + ":" + min + " " + ((Integer.parseInt(hr) >= 12) ? "PM" : "AM");
        String _12hr = ((Integer.parseInt(hr) % 12)==0 ?"0"+Integer.parseInt(hr) % 12:""+Integer.parseInt(hr) % 12) + ":" + min + " " + ((Integer.parseInt(hr) >= 12) ? "PM" : "AM");
        stringBuilder.append(_12hr);
        String formatted_time = String.valueOf(stringBuilder);
        return formatted_time;
    }
//    dow mon dd hh:mm:ss zzz yyyy

    public String getMonthANdYear(String datetime){
        String dateTimeSplit[] = datetime.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dateTimeSplit[1]);
        stringBuilder.append(", ");
        stringBuilder.append(dateTimeSplit[5]);
        String formatted_time = String.valueOf(stringBuilder);
        return formatted_time;
    }
    public String get12hrFormattedTime(String datettime){
        StringBuilder stringBuilder = new StringBuilder();
        String[] split = datettime.split(" ");
        String[] time = split[0].split(":");
        String hr = time[0];
        String min = time[1];
        String _12hr = ((Integer.parseInt(hr) % 12)==0 ?"0"+Integer.parseInt(hr) % 12:""+Integer.parseInt(hr) % 12) + ":" + min + " " + ((Integer.parseInt(hr) >= 12) ? "PM" : "AM");
        stringBuilder.append(_12hr);
        stringBuilder.append(" ");
        stringBuilder.append(split[1]);
        String formatted_time = String.valueOf(stringBuilder);
        return formatted_time;
    }

    public String getTimestamp(){
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
//        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
//        return sdf.format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd ");
//        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

}
