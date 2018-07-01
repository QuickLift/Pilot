package com.quickliftpilot.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quickliftpilot.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OTPActivity extends AppCompatActivity {
    private static EditText otp;
    private static Button otp_btn;
    private SharedPreferences ride_info;
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        getSupportActionBar().setTitle("OTP");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);

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
                        SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
                        DatabaseReference db= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
                        db.child(getIntent().getStringExtra("id")+"/started").setValue(sdf.format(new Date()));
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
}
