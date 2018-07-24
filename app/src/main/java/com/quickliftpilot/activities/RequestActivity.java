package com.quickliftpilot.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.services.WakeLockService;

public class RequestActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static Button confirm,cancel;
    private static SharedPreferences preferences,log_id;
    private static SharedPreferences.Editor editor;
    private static TextView user_name,user_add;
    DatabaseReference customerReq;
    public static Activity RequestActivity=null;
    Handler handler=new Handler();
    Runnable runnable;
    Vibrator v;
    MediaPlayer mMediaPlayer=new MediaPlayer();
    public PowerManager.WakeLock mWakeLock;
    boolean isReleased=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG").acquire();
        RequestActivity=this;
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
//        if (!mWakeLock.isHeld()) {
//            mWakeLock.acquire();
//        }
//        startService(wakeserv);
        preferences = getSharedPreferences("ride_info",MODE_PRIVATE);
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
//        customerReq.keepSynced(true);
//        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//        registerReceiver(new Receiver(), intentFilter);

        confirm = (Button)findViewById(R.id.confirm_btn);
        cancel = (Button)findViewById(R.id.cancel_btn);
        user_name = (TextView)findViewById(R.id.user_name);
        user_add = (TextView)findViewById(R.id.user_add);

        runnable=new Runnable() {
            @Override
            public void run() {
                if (preferences.contains("customer_id")) {
                    customerReq.child(preferences.getString("customer_id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child("accept").getValue().toString().equals("0"))
                                    customerReq.child(dataSnapshot.child("customer_id").getValue().toString()).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

        mMediaPlayer = MediaPlayer.create(this, R.raw.notification_tone);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1500};
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern,0));
        }else{
            //deprecated in API 26
            v.vibrate(pattern,0);
        }

//        handler.postDelayed(runnable,60000);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferences.contains("customer_id")) {
                    customerReq.child(preferences.getString("customer_id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child("accept").getValue().toString().equals("0")){
                                    if (!isReleased) {
                                        mMediaPlayer.stop();
                                        mMediaPlayer.release();
                                        isReleased=true;
                                    }
//            if (!mWakeLock.isHeld()) {
//                mWakeLock.release();
//            }
                                    if (v.hasVibrator()) {
                                        v.cancel();
                                    }
                                        Intent confirmIntent = new Intent(RequestActivity.this, TripHandlerActivity.class);
                                        confirmIntent.putExtra("value","confirm");
                                        startActivity(confirmIntent);
                                        finish();
                                }
                                else {
                                    finish();
                                }
                            }
                            else {
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                customerReq.child(preferences.getString("customer_id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            if (dataSnapshot.child("accept").getValue().toString().equals("0")){
////                                if (mMediaPlayer.isPlaying())
////                                    mMediaPlayer.stop();
////                                if (v.hasVibrator())
////                                    v.cancel();
////                                if (!mWakeLock.isHeld())
////                                    mWakeLock.release();
//
//                            }
//                            else {
//                                finish();
//                            }
//                        }
//                        else {
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
                if (!isReleased) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    isReleased=true;
                }
//            if (!mWakeLock.isHeld()) {
//                mWakeLock.release();
//            }
                if (v.hasVibrator()) {
                    v.cancel();
                }
                Intent cancelIntent = new Intent(RequestActivity.this, TripHandlerActivity.class);
                cancelIntent.putExtra("value","cancel");
                startActivity(cancelIntent);
                finish();
            }
        });

        customerReq.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equalsIgnoreCase(preferences.getString("customer_id",null))){
                    handler.removeCallbacks(runnable);
                    getCurrentLocation();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
                    SharedPreferences.Editor editor1=pref.edit();
                    editor1.putBoolean("status", true);
                    editor1.commit();
                    finish();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(Double.parseDouble(preferences.getString("st_lat",null)), Double.parseDouble(preferences.getString("st_lng",null)));
        mMap.addMarker(new MarkerOptions().position(sydney).title("Pick Up"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    protected void onStart() {
        super.onStart();
        user_name.setText(preferences.getString("name",null));
        user_add.setText(preferences.getString("source",null));
        if (!preferences.getString("seat",null).equals("full")){
            ((TextView)findViewById(R.id.title)).setText(R.string.Incoming_Share_Request);
        }

//        if (!preferences.contains("customer_id")){
//            if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
//                mMediaPlayer.stop();
//                mMediaPlayer.release();
//            }
//            finish();
//        }
    }

    private void getCurrentLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        }
        GPSTracker gps = new GPSTracker(this);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            if (log_id.getString("ride",null).equals("")) {
                String userId = log_id.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));

                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(latitude, longitude));
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(this.isFinishing()) {
            if (!isReleased) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                isReleased = true;
            }
        }
//            if (!mWakeLock.isHeld()) {
//                mWakeLock.release();
//            }
        if (v.hasVibrator())
            v.cancel();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //    public class Receiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//            if (isConnected) {
//                findViewById(R.id.network_status).setVisibility(View.VISIBLE);
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            }
//            else {
//                findViewById(R.id.network_status).setVisibility(View.GONE);
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            }
//        }
//    }
}
