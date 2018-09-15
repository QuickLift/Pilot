package com.quickliftpilot.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickliftpilot.DAO.DatabaseHelper;
import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.SequenceModel;
import com.quickliftpilot.services.LocationService;
import com.quickliftpilot.services.OngoingRideService;
import com.quickliftpilot.services.RequestService;
import com.quickliftpilot.services.ShareRideCheckingService;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.services.UpdateLocation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Welcome extends AppCompatActivity implements Runnable,LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final int RequestPermissionCode = 1;
    private static LinearLayout ride,profile,account,help;
    private static Switch login_btn;
    private static DatabaseReference db,driver_acc,driver_info;
    private static TextView login_status,login_duration,book,earn,cancel,name,contact,pickup;
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor,wel_edit;
    private static RatingBar rate;
    private static SharedPreferences log_id,welcome,account_info;
    private static DatabaseHelper databaseHelper;
    private static Intent rideCheckingService,locationService,requestService;
    private static Date login_time,logout_time;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    boolean doubleBackToExitPressedOnce = false;
    CircleImageView image,profile_icon;
    String type = null;
    Bitmap photo;
    LocationManager manager;
    public static Activity WelcomeActivity=null;
    private PowerManager.WakeLock mWakeLock;
    UpdateLocation mReceiver=new UpdateLocation();
    ProgressDialog dialog;
//    private GoogleApiClient googleApiClient;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }
//        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
//        registerReceiver(mReceiver, intentFilter);
//        Intent it = new Intent("android.intent.action.MAIN");
//        sendBroadcast(it);
        if (Welcome.WelcomeActivity != null) {
            Welcome.WelcomeActivity.finish();
        }
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference();
        scoresRef.keepSynced(true);
        WelcomeActivity=this;
        dialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading ! Please Wait...");
//        dialog.show();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();

        manager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                displayLocationSettingsRequest(getApplicationContext());
            }

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        final SharedPreferences.Editor pref_editor=log_id.edit();

//        startService(new Intent(this, RequestService.class));

        db = FirebaseDatabase.getInstance().getReference("Drivers");
        type = log_id.getString("type",null);
        driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info");
        databaseHelper = new DatabaseHelper(getApplicationContext());
        requestService = new Intent(Welcome.this,RequestService.class);
        locationService = new Intent(Welcome.this,LocationService.class);
        rideCheckingService=new Intent(this, ShareRideCheckingService.class);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pref = getSharedPreferences("loginPref",MODE_PRIVATE);
        account_info = getSharedPreferences("account",MODE_PRIVATE);

        welcome = getSharedPreferences("welcome",MODE_PRIVATE);

        name = (TextView)findViewById(R.id.driver_name);
        contact = (TextView)findViewById(R.id.driver_contact);
        rate = (RatingBar)findViewById(R.id.rateBar);
        rate.setRating(3);
        rate.setIsIndicator(true);

        profile = (LinearLayout)findViewById(R.id.profile);
        ride = (LinearLayout)findViewById(R.id.ride);
        account = (LinearLayout)findViewById(R.id.account);
        help = (LinearLayout)findViewById(R.id.help);
        login_btn = (Switch)findViewById(R.id.login_switch);
        login_status = (TextView)findViewById(R.id.login_status);
        login_duration = (TextView)findViewById(R.id.login_duration);
        book = (TextView)findViewById(R.id.book_no);
        earn = (TextView)findViewById(R.id.earn_no);
        cancel = (TextView)findViewById(R.id.cancel_no);
        pickup = (TextView)findViewById(R.id.pickup_dist);
        image = (CircleImageView)findViewById(R.id.image);
        profile_icon = (CircleImageView)findViewById(R.id.profile_icon);

        // variables storing function values returned by network connection functions
        boolean status1 = haveNetworkConnection();
        boolean status2 = hasActiveInternetConnection();

