package com.quickliftpilot.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adarsh on 27/4/18.
 */

public class GetDistance extends AsyncTask<Object,String,String> {
    String url,googleDirectionsData,duration,distance;
    //    Data data;
    SharedPreferences context;
    DatabaseReference ref;

    @Override
    protected String doInBackground(Object... objects) {
        url=(String)objects[0];
        ref=(DatabaseReference)objects[1];
        context=(SharedPreferences)objects[2];
//        data=(Data) objects[17];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl=new DownloadUrl();

        try {
            googleDirectionsData=downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String,String> directionsList=null;
        PriceParser parser=new PriceParser();
        directionsList=parser.parseDirections(s);
        duration=directionsList.get("duration");
        distance=directionsList.get("distance");

        ref.child("pickup_distance").setValue(String.valueOf(Float.valueOf(distance)/1000));
        if ((Float.valueOf(distance)/1000)>Float.valueOf(context.getString("pickupdistance",null))){
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            String date = String.format("%02d", gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
            String month = String.format("%02d", (gregorianCalendar.get(GregorianCalendar.MONTH) + 1));
            String year = String.format("%02d", gregorianCalendar.get(GregorianCalendar.YEAR));
            String formateDate = date + "-" + month + "-" + year;

            final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+context.getString("id",null)+"/"+formateDate);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot data:dataSnapshot.getChildren()){
                            String key=data.getKey();
                            Map<String,Object> map=(Map<String, Object>) data.getValue();
                            if (map.containsKey("pickup")){
                                float val=Float.valueOf(map.get("pickup").toString());
                                val+=((Float.valueOf(distance)/1000)-Float.valueOf(context.getString("pickupdistance",null)))*Float.valueOf(context.getString("pickupprice",null));
                                reference.child(key+"/pickup").setValue(String.format("%.2f",val));
                            } else {
                                float val=((Float.valueOf(distance)/1000)-Float.valueOf(context.getString("pickupdistance",null)))*Float.valueOf(context.getString("pickupprice",null));
                                reference.child(key+"/pickup").setValue(String.format("%.2f",val));
                            }
                        }
                    }
                    else {
                        String key=reference.push().getKey();
                        float val=((Float.valueOf(distance)/1000)-Float.valueOf(context.getString("pickupdistance",null)))*Float.valueOf(context.getString("pickupprice",null));
                        reference.child(key+"/pickup").setValue(String.format("%.2f",val));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
