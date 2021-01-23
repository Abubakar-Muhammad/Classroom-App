package com.example.student.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAndTimeConversion  {
    private static final String TAG = "DateAndTimeConversion";
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

    public String get12hrAssigmentTime(String time){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(time.split(",")[0]);
        stringBuilder.append(", ");
        stringBuilder.append(time.split(",")[1].split(" ")[0]);
        stringBuilder.append(" ");
        String hr = time.split(",")[1].split(" ")[1].split(":")[0];
        String min = time.split(",")[1].split(" ")[1].split(":")[1];
        String _12hr = ((Integer.parseInt(hr) % 12)==0 ?"0"+Integer.parseInt(hr) % 12:""+Integer.parseInt(hr) % 12) + ":" + min + " " + ((Integer.parseInt(hr) >= 12) ? "PM" : "AM");
        stringBuilder.append(_12hr);
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

    public static String getDayOfWeek(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        return day;
    }

    public static boolean dueDatePassed(String due_date){
        String[] datesplit = due_date.split(",");
        String due = datesplit[1];
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date dueDate = sdf.parse(due);
            Date current_date = new Date();
            if(current_date.after(dueDate)){
                Log.d(TAG, "dueDatePassed: date"+dueDate.toString());
                return true;
            }
            else{
               return false;
            }
        } catch (ParseException e) {
            Log.d(TAG, "dueDatePassed: "+e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

}