//         checking user permission
        if(!checkPermission())
        {
            appendLog(getCurrentTime()+"Gathering permissions status:0");

            //requesting permission to access mobile resources
            Intent i = new Intent(this,Login.class);
            startActivity(i);
            requestPermission();
        }
        else {
            appendLog(getCurrentTime() + "Gathered permissions status:1");

            if(status1 && status2)
            {
                appendLog(getCurrentTime()+"Gathering network information status:1");
//                Toast.makeText(this, "Active Internet connection", Toast.LENGTH_SHORT).show();
            }
            else{
//                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }

            if (getIntent().hasExtra("status")){
                editor = pref.edit();
                editor.putBoolean("status",true);
                editor.commit();
            }
            if (getIntent().hasExtra("cancelled")){
                View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
                TextView title=(TextView)view.findViewById(R.id.title);
                TextView message=(TextView)view.findViewById(R.id.message);
                Button left=(Button) view.findViewById(R.id.left_btn);
                Button right=(Button) view.findViewById(R.id.right_btn);

                left.setVisibility(View.GONE);
                right.setText(R.string.Map_Ok);
                title.setText(R.string.Trip_Cancelled);
                if (getIntent().getStringExtra("cancelled")!=null)
                    message.setText(getIntent().getStringExtra("cancelled")+" "+getString(R.string.Cancelled_by));
                else
                    message.setText("Customer "+getString(R.string.Cancelled_by));
                AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                builder .setView(view)
                        .setCancelable(false);

                final AlertDialog alert = builder.create();
                alert.show();

                right.setOnClickListener(null);
                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
            }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,ProfileActivity.class);
                startActivity(intent);
//                finish();
            }
        });

        ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,RidesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,AccountActivity.class);
                startActivity(intent);
//                finish();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,HelpActivity.class);
                startActivity(intent);
//                finish();
            }
        });

//        login_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Boolean check_status = login_btn.isChecked();
//                if (check_status){
//                    GregorianCalendar gregorianCalendar=new GregorianCalendar();
//                    String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
//                    String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
//                    String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
////                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
////                            .append(String.format("%02d", (month+1))).append("-").append(year);
//                    final String formateDate = date+"-"+month+"-"+year;
//                    login_time = new Date();
//                    editor = pref.edit();
//                    editor.putBoolean("status",true);
//                    editor.commit();
//                    databaseHelper.insertLoginData(formateDate,"login","0.0ms");
////                    stopService(requestService);
////                    stopService(rideCheckingService);
//                    startService(requestService);
//                    startService(rideCheckingService);
//                    login_status.setText(R.string.Welcome_Login);
//                    login_duration.setText("Running...");
//                    wel_edit = welcome.edit();
//                    wel_edit.putString("date",formateDate);
//                    wel_edit.putString("login_time",login_time.toString());
//                    wel_edit.commit();
//                    getCurrentLocation();
//                }else{
//                    logOut();
//                }
//            }
//        });
        login_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (!pref.getBoolean("status",false)){
                        if (!log_id.contains("block") || (log_id.contains("block") && log_id.getString("block",null).equals("0"))) {
                            GregorianCalendar gregorianCalendar = new GregorianCalendar();
                            String date = String.format("%02d", gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
                            String month = String.format("%02d", (gregorianCalendar.get(GregorianCalendar.MONTH) + 1));
                            String year = String.format("%02d", gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
                            final String formateDate = date + "-" + month + "-" + year;
                            login_time = new Date();
                            editor = pref.edit();
                            editor.putBoolean("status", true);
                            editor.commit();
                            databaseHelper.insertLoginData(formateDate, "login", "0.0ms");
//                    stopService(requestService);
//                    stopService(rideCheckingService);
//                            startService(requestService);
                            startService(rideCheckingService);
//                        startService(new Intent(Welcome.this, LocationService.class));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(locationService);
//                            startService(locationService);
                            } else {
                                startService(locationService);
                            }
                            login_status.setText(R.string.Welcome_Login);
                            login_duration.setText("Running...");
                            wel_edit = welcome.edit();
                            wel_edit.putString("date", formateDate);
                            wel_edit.putString("login_time", login_time.toString());
                            wel_edit.commit();
                            driver_info = FirebaseDatabase.getInstance().getReference("Driver_Login_Info/" + log_id.getString("id", null) + "/" + welcome.getString("date", null));
                            String key = driver_info.push().getKey();
                            pref_editor.putString("loginkey", key);
                            pref_editor.commit();
                            driver_info.child(key + "/Login_Time").setValue(login_time.toString());
//                        getCurrentLocation();
                        }
                        else {
                            View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
                            TextView title=(TextView)view.findViewById(R.id.title);
                            TextView message=(TextView)view.findViewById(R.id.message);
                            Button left=(Button) view.findViewById(R.id.left_btn);
                            Button right=(Button) view.findViewById(R.id.right_btn);

                            left.setVisibility(View.GONE);
                            right.setText(R.string.Map_Ok);
                            title.setText(R.string.Account_blocked);
                            message.setText(R.string.block_message);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                            builder .setView(view)
                                    .setCancelable(false);

                            final AlertDialog alert = builder.create();
                            if (!alert.isShowing())
                            alert.show();

                            right.setOnClickListener(null);
                            right.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    login_btn.setChecked(false);
                                    alert.dismiss();
                                }
                            });
                        }
                    }

                }else {
                    if (pref.getBoolean("status",false)) {
                        logOut();
                    }
                }
            }
        });

