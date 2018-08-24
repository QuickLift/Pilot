package com.quickliftpilot.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {
    private static Button fare_charge;
    private static TextView fare_text;
    private static EditText address;
    private static ImageButton center,office;
    private static ArrayList<String> phone=new ArrayList<>();
    private static String text = "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200";

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
        setContentView(R.layout.activity_help);

        getSupportActionBar().setTitle(R.string.Welcome_Help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        office = (ImageButton)findViewById(R.id.call_office);
        center = (ImageButton)findViewById(R.id.call_center);

        fare_charge = (Button)findViewById(R.id.fare_button);
        fare_text = (TextView)findViewById(R.id.fare_text);
        address = (EditText) findViewById(R.id.address);
        fare_text.setText(text);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("EmergencyContacts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    phone.clear();
                    address.setText(dataSnapshot.child("Address").getValue().toString());
                    for (DataSnapshot data:dataSnapshot.child("Contacts").getChildren()){
//                        ((EditText)findViewById(R.id.office_text)).setText("");
                        phone.add(data.child("phone").getValue().toString());
                    }
                    if (phone.size()==0){
                        ((EditText)findViewById(R.id.office_text)).setVisibility(View.GONE);
                        ((EditText)findViewById(R.id.support_text)).setVisibility(View.GONE);
                    }
                    else if (phone.size()==1){
                        ((EditText)findViewById(R.id.office_text)).setText(phone.get(0));
                        ((EditText)findViewById(R.id.support_text)).setVisibility(View.GONE);
                    }
                    else {
                        ((EditText) findViewById(R.id.office_text)).setText(phone.get(0));
                        ((EditText) findViewById(R.id.support_text)).setText(phone.get(1));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fare_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fare_text.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_top);
                fare_text.startAnimation(animation);
            }
        });

        office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_CALL);
                dialIntent.setData(Uri.parse("tel:"+((EditText)findViewById(R.id.office_text)).getText().toString()));
                startActivity(dialIntent);
            }
        });

        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_CALL);
                dialIntent.setData(Uri.parse("tel:"+((EditText)findViewById(R.id.support_text)).getText().toString()));
                startActivity(dialIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
