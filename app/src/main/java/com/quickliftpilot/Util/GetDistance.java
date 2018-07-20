package com.quickliftpilot.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by adarsh on 27/4/18.
 */

public class GetDistance extends AsyncTask<Object,String,String> {
    String url,googleDirectionsData,duration,distance;
    //    Data data;
    DatabaseReference ref;

    @Override
    protected String doInBackground(Object... objects) {
        url=(String)objects[0];
        ref=(DatabaseReference)objects[1];
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
    }
}