//            login_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Boolean check_status = login_btn.isChecked();
//                    Log.v("LOGIN","true");
//                    if (check_status){
//                        GregorianCalendar gregorianCalendar=new GregorianCalendar();
//                        String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
//                        String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
//                        String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
////                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
////                            .append(String.format("%02d", (month+1))).append("-").append(year);
//                        final String formateDate = date+"-"+month+"-"+year;
//                        login_time = new Date();
//                        editor = pref.edit();
//                        editor.putBoolean("status",true);
//                        editor.commit();
//                        databaseHelper.insertLoginData(formateDate,"login","0.0ms");
////                    stopService(requestService);
////                    stopService(rideCheckingService);
//                        startService(requestService);
//                        startService(rideCheckingService);
//                        login_status.setText(R.string.Welcome_Login);
//                        login_duration.setText("Running...");
//                        wel_edit = welcome.edit();
//                        wel_edit.putString("date",formateDate);
//                        wel_edit.putString("login_time",login_time.toString());
//                        wel_edit.commit();
//                        getCurrentLocation();
//                    }else{
//                        logOut();
//                    }
//                }
//            });
    }

    private void logOut(){
        if (welcome.contains("login_time")){
            DatabaseReference cus=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
            cus.removeValue();
            editor = pref.edit();
            editor.putBoolean("status",false);
            editor.commit();
            databaseHelper.insertLoginData(welcome.getString("date",null),"logout","100.0ms");
//            stopService(requestService);
            stopService(rideCheckingService);
//            stopService(locationService);
            login_status.setText(R.string.Welcome_Logout);
            logout_time = new Date();
            login_time = new Date(welcome.getString("login_time",null));

            long diff = logout_time.getTime() - login_time.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000);
            String second,minute,hour;
            if (diffSeconds < 10){
                second = "0"+diffSeconds;
            }else {
                second = ""+diffSeconds;
            }
            if (diffMinutes < 10){
                minute = "0"+diffMinutes;
            }else {
                minute = ""+diffMinutes;
            }
            if (diffHours < 10){
                hour = "0"+diffHours;
            }else {
                hour = ""+diffHours;
            }
            String dura = hour+":"+minute+":"+second+" hr";
            login_duration.setText(dura);
            driver_info = FirebaseDatabase.getInstance().getReference("Driver_Login_Info/"+log_id.getString("id",null)+"/"+welcome.getString("date",null));
            HashMap<String,Object> map = new HashMap<>();
            map.put("Login_Time",login_time.toString());
            map.put("Logout_Time",logout_time.toString());
            map.put("Duration",dura);
            driver_info.child(log_id.getString("loginkey",null)).setValue(map);
        }
    }

    @Override
    public void run() {
//        loginDurationThread.getDuration();
    }

    private Stack<SequenceModel> stack;

    @Override
    protected void onStart() {
        super.onStart();

//        Log.v("TAG","ON Start Called");

        startService(requestService);
        if (log_id.contains("ride")) {
            if (!log_id.getString("ride", null).equals(""))
                login_btn.setEnabled(false);
        }

        if(pref.getBoolean("status",false) || getIntent().hasExtra("status")){
            if (log_id.contains("block")) {
                if (log_id.getString("block",null).equals("1")){
                    login_btn.setChecked(false);
                    login_status.setText(R.string.Welcome_Logout);
                    login_duration.setText("Not Working");
//                    stopService(requestService);
                    stopService(rideCheckingService);

                    View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
                    TextView title=(TextView)view.findViewById(R.id.title);
                    TextView message=(TextView)view.findViewById(R.id.message);
                    Button left=(Button) view.findViewById(R.id.left_btn);
                    Button right=(Button) view.findViewById(R.id.right_btn);

                    left.setVisibility(View.GONE);
                    right.setText(R.string.Map_Ok);
                    title.setText(R.string.Account_blocked);
                    message.setText(R.string.block_message);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                    builder .setView(view)
                            .setCancelable(false);

                    final AlertDialog alert = builder.create();
                    if (!alert.isShowing())
                    alert.show();

                    right.setOnClickListener(null);
                    right.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.dismiss();
                        }
                    });
                }
                else {
                    login_btn.setChecked(true);
                    login_status.setText(R.string.Welcome_Login);
                    login_duration.setText("Running");
//            Log.v("TAG","STATUS TRUE !");
//            stopService(requestService);
//            stopService(rideCheckingService);
//            startService(requestService);
//            startService(rideCheckingService);
//                    startService(requestService);
                    startService(rideCheckingService);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(locationService);
//                startService(locationService);
                    } else {
                        startService(locationService);
                    }
                }
            }
            else {
                login_btn.setChecked(true);
                login_status.setText(R.string.Welcome_Login);
                login_duration.setText("Running");
//            Log.v("TAG","STATUS TRUE !");
//            stopService(requestService);
//            stopService(rideCheckingService);
//            startService(requestService);
//            startService(rideCheckingService);
//                startService(requestService);
                startService(rideCheckingService);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(locationService);
//                startService(locationService);
                } else {
                    startService(locationService);
                }
            }
