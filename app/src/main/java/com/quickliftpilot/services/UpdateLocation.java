package com.quickliftpilot.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.activities.Welcome;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by adarsh on 5/5/18.
 */

public class UpdateLocation extends BroadcastReceiver {
    Context con;
    int i=0;
    LocationManager manager;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    SharedPreferences log_id,pref;
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
//            i=i+1;
//            Toast.makeText(con, "Locating "+i, Toast.LENGTH_SHORT).show();
            if (log_id.contains("ride") && !log_id.getString("ride", null).equals("")) {
                Log.v("Tag","On trip");
//                if () {
                    getStatusCurrentLocation();

                    handler.postDelayed(runnable, 10000);
//                }
            }
            else if (pref.contains("status") && pref.getBoolean("status",false)){
                Log.v("Tag","Available");
                    getCurrentLocation();
                    handler.postDelayed(runnable, 15000);
//                }
            }
            else {
                Log.v("Tag","Locating");
                handler.postDelayed(runnable,15000);
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        this.con=context;

        log_id=context.getSharedPreferences("Login",MODE_PRIVATE);
        pref = context.getSharedPreferences("loginPref",MODE_PRIVATE);
        manager =  (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            displayLocationSettingsRequest(context);
        }
//        getCurrentLocation();
//        Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show();
//        Log.v("Update","Receiver");
//        PendingIntent service = null;
//        Intent intentForService = new Intent(context.getApplicationContext(), UpdateLocation.class);
//        final AlarmManager alarmManager = (AlarmManager) context
//                .getSystemService(Context.ALARM_SERVICE);
//        final Calendar time = Calendar.getInstance();
//        time.set(Calendar.MINUTE, 0);
//        time.set(Calendar.SECOND, 0);
//        time.set(Calendar.MILLISECOND, 0);
//        if (service == null) {
//            service = PendingIntent.getService(context, 0,
//                    intentForService,    PendingIntent.FLAG_CANCEL_CURRENT);
//        }
//
//        alarmManager.setRepeating(AlarmManager.RTC, time.getTime()
//                .getTime(), 5000, service);


        handler.postDelayed(runnable,10);

    }

    private void getCurrentLocation() {
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//            displayLocationSettingsRequest(con);
//        }
        GPSTracker gps = new GPSTracker(con);
//
//        if (gps.canGetLocation()){
//            Log.v("Tag",""+gps.getLatitude()+" "+gps.getLongitude());
//        }
        // check if GPS enabled
        if (gps.canGetLocation()) {
//            Log.i("TAG","Getting location from GPS Tracker");

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

//            if (log_id.getString("ride",null).equals("")) {
//                Log.v("TAG","CURRENT LOCATION CALLED !");
                String userId = log_id.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
//            ref.push().setValue("hello");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//            }
//            else {
//                String userId= log_id.getString("id",null);
//                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null));
////                GeoFire geoFire=new GeoFire(ref);
////                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//            }
        }else {
            Log.i("Tag","False");
        }
    }

    private void getStatusCurrentLocation() {
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

//            displayLocationSettingsRequest(con);
//        }
        GPSTracker gps = new GPSTracker(con);
//
//        if (gps.canGetLocation()){
//            Log.v("Tag",""+gps.getLatitude()+" "+gps.getLongitude());
//        }
        // check if GPS enabled
        if (gps.canGetLocation()) {
//            Log.i("TAG","Getting location from GPS Tracker");

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

//            if (log_id.getString("ride",null).equals("")) {
//                Log.v("TAG","CURRENT LOCATION CALLED !");
//                String userId = log_id.getString("id",null);
//                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
////            ref.push().setValue("hello");
//                GeoFire geoFire = new GeoFire(ref);
//                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//            }
//            else {
//                String userId= log_id.getString("id",null);
//                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("type",null));
//                GeoFire geoFire=new GeoFire(ref);
//                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//            }
                String userId= log_id.getString("id",null);
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Status");
                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
        }else {
            Log.i("TAG","False");
        }
    }

//    private void displayLocationSettingsRequest(final Context context) {
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
//                .addApi(LocationServices.API).build();
//        googleApiClient.connect();
//
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(10000 / 2);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//        builder.setAlwaysShow(true);
//
//        com.google.android.gms.common.api.PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        Log.i("TAG", "All location settings are satisfied.");
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
//
//                        try {
//                            // Show the dialog by calling startResolutionForResult(), and check the result
//                            // in onActivityResult().
//                            status.startResolutionForResult(, REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.i("TAG", "PendingIntent unable to execute request.");
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
//                        break;
//                }
//            }
//        });
//    }
}
