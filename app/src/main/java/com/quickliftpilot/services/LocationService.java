package com.quickliftpilot.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.Util.GPSTracker;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {
    SharedPreferences log_id,pref;
    Timer timer=new Timer();
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
//            i=i+1;
//            Toast.makeText(con, "Locating "+i, Toast.LENGTH_SHORT).show();
            if (log_id.contains("ride") && !log_id.getString("ride", null).equals("")) {
                getStatusCurrentLocation();
                handler.postDelayed(runnable, 10000);
            }
            else if (pref.contains("status") && pref.getBoolean("status",false)){
                getCurrentLocation();
                handler.postDelayed(runnable, 25000);
            }
            else if (pref.contains("status") && !pref.getBoolean("status",false)){
                stopSelf();
            }
        }
    };

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handler.postDelayed(runnable,1);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void getCurrentLocation() {
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            String userId = log_id.getString("id",null);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
        }
    }

    private void getStatusCurrentLocation() {
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            String userId= log_id.getString("id",null);
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Status");
            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pref.getBoolean("status",false)){
            startService(new Intent(this,LocationService.class));
        }
    }
}