//            getCurrentLocation();
        }else {
            login_btn.setChecked(false);
            login_status.setText(R.string.Welcome_Logout);
            login_duration.setText("Not Working");
//            stopService(requestService);
            stopService(rideCheckingService);
//            stopService()
//            Log.v("TAG","STATUS False !");
        }

//        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
////
////        this.registerReceiver(mReceiver, intentFilter);
////        unregisterReceiver(mReceiver);
////
//        Welcome.this.registerReceiver(mReceiver, intentFilter);
//
        final Intent i = new Intent("android.intent.action.MAIN");

        DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
                    seat_data.removeValue();
                    DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                    tripstatus.removeValue();
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("seats","0");
                    editor.putString("ride","");
                    editor.commit();
                    stack = new SequenceStack().getStack();
                    stack.removeAllElements();
//                    if(pref.getBoolean("status",false))
//                        getCurrentLocation();
                    login_btn.setEnabled(true);
                    if (dialog.isShowing())
                        dialog.dismiss();
                    startService(rideCheckingService);

                    if (log_id.contains("block") && log_id.getString("block",null).equals("1")){
                        if (pref.getBoolean("status",false) ){
                            login_btn.setChecked(false);
                            login_status.setText(R.string.Welcome_Logout);
                            login_duration.setText("Not Working");
//                    stopService(requestService);
                            stopService(rideCheckingService);

                            View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
                            TextView title=(TextView)view.findViewById(R.id.title);
                            TextView message=(TextView)view.findViewById(R.id.message);
                            Button left=(Button) view.findViewById(R.id.left_btn);
                            Button right=(Button) view.findViewById(R.id.right_btn);

                            left.setVisibility(View.GONE);
                            right.setText(R.string.Map_Ok);
                            title.setText(R.string.Account_blocked);
                            message.setText(R.string.block_message);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                            builder .setView(view)
                                    .setCancelable(false);

                            final AlertDialog alert = builder.create();
                            if (!alert.isShowing())
                            alert.show();

                            right.setOnClickListener(null);
                            right.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.dismiss();
                                }
                            });
                        }
                    }
