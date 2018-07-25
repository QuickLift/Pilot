package com.quickliftpilot.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickliftpilot.R;
import com.quickliftpilot.Util.SQLQueries;
import com.quickliftpilot.Util.SendSms;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Login extends AppCompatActivity {
    public static final int RequestPermissionCode = 1;
    ProgressDialog pdialog;
    EditText reference,otp;
    TextView err;
    Button send_otp,submit;
    DatabaseReference driver;
    String otp_number;
    Handler handler;
    String type;
    SharedPreferences log_id;
    FirebaseAuth auth;
    final String FirebaseUserId = "quicklift@email.com";
    final String FirebaseUserPass = "quicklift";
    DatabaseReference otp_ref;
    String apiKey = "apikey=" + "bqvQUgZIuxg-FeJqx9u1RMutVzVDtLw9haP2VNQ5DH";
    String message = "&message=";
    String sender = "&sender=" + "QIKLFT";          //TXTLCL
    String numbers = "&numbers=";
    String msg;

    LocationManager manager;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
//        if (!checkPermission()){
//            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//            requestPermission();
//        }
//    }
//

//
//    private void signIn() {
//
//        if (!validateForm()) {
//            return;
//        }
//
//        showProgressDialog();
//
//        mAuth.signInWithEmailAndPassword(mPhone.getText().toString(), mPassword.getText().toString())
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            // Log.d(TAG, "signInWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Log.i("TAG","User id : "+user.getUid());
//                                updateUI(user);
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            // Log.w(TAG, "signInWithEmail:failure", task.getException());
//                            Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                            updateUI(null);
//                        }
//                        hideProgressDialog();
//                        // [END_EXCLUDE]
//                    }
//                });
//        // [END sign_in_with_email]
//    }
//
//    private boolean validateForm() {
//        boolean valid = true;
//
//        String email = mPhone.getText().toString();
//        if (TextUtils.isEmpty(email)) {
//            mPhone.setError("Required.");
//            valid = false;
//        } else {
//            mPhone.setError(null);
//        }
//
//        String password = mPassword.getText().toString();
//        if (TextUtils.isEmpty(password)) {
//            mPassword.setError("Required.");
//            valid = false;
//        } else {
//            mPassword.setError(null);
//        }
//        return valid;
//    }
//
//
//
//    public void register(View v){
//        startActivity(new Intent(Login.this,DriverRegistration.class));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            displayLocationSettingsRequest(getApplicationContext());
        }

        getSupportActionBar().setTitle("Driver Login");

        auth = FirebaseAuth.getInstance();
        FirebaseAuthentication();

        pdialog=new ProgressDialog(this);

        reference = (EditText)findViewById(R.id.reference);
        otp = (EditText)findViewById(R.id.otp);
        err = (TextView)findViewById(R.id.err);
        send_otp = (Button)findViewById(R.id.send_otp);
        submit = (Button)findViewById(R.id.submit);

        reference.setVisibility(View.VISIBLE);
        send_otp.setVisibility(View.VISIBLE);
        otp.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);
        driver = FirebaseDatabase.getInstance().getReference("Drivers");
        otp_ref=FirebaseDatabase.getInstance().getReference("OTP");

        if (!checkPermission()){
//            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            requestPermission();
        }

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        if (log_id.contains("id")){
            if (log_id.getString("id",null) != null){
                updateUI(log_id.getString("id",null));
            }
        }
        // [START initialize_auth]
