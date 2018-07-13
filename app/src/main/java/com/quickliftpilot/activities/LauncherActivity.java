package com.quickliftpilot.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.quickliftpilot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.Util.SQLQueries;
import com.quickliftpilot.services.UpdateLocation;

import java.util.ArrayList;

public class LauncherActivity extends AppCompatActivity {
    UpdateLocation mReceiver=new UpdateLocation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.v("Tag","Launcher Activity");
        SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        final SharedPreferences.Editor editor=log_id.edit();
        if (log_id.contains("id")) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers");
            String version=null;
            try {
                version=getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            db.child(log_id.getString("id", null)+"/version").setValue(version);
            db.child(log_id.getString("id", null)).child("subPlan").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("TAG","I am Here : "+dataSnapshot.getValue().toString());
                    if (dataSnapshot.exists()) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubscriptionCategory/" + dataSnapshot.getValue().toString());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("amount"))
                                        editor.putString("basevalue", String.valueOf(dataSnapshot.child("amount").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("tax"))
                                        editor.putString("tax", String.valueOf(dataSnapshot.child("tax").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("mincommission"))
                                        editor.putString("mincommission", String.valueOf(dataSnapshot.child("mincommission").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("maxcommission"))
                                        editor.putString("maxcommission", String.valueOf(dataSnapshot.child("maxcommission").getValue(Integer.class)));
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fare/Patna/DriverCancelCharge");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        editor.putString("cancelcharge", dataSnapshot.getValue(Integer.class).toString());
                        editor.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final SQLQueries sqlQueries=new SQLQueries(this);
            sqlQueries.deletefare();
            sqlQueries.deletelocation();
            DatabaseReference dblist= FirebaseDatabase.getInstance().getReference("Fare/Patna");
            dblist.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("excelcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/excel").getValue(Integer.class)));
                    editor.putString("sharecharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/share").getValue(Integer.class)));
                    editor.putString("fullcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/full").getValue(Integer.class)));
                    editor.putString("ratemultiplier",String.valueOf(dataSnapshot.child("RateMultiplier").getValue(Float.class)));
                    editor.putString("searchingtime",String.valueOf(dataSnapshot.child("SearchingTime").getValue(Integer.class)));
                    editor.putString("outsidetripextraamount",String.valueOf(dataSnapshot.child("OutsideTripExtraAmount").getValue(Integer.class)));
                    editor.putString("twoseatprice",String.valueOf(dataSnapshot.child("Twoseatprice").getValue(Integer.class)));
                    editor.putString("excel",String.valueOf(dataSnapshot.child("ParkingCharge/excel").getValue(Integer.class)));
                    editor.putString("fullcar",String.valueOf(dataSnapshot.child("ParkingCharge/fullcar").getValue(Integer.class)));
                    editor.putString("fullrickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/fullrickshaw").getValue(Integer.class)));
                    editor.putString("sharecar",String.valueOf(dataSnapshot.child("ParkingCharge/sharecar").getValue(Integer.class)));
                    editor.putString("sharerickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/sharerickshaw").getValue(Integer.class)));
                    editor.putString("normaltimeradius",dataSnapshot.child("NormalTimeSearchRadius").getValue(String.class));
                    editor.putString("peaktimeradius",dataSnapshot.child("PeakTimeSearchRadius").getValue(String.class));
                    editor.putString("waittime",String.valueOf(dataSnapshot.child("WaitingTime").getValue(Integer.class)));
                    editor.putString("waitingcharge",String.valueOf(dataSnapshot.child("WaitingCharge").getValue(Integer.class)));
                    editor.commit();
                    for (DataSnapshot data:dataSnapshot.child("Package").getChildren()){
                        ArrayList<String> price=new ArrayList<String>();
                        price.add(data.child("Latitude").getValue(String.class));
                        price.add(data.child("Longitude").getValue(String.class));
                        price.add(data.child("Amount").getValue(String.class));
                        price.add(data.child("Distance").getValue(String.class));

                        sqlQueries.savelocation(price);
                    }
                    for (DataSnapshot data:dataSnapshot.child("Price").getChildren()){
                        ArrayList<String> price=new ArrayList<String>();
                        price.add(data.child("NormalTime/BaseFare/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/BaseFare/Distance").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/Time").getValue(String.class));

                        sqlQueries.savefare(price);
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");

                        price.clear();
                        price.add(data.child("PeakTime/BaseFare/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/BaseFare/Distance").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/Time").getValue(String.class));

                        sqlQueries.savefare(price);
//                    Toast.makeText(WelcomeScreen.this, ""+"hi", Toast.LENGTH_SHORT).show();
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

//        if (!log_id.contains("receiver")) {
//            IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
//            registerReceiver(mReceiver, intentFilter);
//            Intent it = new Intent("android.intent.action.MAIN");
//            sendBroadcast(it);
//            editor.putString("receiver","set");
//            editor.commit();
//        }
//        else {
////            unregisterReceiver(new UpdateLocation());
////            IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
////            this.registerReceiver(new UpdateLocation(), intentFilter);
//            IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
//            registerReceiver(mReceiver, intentFilter);
//            Intent it = new Intent("android.intent.action.MAIN");
//            sendBroadcast(it);
//        }
//            IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
//            registerReceiver(mReceiver, intentFilter);
//            Intent it = new Intent("android.intent.action.MAIN");
//            sendBroadcast(it);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LauncherActivity.this,Login.class));
                finish();
            }
        },2000);
    }
}
