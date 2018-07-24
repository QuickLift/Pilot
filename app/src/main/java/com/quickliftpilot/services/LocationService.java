package com.quickliftpilot.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.activities.MapActivity;
import com.quickliftpilot.activities.RequestActivity;
import com.quickliftpilot.activities.Welcome;
import com.quickliftpilot.model.SequenceModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
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
    private DatabaseReference customerReq;
    private SharedPreferences log_id;
    private SharedPreferences ride_info;
    private SharedPreferences.Editor editor;
    private Intent notificationServ;
    private Stack<SequenceModel> stack;

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
        stack = new SequenceStack().getStack();
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
//            Notification notification=new Notification();

//            startForeground(1,notification);
            notification();
            getrequest();
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakelockTag");
        wakeLock.acquire();
        return START_STICKY;
    }

    private void getrequest() {
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));

        customerReq.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i("OK","Child Added with string "+s);
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Log.i("TAG", "Request comming");
//                Log.i("OK","Map value in request : "+map.toString());
                    if (map.containsKey("customer_id")) {
                        ride_info = getSharedPreferences("ride_info", MODE_PRIVATE);
                        editor = ride_info.edit();
                        editor.putString("accept", map.get("accept").toString());
                        editor.putString("customer_id", map.get("customer_id").toString());
                        editor.putString("d_lat", map.get("d_lat").toString());
                        editor.putString("d_lng", map.get("d_lng").toString());
                        editor.putString("destination", map.get("destination").toString());
                        editor.putString("en_lat", map.get("en_lat").toString());
                        editor.putString("en_lng", map.get("en_lng").toString());
                        editor.putString("otp", map.get("otp").toString());
                        editor.putString("price", map.get("price").toString());
                        editor.putString("seat", map.get("seat").toString());
                        editor.putString("source", map.get("source").toString());
                        editor.putString("st_lat", map.get("st_lat").toString());
                        editor.putString("st_lng", map.get("st_lng").toString());
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users/" + map.get("customer_id"));
                        user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("TAG", "getting user info");
                                Map<String, Object> userMap = (Map<String, Object>) dataSnapshot.getValue();
                                editor.putString("name", userMap.get("name").toString());
                                editor.putString("phone", userMap.get("phone").toString());
                                editor.putString("email", userMap.get("email").toString());
                                editor.commit();
                                notificationServ = new Intent(LocationService.this, NotificationService.class);
                                Log.i("TAG", "NotificationService Started");
                                startService(notificationServ);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.v("OK","Request Service Again Called"+dataSnapshot.getKey());
                if (dataSnapshot.child("accept").getValue(Integer.class) == 0) {
                    if (RequestActivity.RequestActivity != null)
                        RequestActivity.RequestActivity.finish();
                    if (dataSnapshot.child("customer_id").getValue().toString().equals(ride_info.getString("customer_id",null))){
                        SharedPreferences.Editor editor=ride_info.edit();
                        editor.remove("customer_id");
                        editor.commit();
                    }
                }

                if (dataSnapshot.child("accept").getValue(Integer.class) != 0) {
                    final SharedPreferences.Editor editor = log_id.edit();

                    String string = null;
                    int count=0;
                    ArrayList<SequenceModel> sequenceModels = new ArrayList<>();
                    if (stack.size() > 0) {
                        Log.v("TAG", "Child Removed Called !");
                        while (stack.size() > 0) {
                            Log.i("TAG", "Stak pop : " + stack.size());
                            SequenceModel deleteModel = stack.pop();
                            Log.v("TAG", deleteModel.getId() + " " + dataSnapshot.child("customer_id").getValue().toString());
                            if (!deleteModel.getId().equals(dataSnapshot.child("customer_id").getValue().toString())) {
                                sequenceModels.add(deleteModel);
                                if (deleteModel.getType().equals("drop"))
                                    count=count+deleteModel.getSeat();
                                Log.i("TAG", "item for pushing : " + sequenceModels.size());
                            } else {
                                string = deleteModel.getName();
//                                    Toast.makeText(RequestService.this, ""+ string, Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (sequenceModels.size() > 0) {
                            for (int i = sequenceModels.size() - 1; i >= 0; i--) {
                                Log.i("TAG", "Stak push : " + i);
                                stack.push(sequenceModels.get(i));
                            }
                            sequenceModels.clear();
                        }
                    }

                    if (!dataSnapshot.child("seat").getValue().toString().equalsIgnoreCase("full")) {
//                    seat = seat-Integer.parseInt(dataSnapshot.child("seat").getValue().toString());
                        Log.i("OK", "Child Seat : ");
                        int seat = 0;
                        editor.putString("seats", String.valueOf(count));
                        editor.commit();
//                    Toast.makeText(RequestService.this, ""+seat, Toast.LENGTH_SHORT).show();
                        if (count == 0) {
                            Log.v("OK", "Removed");
                            DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/" + log_id.getString("type", null) + "/" + log_id.getString("id", null));
                            seat_data.removeValue();

                        } else {
                            Log.v("OK", "Set value");
                            DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/" + log_id.getString("type", null) + "/" + log_id.getString("id", null) + "/seat");
                            seat_data.setValue(Integer.toString(count));
                        }
                    } else if (dataSnapshot.child("seat").getValue().toString().equalsIgnoreCase("full")) {
                        DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/" + log_id.getString("type", null) + "/" + log_id.getString("id", null));
                        seat_data.removeValue();
                        editor.putString("seats", "0");
                        editor.commit();
                    }

                    //int size = stack.size();

                    if (dataSnapshot.child("accept").getValue(Integer.class) == 2 && dataSnapshot.child("accept").getValue(Integer.class) != 3) {
                        Log.v("OK", "This is called !");

                        if (stack.size() > 0) {
                            if (MapActivity.fa != null) {
                                MapActivity.fa.finish();
                            }
                            Intent intent = new Intent(LocationService.this, MapActivity.class);
                            intent.putExtra("cancelled", string);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.v("TAG", "Placing DriverAvailable !");
                            editor.putString("ride", "");
                            editor.commit();

                            SharedPreferences pref = getSharedPreferences("loginPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = pref.edit();
                            editor1.putBoolean("status", true);
                            editor1.commit();

                            GPSTracker gps = new GPSTracker(LocationService.this);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/" + log_id.getString("type", null));
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation(log_id.getString("id", null), new GeoLocation(gps.getLatitude(), gps.getLongitude()));

                            if (Welcome.WelcomeActivity != null) {
                                Welcome.WelcomeActivity.finish();
                            }
                            Intent intent = new Intent(LocationService.this, Welcome.class);
                            intent.putExtra("status", "true");
                            intent.putExtra("cancelled", string);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else if (dataSnapshot.child("accept").getValue(Integer.class) != 3) {
                        Log.v("OK", "This is also called !");
                        if (stack.size() > 0) {
                            if (MapActivity.fa != null) {
                                MapActivity.fa.finish();
                            }
                            Intent intent = new Intent(LocationService.this, MapActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.v("TAG", "Placing DriverAvailable !");
                            editor.putString("ride", "");
                            editor.commit();

                            SharedPreferences pref = getSharedPreferences("loginPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = pref.edit();
                            editor1.putBoolean("status", true);
                            editor1.commit();

                            GPSTracker gps = new GPSTracker(LocationService.this);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/" + log_id.getString("type", null));
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation(log_id.getString("id", null), new GeoLocation(gps.getLatitude(), gps.getLongitude()));

                            if (Welcome.WelcomeActivity != null) {
                                Welcome.WelcomeActivity.finish();
                            }
                            Intent intent = new Intent(LocationService.this, Welcome.class);
                            intent.putExtra("status", "true");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

//                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View view = inflater.inflate(R.layout.dialog_layout, null);
//                Dialog dialog=new Dialog(RequestService.this);
//                dialog.setCancelable(false);
//                dialog.setTitle("Trip Cancelled !");
//                dialog.setContentView(view);
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                dialog.show();
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

    public void notification (){
        String title="";
        String channelId = "channel-07";
        String channelName = "Driver Location";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification noti = new NotificationCompat.Builder(this,channelId)
                .setContentTitle("QuickLift Pilot")
                .setContentText("Quicklift Pilot is running. Do not close this service.")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo))
                .setSmallIcon(R.drawable.carfinal)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        mNotificationManager.notify(001, noti);
        startForeground(7,noti);
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