//        mAuth = FirebaseAuth.getInstance();
//
//        mPhone=(EditText)findViewById(R.id.phone);
//        mPassword=(EditText)findViewById(R.id.password);
//
//        mLogin=(Button)findViewById(R.id.login);
//        mRegistration=(Button)findViewById(R.id.registration);
//
//        mLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                signIn();
//            }
//        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        reference.setVisibility(View.VISIBLE);
//        send_otp.setVisibility(View.VISIBLE);
//        otp.setVisibility(View.GONE);
//        submit.setVisibility(View.GONE);
//
//        otp_number = null;
//    }

    private void hideProgressDialog() {
        pdialog.dismiss();
    }

    private void showProgressDialog() {
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(false);
        pdialog.setMessage("Please Wait ... ");
        pdialog.show();
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

    private void requestPermission() {

        ActivityCompat.requestPermissions(Login.this, new String[]
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
//                        Toast.makeText(Login.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void send_otp(View view) {
        err.setText("");
        //Toast.makeText(this, "Send otp clicked", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(reference.getText().toString())){
//            err.setTextColor(Color.RED);
//            err.setText("Please Enter Reference Id");
            reference.setError("Please Enter Reference ID");
        }else {
            final String ref_id = reference.getText().toString().trim();
            Log.i("OK","Reference Id : "+ref_id);
            driver.child(ref_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        if (dataSnapshot.exists()){
                            String phone = dataSnapshot.child("phone").getValue().toString();
                            if (TextUtils.isEmpty(phone)){
                                err.setTextColor(Color.RED);
                                err.setText("Mobile number is not present in the database");
                            }else {
                                showProgressDialog();
                                otp_number = String.valueOf((int)(Math.random()*9999)+1000);
//                                String otp_msg = "Enter "+otp_number+" as an otp to verify yourself. This otp is valid for only 2 mins from the time when otp was sent.";
//                                Toast.makeText(Login.this, ""+otp_number, Toast.LENGTH_LONG).show();

                                String otp_msg="\"Enter "+otp_number+" as an otp to verify yourself. This otp is valid for only 2 mins from the time when otp was sent.";
                                Log.v("TAG",otp_msg);
//
                                otp_ref.child(ref_id).setValue(String.valueOf(otp_number));
                                message = message + otp_msg;
                                numbers = numbers + phone;

                                new SendSms(otp_msg,phone).start();

                                hideProgressDialog();

                                reference.setEnabled(false);
                                send_otp.setEnabled(false);
                                send_otp.setVisibility(View.GONE);
                                otp.setVisibility(View.VISIBLE);
                                submit.setVisibility(View.VISIBLE);
                                handler = new Handler();
                                final Runnable r = new Runnable() {
                                    public void run() {
                                        reference.setEnabled(true);
                                        send_otp.setEnabled(true);
                                        send_otp.setVisibility(View.VISIBLE);
                                        otp.setVisibility(View.GONE);
                                        submit.setVisibility(View.GONE);
                                        otp.setText("");

                                        err.setTextColor(Color.RED);
                                        err.setText("Time Expired\nPlease try again");
                                    }
                                };

                                handler.postDelayed(r, 120000);
                            }
                        }else {
                            err.setTextColor(Color.RED);
                            err.setText("Invalid reference number \nPlease contact QuickLift customer care");
                        }
                    }catch (Exception e){
                        Log.e("OK","Error : "+e.getLocalizedMessage());
                        err.setTextColor(Color.RED);
                        err.setText("Server Error.\nPlease try again");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                    Toast.makeText(Login.this, "Failed database Error", Toast.LENGTH_SHORT).show();
                    err.setTextColor(Color.RED);
                    err.setText("Server Error.\nPlease try again");
                }
            });
        }

    }

    public void submit(View view) {
        err.setText("");
        if (TextUtils.isEmpty(otp.getText().toString().trim())){
            err.setTextColor(Color.RED);
            err.setText("Please enter OTP");
        }else {
            if (otp.getText().toString().trim().equalsIgnoreCase(otp_number)){
                otp_ref.child(reference.getText().toString()).removeValue();
                updateUI(reference.getText().toString().trim());
            }else {
                err.setTextColor(Color.RED);
                err.setText("Invalid OTP\nTry again");
            }
        }
    }

    private void updateUI(String user) {
        hideProgressDialog();
        if (user != null) {
            //final SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
            final SharedPreferences.Editor editor=log_id.edit();
            if (user != null){
                editor.putString("id",user);
            }else {
//                editor.putString("id","");
            }
            editor.putString("ride","");
            editor.commit();

            final SQLQueries sqlQueries=new SQLQueries(this);
            sqlQueries.deletefare();
            sqlQueries.deletelocation();
            DatabaseReference dblist= FirebaseDatabase.getInstance().getReference("Fare/Patna");
            dblist.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("excelcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/excel").getValue(Integer.class)));
                    editor.putString("sharecharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/share").getValue(Integer.class)));
                    editor.putString("fullcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/full").getValue(Integer.class)));
                    editor.putString("ratemultiplier",String.valueOf(dataSnapshot.child("RateMultiplier").getValue(Float.class)));
                    editor.putString("searchingtime",String.valueOf(dataSnapshot.child("SearchingTime").getValue(Integer.class)));
                    editor.putString("outsidetripextraamount",String.valueOf(dataSnapshot.child("OutsideTripExtraAmount").getValue(Integer.class)));
                    editor.putString("twoseatprice",String.valueOf(dataSnapshot.child("Twoseatprice").getValue(Integer.class)));
                    editor.putString("excel",String.valueOf(dataSnapshot.child("ParkingCharge/excel").getValue(Integer.class)));
                    editor.putString("fullcar",String.valueOf(dataSnapshot.child("ParkingCharge/fullcar").getValue(Integer.class)));
                    editor.putString("fullrickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/fullrickshaw").getValue(Integer.class)));
                    editor.putString("sharecar",String.valueOf(dataSnapshot.child("ParkingCharge/sharecar").getValue(Integer.class)));
                    editor.putString("sharerickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/sharerickshaw").getValue(Integer.class)));
                    editor.putString("normaltimeradius",dataSnapshot.child("NormalTimeSearchRadius").getValue(String.class));
                    editor.putString("peaktimeradius",dataSnapshot.child("PeakTimeSearchRadius").getValue(String.class));
                    editor.putString("waittime",String.valueOf(dataSnapshot.child("WaitingTime").getValue(Integer.class)));
                    editor.putString("waitingcharge",String.valueOf(dataSnapshot.child("WaitingCharge").getValue(Integer.class)));
                    editor.putString("pickupdistance",String.valueOf(dataSnapshot.child("PickupDistance").getValue(String.class)));
                    editor.putString("pickupprice",String.valueOf(dataSnapshot.child("PickupPrice").getValue(String.class)));
                    editor.commit();
                    for (DataSnapshot data:dataSnapshot.child("Package").getChildren()){
                        ArrayList<String> price=new ArrayList<String>();
                        price.add(data.child("Latitude").getValue(String.class));
                        price.add(data.child("Longitude").getValue(String.class));
                        price.add(data.child("Amount").getValue(String.class));
                        price.add(data.child("Distance").getValue(String.class));

                        sqlQueries.savelocation(price);
                    }
                    for (DataSnapshot data:dataSnapshot.child("Price").getChildren()){
                        ArrayList<String> price=new ArrayList<String>();
                        price.add(data.child("NormalTime/BaseFare/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/BaseFare/Distance").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                        price.add(data.child("NormalTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                        price.add(data.child("NormalTime/Time").getValue(String.class));

                        sqlQueries.savefare(price);
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");

                        price.clear();
                        price.add(data.child("PeakTime/BaseFare/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/BaseFare/Distance").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                        price.add(data.child("PeakTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                        price.add(data.child("PeakTime/Time").getValue(String.class));

                        sqlQueries.savefare(price);
//                    Toast.makeText(WelcomeScreen.this, ""+"hi", Toast.LENGTH_SHORT).show();
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers");
            db.child(log_id.getString("id",null)).child("veh_type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("TAG","I am Here : "+dataSnapshot.getValue().toString());
                    if (dataSnapshot.exists()) {
                        String getType = dataSnapshot.getValue().toString();
                        if (getType.equalsIgnoreCase("car")) {
                            type = "Car";
                        } else if (getType.equalsIgnoreCase("bike")) {
                            type = "Bike";
                        } else if (getType.equalsIgnoreCase("auto")) {
                            type = "Auto";
                        } else if (getType.equalsIgnoreCase("rickshaw")) {
                            type = "Rickshaw";
                        } else if (getType.equalsIgnoreCase("excel")) {
                            type = "Excel";
                        }

                        editor.putString("type", type);
                        editor.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            db.child(log_id.getString("id", null)).child("subPlan").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("TAG","I am Here : "+dataSnapshot.getValue().toString());
                    if (dataSnapshot.exists()) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubscriptionCategory/" + dataSnapshot.getValue().toString());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("amount"))
                                        editor.putString("basevalue", String.valueOf(dataSnapshot.child("amount").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("tax"))
                                        editor.putString("tax", String.valueOf(dataSnapshot.child("tax").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("mincommission"))
                                        editor.putString("mincommission", String.valueOf(dataSnapshot.child("mincommission").getValue(Integer.class)));

                                    if (dataSnapshot.hasChild("maxcommission"))
                                        editor.putString("maxcommission", String.valueOf(dataSnapshot.child("maxcommission").getValue(Integer.class)));
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fare/Patna/DriverCancelCharge");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        editor.putString("cancelcharge", dataSnapshot.getValue(Integer.class).toString());
                        editor.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            startActivity(new Intent(Login.this,Welcome.class));
            finish();
        } else {
            err.setTextColor(Color.RED);
            err.setText("Didn't find reference number");
        }
    }

    private void FirebaseAuthentication(){
        auth.signInWithEmailAndPassword(FirebaseUserId, FirebaseUserPass)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i("OK","Firebase Authentication Failed");
                        } else {
                        }
                    }
                });
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("TAG", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Login.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("TAG", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
}
