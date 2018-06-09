package com.quickliftpilot.activities;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static Button cancel,confirm;
    private static TextView name,contact;
    private static EditText mobile,email,address;
    private static RatingBar rate;
    private static SharedPreferences login;
    private static DatabaseReference db;
    private CircleImageView image;
    private Bitmap photo;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        login = getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        db = FirebaseDatabase.getInstance().getReference("Drivers");

        cancel = (Button)findViewById(R.id.cancel_btn);
        confirm = (Button)findViewById(R.id.confirm_btn);
        name = (TextView)findViewById(R.id.driver_name);
        contact = (TextView)findViewById(R.id.driver_contact);
        mobile = (EditText)findViewById(R.id.mobile_num);
        email = (EditText)findViewById(R.id.email);
        address = (EditText)findViewById(R.id.address);
        rate = (RatingBar)findViewById(R.id.rateBar);
        rate.setRating(0);
        rate.setIsIndicator(true);

        image = (CircleImageView)findViewById(R.id.image);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ProfileActivity.this,Welcome.class);
//                startActivity(intent);
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("phone",mobile.getText().toString());
                    map.put("email",email.getText().toString());
                    map.put("address",address.getText().toString());

                    db.child(login.getString("id",null)).updateChildren(map);
                    finish();
                }
            }
        });
    }

    private boolean validate(){
        boolean status = false;
        if (TextUtils.isEmpty(mobile.getText().toString().trim())){
            mobile.setError("Mobile number should not be empty");
        }else if (mobile.getText().toString().trim().length() != 10){
            mobile.setError("Mobile number should be 10 digit");
        }else if (TextUtils.isEmpty(email.getText().toString().trim())){
            email.setError("Email should not be empty");
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()){
            email.setError("Please enter a valid email address");
        }else if (TextUtils.isEmpty(address.getText().toString().trim())){
            address.setError("Address should not be empty");
        }else {
            status = true;
        }
        return status;
    }

    @Override
    protected void onResume() {
        db.child(login.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();

//                    Log.i("TAG","name : "+map.get("name").toString());
                    name.setText(map.get("name").toString());
                    contact.setText(map.get("phone").toString());
                    mobile.setText(map.get("phone").toString());
                    email.setText(map.get("email").toString());
                    address.setText(map.get("address").toString());
                    rate.setRating(Float.parseFloat(map.get("rate").toString()));

                    byte[] decodedString = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                    photo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image.setImageBitmap(photo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        super.onResume();

    }

    public void edit_add(View view) {
        mobile.setEnabled(false);
        email.setEnabled(false);
        address.setEnabled(true);
    }

    public void edit_email(View view) {
        mobile.setEnabled(false);
        email.setEnabled(true);
        address.setEnabled(false);
    }

    public void edit_mobile(View view) {
        mobile.setEnabled(true);
        email.setEnabled(false);
        address.setEnabled(false);
    }
}
