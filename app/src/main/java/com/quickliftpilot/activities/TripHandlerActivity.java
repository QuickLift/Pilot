package com.quickliftpilot.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.SequenceModel;
import com.quickliftpilot.services.RouteArrangeService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Stack;

public class TripHandlerActivity extends AppCompatActivity {

    DatabaseReference db;
    SharedPreferences log_id,ride_info;
    SharedPreferences.Editor editor;
    GPSTracker gps;
    double latitude,longitude;
    Stack<SequenceModel> stack;
    NotificationManager notificationManager;
    public static Activity TripHandler=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_handler);

        TripHandler=this;

        Log.v("TAG","TripHandlerActivity");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getSharedPreferences("Login", MODE_PRIVATE);
        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
        editor = log_id.edit();
        db=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+ride_info.getString("customer_id",null));

        gps = new GPSTracker(this);

//        Toast.makeText(this, "Trip Handler Activity", Toast.LENGTH_SHORT).show();
        Intent extras = getIntent();
        String value = extras.getStringExtra("value");
//        Toast.makeText(this, "value1 = " + value, Toast.LENGTH_SHORT).show();

        notificationManager = notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancel(0);
        if (value.equalsIgnoreCase("cancel")) {
            cancelTrip();
        } else if (value.equalsIgnoreCase("confirm")) {
            stack = new SequenceStack().getStack();
//            Log.i("TAG","stack size : "+stack.size());
            confirmTrip();
        }
    }

    public void cancelTrip() {
        if (gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
//            Toast.makeText(this, "Trip Canceled", Toast.LENGTH_SHORT).show();
            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/" + ride_info.getString("customer_id",null));
            dref.child("resp").setValue("Reject");
            dref.child("driver").setValue(log_id.getString("id",null));
            String userId= log_id.getString("id",null);
            if (log_id.getString("ride",null).equals("")) {
//                Toast.makeText(TripHandlerActivity.this, "Available", Toast.LENGTH_SHORT).show();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/" + log_id.getString("type", null));
//                ref.removeValue();
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(latitude, longitude));

                SharedPreferences pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
                SharedPreferences.Editor editor1=pref.edit();
                editor1.putBoolean("status", true);
                editor1.commit();
            }

//            DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+userId);
//            delref.removeValue();
            db.removeValue();
        }

        GregorianCalendar gregorianCalendar=new GregorianCalendar();
        String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
        String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
        String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
        final String formateDate = date+"-"+month+"-"+year;

        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
        driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String,Object>)data.getValue();
                    int cancel = Integer.parseInt(map.get("reject").toString());
                    cancel = cancel+1;
                    String key = data.getKey();
                    try {
                        driver_acc.child(key).child("reject").setValue(Integer.toString(cancel));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (MapActivity.fa!=null){
            MapActivity.fa.finish();
        }
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        finish();
    }

    public void confirmTrip() {
//        DatabaseReference customerReq = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("id", null));

        SequenceModel model = new SequenceModel();
        model.setId(ride_info.getString("customer_id",null));
        model.setName(ride_info.getString("name",null));
        model.setType("pick");
        model.setOtp(ride_info.getString("otp",null));
        if (!ride_info.getString("seat",null).equals("full"))
            model.setSeat(Integer.parseInt(ride_info.getString("seat",null)));
        model.setAddress(ride_info.getString("source",null));
        model.setLat(Double.parseDouble(ride_info.getString("st_lat",null)));
        model.setLng(Double.parseDouble(ride_info.getString("st_lng",null)));
        model.setLatLng(new LatLng(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null))));

        SequenceModel dropModel = new SequenceModel();
        dropModel.setId(ride_info.getString("customer_id",null));
        dropModel.setName(ride_info.getString("name",null));
        dropModel.setType("drop");
        dropModel.setOtp(ride_info.getString("otp",null));
        if (!ride_info.getString("seat",null).equals("full"))
            dropModel.setSeat(Integer.parseInt(ride_info.getString("seat",null)));
        dropModel.setAddress(ride_info.getString("destination",null));
        dropModel.setLat(Double.parseDouble(ride_info.getString("en_lat",null)));
        dropModel.setLng(Double.parseDouble(ride_info.getString("en_lng",null)));
        dropModel.setLatLng(new LatLng(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null))));

        stack.push(dropModel);
//        Log.i("TAG","stack size : "+stack.size());
        stack.push(model);
//        Log.i("TAG","stack size : "+stack.size());

        startService(new Intent(this, RouteArrangeService.class));

        db.child("accept").setValue(1);
        DatabaseReference response = FirebaseDatabase.getInstance().getReference("Response/"+ride_info.getString("customer_id",null));
        response.child("resp").setValue("Accept");
        response.child("driver").setValue(log_id.getString("id",null));

        int seat=0;
        if (log_id.contains("seats")){
            if (!log_id.getString("seats",null).equals("full"))
                seat=Integer.parseInt(log_id.getString("seats",null));
            else
                seat=0;
        }
        if (!ride_info.getString("seat",null).equals("full")) {
            seat = seat + Integer.parseInt(ride_info.getString("seat", null));
            DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/" + log_id.getString("type", null) + "/" + log_id.getString("id", null));
            seat_data.child("seat").setValue(String.valueOf(seat));
            editor.putString("seats",String.valueOf(seat));
        }
        else {
            DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/" + log_id.getString("type", null) + "/" + log_id.getString("id", null));
            seat_data.child("seat").setValue("full");
            editor.putString("seats","full");
        }

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
        ref.removeValue();
//        Log.v("TAG","No of seats "+log_id.getString("seats",null)+" , "+seat+" , "+ride_info.getString("seat",null));
        editor.putString("ride", "ride");
        editor.commit();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
        SharedPreferences.Editor editor1=pref.edit();
        editor1.putBoolean("status", true);
        editor1.commit();

        getCurrentLocation();

//        GregorianCalendar gregorianCalendar=new GregorianCalendar();
//        String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
//        String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
//        String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
//        final String formateDate = year+"-"+month+"-"+date;
//
//        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
//        driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()){
//                    Map<String,Object> map = (Map<String,Object>)data.getValue();
//                    int confirm = Integer.parseInt(map.get("book").toString());
//                    int earn = Integer.parseInt(map.get("earn").toString());
//                    confirm = confirm+1;
//                    earn = earn + 100;
//                    String key = data.getKey();
//                    try {
//                        driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
//                        driver_acc.child(key).child("earn").setValue(Integer.toString(earn));
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

//        (new Handler()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.v("TAG","Mapactivity fire");
//                Intent intent = new Intent(TripHandlerActivity.this, MapActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish();
//            }
//        },3000) ;
    }

    private void getCurrentLocation() {
        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            final String userId= log_id.getString("id",null);
            DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null)+"/"+userId);
            delref.removeValue();
//            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+userId);
//            Log.i("TAG","latitude : "+latitude+"\nLongitude : "+longitude);
//            GeoFire geoFire=new GeoFire(ref);
//            geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//            Log.i("TAG","latitude : "+latitude+"\nLongitude : "+longitude);
            DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status");
            GeoFire loc=new GeoFire(tripstatus);
//            ref.child("seat").setValue("0");
            loc.setLocation(userId,new GeoLocation(latitude,longitude));
        }
    }

}
