package com.quickliftpilot.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.Util.RequestDetails;

import java.util.ArrayList;
import java.util.Map;

public class StoreRequestDetails extends Service {
    RequestDetails requestDetails=new RequestDetails();
    ArrayList<Map<String,Object>> arr_list=new ArrayList<>();
    public StoreRequestDetails() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    arr_list.add((Map<String,Object>)data.getValue());
                }
                requestDetails.setRequest_list(arr_list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
