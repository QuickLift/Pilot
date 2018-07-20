package com.quickliftpilot.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.Util.GPSTracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,LocationListener {
    private SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;
    private SharedPreferences logid;
    private DatabaseReference ref;
    PowerManager.WakeLock wakeLock;
    private LocationRequest lct;
    private GoogleApiClient googleApiClient;
    private SharedPreferences pref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
        logid=context.getSharedPreferences("Login",MODE_PRIVATE);
        pref = context.getSharedPreferences("loginPref",MODE_PRIVATE);
        ref = FirebaseDatabase.getInstance().getReference("Location");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
    }

    public Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
            while (true) {
//                Log.v("Target", "Printing");
                if (!pref.getBoolean("status",false) && (!logid.contains("ride") || logid.getString("ride",null).equals(""))){
                    stopForeground(true);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    stopSelf();
                }
                else {
                    googleApiClient = new GoogleApiClient.Builder(LocationService.this)
                            .addConnectionCallbacks(LocationService.this)
                            .addOnConnectionFailedListener(LocationService.this)
                            .addApi(LocationServices.API)
                            .build();
                    googleApiClient.connect();
//                ref.child(logid.getString("key", null)).setValue(sdf.format(new Date()));
                    if (logid.contains("ride") && !logid.getString("ride", null).equals("")) {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (pref.contains("status") && pref.getBoolean("status",false)){
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
//            stopSelf();
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            backgroundThread.start();
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
        if (wakeLock.isHeld())
            wakeLock.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pref.getBoolean("status",false))
                startForegroundService(new Intent(this.context,LocationService.class));
        }
        else {
            if (pref.getBoolean("status",false))
                startService(new Intent(this.context,LocationService.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
            Notification notification=new Notification();
            startForeground(1,notification);
        }
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lct = LocationRequest.create();
        lct.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, lct, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v("Location","Location Changed !");
        if (location!=null){
            SharedPreferences.Editor editor=logid.edit();
            editor.putString("cur_lat",String.valueOf(location.getLatitude()));
            editor.putString("cur_lng",String.valueOf(location.getLongitude()));
            editor.commit();
            if (logid.contains("ride") && !logid.getString("ride", null).equals("")) {
                getStatusCurrentLocation(location);
            }
            else if (pref.contains("status") && pref.getBoolean("status",false)){
                getCurrentLocation(location);
            }
        }
    }

    public void getStatusCurrentLocation(Location location) {
        String userId= logid.getString("id",null);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Status");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
    }

    public void getCurrentLocation(Location location) {
        String userId = logid.getString("id",null);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+logid.getString("type",null));
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
    }
}