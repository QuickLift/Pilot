package com.quickliftpilot.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.quickliftpilot.services.UpdateLocation;

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
