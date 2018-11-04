package com.quickliftpilot.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetLocation extends Service {
    public SetLocation() {

//        Log.v("Update","Location Updated");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Update","Location Updated");

        SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);

        DatabaseReference accinfo = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null));
        accinfo.keepSynced(true);
        DatabaseReference rides = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        rides.keepSynced(true);
        DatabaseReference cusreq = FirebaseDatabase.getInstance().getReference("Rides");
        cusreq.keepSynced(true);

        return super.onStartCommand(intent, flags, startId);
    }
}
