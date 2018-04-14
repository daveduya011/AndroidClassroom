package com.isidoreofseville.androidclassroom;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dave on 1/2/2018.
 */

public class DateRetriever {

    private static String dateToday, time;

    private static boolean connected;

    private static int day, year, month;

    public DateRetriever(final Context context) {

        //Get the server date as soon as possible
        getDate("MM/dd/yyyy");
        returnSystemDate("MM/dd/yyyy");
        retrieveConnectionState();


    }

    private void retrieveConnectionState(){
        //Retrieve connection status
        Database.ref_connection.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isConnected = dataSnapshot.getValue(Boolean.class);
                if (isConnected) {
                    //Get server date if there is connection
                    getDate("MM/dd/yyyy");
                    DateRetriever.connected = true;
                    System.out.println("Connected");
                    //MainActivity.refreshPosts();
                } else {
                    //Get system date if no connection
                    DateRetriever.connected = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void returnSystemDate(String format){
        long timeStamp = (long) (System.currentTimeMillis());
        Date date = new Date(timeStamp);

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        dateToday = dateFormat.format(date);
        time = timeFormat.format(date);
        day = Integer.parseInt(dayFormat.format(date));
        month = Integer.parseInt(monthFormat.format(date));
        year = Integer.parseInt(yearFormat.format(date));
        System.out.println("System date " + dateToday + " " + time);
    }

    public void getDate(final String format){
        final String[] currentDate = new String[1];

            Database.ref_time.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double offset = dataSnapshot.getValue(Double.class);
                    long estimatedServerTimeMs = (long) (System.currentTimeMillis() + offset);

                    Date date = new Date(estimatedServerTimeMs);
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                    currentDate[0] = dateFormat.format(date);
                    dateToday = currentDate[0];
                    currentDate[0] = timeFormat.format(date);
                    time = currentDate[0];
                    System.out.println("Server Time: " + currentDate[0]);
                    currentDate[0] = dayFormat.format(date);
                    day = Integer.parseInt(currentDate[0]);
                    currentDate[0] = monthFormat.format(date);
                    month = Integer.parseInt(currentDate[0]);
                    currentDate[0] = yearFormat.format(date);
                    year = Integer.parseInt(currentDate[0]);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Date Cancelled: " + databaseError.getMessage());
                }
            });

    }

    public interface listener {
        void newMethod();
    }

    public static String getDateToday() {
        return dateToday;
    }

    public static int getDay() {
        return day;
    }

    public static int getYear() {
        return year;
    }

    public static int getMonth() {
        return month;
    }

    public static String getTime() {
        return time;
    }

    public static boolean isConnected() {
        return connected;
    }
}