//                    sendBroadcast(i);
                }
                else {
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","ride");
                    editor.commit();
                    editor = pref.edit();
                    editor.putBoolean("status",true);
                    editor.commit();

                    DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
                    seat_data.removeValue();

                    login_btn.setEnabled(false);
                    login_btn.setChecked(true);
                    login_status.setText(R.string.Welcome_Login);
                    startService(rideCheckingService);
                    login_duration.setText("Running");

                    if (dialog.isShowing())
                        dialog.dismiss();
                    startService(new Intent(Welcome.this, OngoingRideService.class));
//                    unregisterReceiver(mReceiver);
//                    sendBroadcast(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentLocation() {
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            displayLocationSettingsRequest(getApplicationContext());
        }
        GPSTracker gps = new GPSTracker(Welcome.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {
            Log.i("TAG","Getting location from GPS Tracker");

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            if (log_id.getString("ride",null).equals("")) {
                Log.v("TAG","CURRENT LOCATION CALLED !");
                String userId = log_id.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
//            ref.push().setValue("hello");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
            }
            else {
                String userId= log_id.getString("id",null);
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null));
//                GeoFire geoFire=new GeoFire(ref);
//                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
            }
        }else {
            Log.i("TAG","False");
        }
    }

        @Override
        protected void onDestroy() {
            super.onDestroy();
//            unregisterReceiver(mReceiver);
            if (!mWakeLock.isHeld()) {
                mWakeLock.release();
            }
//            if (log_id.getString("ride",null).equals("")) {
//                editor = pref.edit();
//                editor.putBoolean("status", false);
//                editor.commit();
//                logOut();
//            }
//            stopService(requestService);
//            stopService(rideCheckingService);
        }

        @Override
    protected void onResume() {
        super.onResume();
//        if (pref.getBoolean("status",false)){
//            login_btn.setChecked(true);
//            login_duration.setText("Running...");
//            login_status.setText(R.string.Welcome_Login);
//        }else {
//            login_btn.setChecked(false);
//            login_status.setText(R.string.Welcome_Logout);
//        }
            db.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
//                        Log.i("TAG", "Id : " + dataSnapshot.getKey());
                        if (dataSnapshot.getChildrenCount() > 0) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

//                            Log.i("TAG", "name : " + map.get("name").toString());
                            name.setText(map.get("name").toString());
                            contact.setText(map.get("phone").toString());
                            rate.setRating(Float.parseFloat(map.get("rate").toString()));
                            if (map.containsKey("thumb") && !map.get("thumb").toString().equals("")) {
                                byte[] decodedString = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                                photo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                image.setImageBitmap(photo);
                                profile_icon.setImageBitmap(photo);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                        String date = String.format("%02d", gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
                        String month = String.format("%02d", (gregorianCalendar.get(GregorianCalendar.MONTH) + 1));
                        String year = String.format("%02d", gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
                        final String formateDate = date + "-" + month + "-" + year;
                        final SharedPreferences.Editor acc_editor=account_info.edit();

                        if (dataSnapshot.hasChild(log_id.getString("id", null))) {
                            driver_acc.child(log_id.getString("id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(formateDate)) {
                                        driver_acc.child(log_id.getString("id", null) + "/" + formateDate).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String key = dataSnapshot.getKey();
                                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                    Map<String, Object> map = (Map<String, Object>) data.getValue();
                                                    if (map.containsKey("book")) {
                                                        book.setText(map.get("book").toString());
                                                        acc_editor.putString("book",map.get("book").toString());
                                                    }
                                                    else {
                                                        book.setText("0");
                                                        acc_editor.putString("book","0");
                                                    }
                                                    float val=0;
                                                    if (map.containsKey("earn")){
                                                        acc_editor.putString("earn",map.get("earn").toString());
                                                    }
                                                    else {
                                                        acc_editor.putString("earn","0");
                                                    }
                                                    if (map.containsKey("offer")) {
                                                        val = Float.parseFloat(map.get("earn").toString()) + Float.parseFloat(map.get("offer").toString());
                                                        acc_editor.putString("offer",map.get("offer").toString());
                                                    }
                                                    else {
                                                        val = Float.parseFloat(map.get("earn").toString());
                                                        acc_editor.putString("offer","0");
                                                    }
                                                    earn.setText("Rs. " + val);

                                                    int count=0;
                                                    if (map.containsKey("cancel")) {
                                                        count += Integer.parseInt(map.get("cancel").toString());
                                                        acc_editor.putString("cancel",map.get("cancel").toString());
                                                    }
                                                    else {
                                                        acc_editor.putString("cancel","0");
                                                    }
                                                    if (map.containsKey("reject")) {
                                                        count += Integer.parseInt(map.get("reject").toString());
                                                        acc_editor.putString("reject",map.get("reject").toString());
                                                    }
                                                    else {
                                                        acc_editor.putString("reject","0");
                                                    }
                                                    cancel.setText(String.valueOf(count));
                                                    if (map.containsKey("pickup")) {
                                                        pickup.setText("Rs. " + Float.parseFloat(map.get("pickup").toString()));
                                                        acc_editor.putString("pickup",map.get("pickup").toString());
                                                    }
                                                    else {
                                                        pickup.setText("Rs. 0");
                                                        acc_editor.putString("pickup","0");
                                                    }
                                                    if (map.containsKey("cancel_charge")){
                                                        acc_editor.putString("cancel_charge",map.get("cancel_charge").toString());
                                                    }
                                                    else {
                                                        acc_editor.putString("cancel_charge","0");
                                                    }
                                                    if (map.containsKey("cash")){
                                                        acc_editor.putString("cash",map.get("cash").toString());
                                                    }
                                                    else {
                                                        acc_editor.putString("cash","0");
                                                    }
                                                    acc_editor.putString("key",data.getKey());
                                                    acc_editor.commit();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        book.setText("0");
                                        earn.setText("Rs. 0");
                                        cancel.setText("0");
                                        pickup.setText("Rs. 0");
                                        HashMap<String, Object> driver_info = new HashMap<>();
                                        driver_info.put("book", "0");
                                        driver_info.put("earn", "0");
                                        driver_info.put("reject", "0");
                                        driver_info.put("cancel", "0");
                                        driver_info.put("pickup", "0");
                                        driver_info.put("offer", "0");
                                        driver_info.put("cash", "0");
                                        driver_info.put("cancel_charge", "0");
                                        driver_acc.child(log_id.getString("id", null)).child(formateDate).push().setValue(driver_info);
                                        acc_editor.putString("book","0");
                                        acc_editor.putString("earn","0");
                                        acc_editor.putString("reject","0");
                                        acc_editor.putString("cancel","0");
                                        acc_editor.putString("pickup","0");
                                        acc_editor.putString("cash","0");
                                        acc_editor.putString("cancel_charge","0");
                                        acc_editor.putString("offer","0");
                                        acc_editor.commit();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            book.setText("0");
                            earn.setText("Rs. 0");
                            cancel.setText("0");
                            pickup.setText("Rs. 0");
                            HashMap<String, Object> driver_info = new HashMap<>();
                            driver_info.put("book", "0");
                            driver_info.put("earn", "0");
                            driver_info.put("reject", "0");
                            driver_info.put("cancel", "0");
                            driver_info.put("pickup", "0");
                            driver_info.put("offer", "0");
                            driver_info.put("cash", "0");
                            driver_info.put("cancel_charge", "0");
                            driver_acc.child(log_id.getString("id", null)).child(formateDate).push().setValue(driver_info);
                            acc_editor.putString("book","0");
                            acc_editor.putString("earn","0");
                            acc_editor.putString("reject","0");
                            acc_editor.putString("cancel","0");
                            acc_editor.putString("pickup","0");
                            acc_editor.putString("cash","0");
                            acc_editor.putString("cancel_charge","0");
                            acc_editor.putString("offer","0");
                            acc_editor.commit();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean hasActiveInternetConnection(){
        // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(Welcome.this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        CALL_PHONE

                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStorgaePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean CallPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && ReadStorgaePermission && CallPermission) {

                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
//                        Toast.makeText(Welcome.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        appendLog(getCurrentTime()+"Few permissions denied status:0");

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SixthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),CALL_PHONE);

        return SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                SixthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    static public void appendLog(String text) {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime())+"\t";
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

        private void displayLocationSettingsRequest(Context context) {
//            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
//                    .addApi(LocationServices.API).build();
//            googleApiClient.connect();
//
//            LocationRequest locationRequest = LocationRequest.create();
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            locationRequest.setInterval(10000);
//            locationRequest.setFastestInterval(10000 / 2);
//
//            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//            builder.setAlwaysShow(true);
//
//            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(LocationSettingsResult result) {
//                    final Status status = result.getStatus();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            Log.i("TAG", "All location settings are satisfied.");
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
//
//                            try {
//                                // Show the dialog by calling startResolutionForResult(), and check the result
//                                // in onActivityResult().
//                                status.startResolutionForResult(Welcome.this, REQUEST_CHECK_SETTINGS);
//                            } catch (IntentSender.SendIntentException e) {
//                                Log.i("TAG", "PendingIntent unable to execute request.");
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
//                            break;
//                    }
//                }
//            });
        }

    @Override
    public void onLocationChanged(Location location) {
//        if (location != null){
//            this.location = location;
//            getCurrentLocation();
//            moveMap();
//        }else {
//            getCurrentLocation();
//            this.location = location;
//            moveMap();
//        }
//        if (location != null) {
////            this.location = location;
////            getCurrentLocation();
////            moveMap();
//            SharedPreferences.Editor editor=log_id.edit();
//            editor.putString("cur_lat",String.valueOf(location.getLatitude()));
//            editor.putString("cur_lng",String.valueOf(location.getLongitude()));
//            editor.commit();
//            Log.v("Tag", "Welcome" + location.getLatitude() + " " + location.getLongitude());
//
//            String userId= log_id.getString("id",null);
//
//            if (log_id.contains("ride") && !log_id.getString("ride",null).equals("")) {
//                DatabaseReference statref = FirebaseDatabase.getInstance().getReference("Status");
//                GeoFire statGeoFire = new GeoFire(statref);
//                statGeoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//            }
//            else if (pref.contains("status") && pref.getBoolean("status",false)) {
//                DatabaseReference cus=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
//                GeoFire statGeoFire = new GeoFire(cus);
//                statGeoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//            }
//        }
    }

    LocationRequest lct;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        lct = LocationRequest.create();
//        lct.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        if (log_id.contains("ride") && !log_id.getString("ride",null).equals("")) {
//            lct.setInterval(15000);
//        }
//        else {
//            lct.setInterval(30000);
//        }
//
//        Log.v("Tag","Service up");
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, lct, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}