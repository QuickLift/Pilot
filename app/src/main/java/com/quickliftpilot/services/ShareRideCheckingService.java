package com.quickliftpilot.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.quickliftpilot.Util.CheckRoute;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.Util.UserRequestInfo;
import com.quickliftpilot.model.SequenceModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class ShareRideCheckingService extends Service {
    DatabaseReference sharereq;
    UserRequestInfo userRequestInfo;
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    SharedPreferences log_id,ride_info;
    SharedPreferences.Editor editor;
    Intent notificationServ;
    private Stack<SequenceModel> stack;
    GPSTracker gpsTracker;
    int value=0;
    PowerManager.WakeLock wakeLock;

    public ShareRideCheckingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        stack = new SequenceStack().getStack();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wakeLock.acquire();
        Log.i("TAG","ShareRideCheckingService");

        sharereq = FirebaseDatabase.getInstance().getReference("Share");
        sharereq.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                value=1;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sharereq.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    if (value == 1 && stack.size() != 0) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        gpsTracker = new GPSTracker(ShareRideCheckingService.this);
                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(Double.valueOf(map.get("st_lat").toString()));
                        location.setLongitude(Double.valueOf(map.get("st_lng").toString()));

//                    Toast.makeText(ShareRideCheckingService.this, ""+String.valueOf(gpsTracker.getLocation().distanceTo(location)), Toast.LENGTH_SHORT).show();
                        if (gpsTracker.getLocation().distanceTo(location) < 5000) {
                            ArrayList<SequenceModel> seq = new ArrayList<>();
                            seq.clear();
                            for (int i = 0; i < stack.size(); i++) {
                                seq.add(stack.pop());
                            }

                            StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
                            StringBuilder url2 = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
                            url.append("origin=" + gpsTracker.getLatitude() + "," + gpsTracker.getLongitude());
                            url2.append("origin=" + gpsTracker.getLatitude() + "," + gpsTracker.getLongitude());
                            url.append("&destination=" + seq.get(seq.size() - 1).getLat() + "," + seq.get(seq.size() - 1).getLng());

                            Location location2 = new Location(LocationManager.GPS_PROVIDER);
                            location2.setLatitude(seq.get(seq.size() - 1).getLat());
                            location2.setLongitude(seq.get(seq.size() - 1).getLng());

                            if (gpsTracker.getLocation().distanceTo(location) > gpsTracker.getLocation().distanceTo(location2))
                                url2.append("&destination=" + location.getLatitude() + "," + location.getLongitude());
                            else
                                url2.append("&destination=" + location2.getLatitude() + "," + location2.getLongitude());

                            url.append("&waypoints=optimize:true");
                            url2.append("&waypoints=optimize:true");
                            url2.append("|" + location.getLatitude() + "," + location.getLongitude());

                            for (int j = seq.size() - 1; j >= 0; j--) {
                                stack.push(seq.get(j));
                                url.append("|" + seq.get(j).getLat() + "," + seq.get(j).getLng());
                                url2.append("|" + seq.get(j).getLat() + "," + seq.get(j).getLng());
                            }
                            url.append("&key=AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
                            url2.append("&key=AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");

                            Log.v("Tag",url.toString());
                            Log.v("Tag",url2.toString());

                            SharedPreferences log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
                            Object[] datatransfer = new Object[5];
                            String form_url = url.toString();
                            String form_url2 = url2.toString();
                            CheckRoute checkRoute = new CheckRoute();
                            datatransfer[0] = form_url;
                            datatransfer[1] = form_url2;
                            datatransfer[2] = log_id;
                            datatransfer[3] = dataSnapshot.getKey();
                            //Log.v("TAG",""+url);
                            checkRoute.execute(datatransfer);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld())
            wakeLock.release();

//        SharedPreferences pref = getSharedPreferences("loginPref", MODE_PRIVATE);
//        if (pref.contains("status") && pref.getBoolean("status",false)){
//            startService(new Intent(this,ShareRideCheckingService.class));
//        }
    }
}
