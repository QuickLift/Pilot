package com.quickliftpilot.Util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by pandey on 12/3/18.
 */

public class GPSTracker extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private GoogleApiClient googleApiClient;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
//        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
//                Log.i("TAG","Geting location");
//
//                // First get location from Network Provider
//
//                Log.i("TAG","Geting Fused location");
//                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return null;
//                }
//                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
////                  Toast.makeText(this, "Current Location : "+location, Toast.LENGTH_SHORT).show();
//                Log.i("TAG","Current Location : "+location);
//
//
//                if (location != null) {
//                    this.canGetLocation = true;
//                    googleApiClient.disconnect();
//                    longitude = location.getLongitude();
//                    latitude = location.getLatitude();
//                }
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.e("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

//                                Log.v("Location",""+latitude+" "+longitude);
                            }
                        }
                    } catch (SecurityException e) {
                        Log.e("SecurityException", " 1 " + e.toString());
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                else if (isGPSEnabled) {
                    if (location == null) {
                        try {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.e("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
//                                Log.v("Location",""+latitude+" "+longitude);
                            }
                        } catch (SecurityException e) {
                            Log.e("SecurityException", " 2 " + e.toString());
                        }
                    }
                }
//                  else{
//                    Log.i("TAG","Geting Fused location");
//                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        return null;
//                    }
//                    location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
////                  Toast.makeText(this, "Current Location : "+location, Toast.LENGTH_SHORT).show();
//                    Log.i("TAG","Current Location : "+location);
//                    if (location != null) {
//                        longitude = location.getLongitude();
//                        latitude = location.getLatitude();
//                    }
//                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        Log.v("Location","here : ");
        return location;
    }


    public void stopUsingGPS() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(GPSTracker.this);
            } catch (SecurityException e) {
                Log.e("SecurityException", " 3 " + e.toString());
            }

        }
    }


    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }


    public boolean canGetLocation() {
        return this.canGetLocation;
    }



    @Override
    public void onLocationChanged(Location location) {
        //        Log.v("Location","h");
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Log.v("Location","hry");
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

