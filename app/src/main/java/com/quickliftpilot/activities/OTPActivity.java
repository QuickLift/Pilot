package com.quickliftpilot.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.GetDistance;
import com.quickliftpilot.Util.GetPriceData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OTPActivity extends AppCompatActivity {
    private static EditText otp;
    private static Button otp_btn;
    private SharedPreferences ride_info;
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private SharedPreferences log_id;
    DatabaseReference db;
    String latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        getSupportActionBar().setTitle("OTP");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+getIntent().getStringExtra("id"));

        final ProgressDialog dialog=new ProgressDialog(this);
        dialog.setMessage("Please Wait !!!");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("d_lat").exists() && dataSnapshot.child("d_lng").exists()){
                        latitude=dataSnapshot.child("d_lat").getValue().toString();
                        longitude=dataSnapshot.child("d_lng").getValue().toString();
                    }
                    dialog.dismiss();
                }
                else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        otp = (EditText)findViewById(R.id.otp);
        otp_btn = (Button)findViewById(R.id.otp_btn);

        otp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otp.getText().toString().isEmpty()){
                    otp.setError("Please enter otp first");
                }else {
                    int num = Integer.parseInt(otp.getText().toString());
                    if (num == Integer.parseInt(getIntent().getStringExtra("otp"))){
                        db.child("started").setValue(sdf.format(new Date()));

                        Object[] dataTransfer = new Object[5];
                        String url = getDirectionsUrltwoplaces(latitude, longitude);
                        GetDistance getDirectionsData = new GetDistance();
                        dataTransfer[0] = url;
                        dataTransfer[1] = db;
                        dataTransfer[2] = log_id;
                        getDirectionsData.execute(dataTransfer);

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",true);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }else {
                        otp.setError("OTP is incorrect \nEnter again");
                    }
                }
            }
        });
    }

    private String getDirectionsUrltwoplaces(String st_lt,String st_ln) {
        GPSTracker gpsTracker=new GPSTracker(this);

//        Log.v("DISTANCE",""+gpsTracker.getLatitude()+" , "+gpsTracker.getLongitude());
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+st_lt+","+st_ln);
        googleDirectionsUrl.append("&destination="+log_id.getString("cur_lat",null)+","+log_id.getString("cur_lng",null));
        googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
        return googleDirectionsUrl.toString();
    }
}
