package com.quickliftpilot.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.quickliftpilot.services.OngoingRideService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class RidesActivity extends AppCompatActivity {
    private Cursor cursor;
    ListView list;
    DatabaseReference db;
    private SharedPreferences log_id;
    ArrayList<Map<String,Object>> ride_list=new ArrayList<Map<String,Object>>();
    Button curr_ride;
    private ProgressDialog progressDialog;
    ImageView no_ride;
    public static Activity RideActivity=null;
    private ArrayList<String> ridekey=new ArrayList<>();

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
    protected void onStart() {
        super.onStart();
        DatabaseReference driver_status = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        driver_status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    curr_ride.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides);

        getSupportActionBar().setTitle("Ride List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        RideActivity=this;

        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...\nLoading Rides");

        curr_ride = (Button)findViewById(R.id.curr_ride);
        curr_ride.setVisibility(View.GONE);
        curr_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!progressDialog.isShowing()){
                    progressDialog.show();
                }
                startService(new Intent(RidesActivity.this, OngoingRideService.class));
//                finish();
            }
        });
        no_ride = (ImageView)findViewById(R.id.no_ride);
        no_ride.setVisibility(View.GONE);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("Rides");

        list=(ListView)findViewById(R.id.list);

        if (!progressDialog.isShowing()){
            progressDialog.show();
        }

        db.orderByChild("driver").equalTo(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() <= 0){
                    no_ride.setVisibility(View.VISIBLE);
                }
                ride_list.clear();
                ridekey.clear();

                for (DataSnapshot data:dataSnapshot.getChildren()){
                    ride_list.add((Map<String, Object>) data.getValue());
                    ridekey.add(data.getKey());
                    //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(CustomerRides.this, ride_list.get(ride_list.size()-1).get("time").toString(), Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                list.setAdapter(new CustomAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(RidesActivity.this,BillDetails.class);
                intent.putExtra("rideid",ridekey.get(ridekey.size()-1-position));
                startActivity(intent);
            }
        });
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ride_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view=getLayoutInflater().inflate(R.layout.ride_info,null);

            final ImageView img=(ImageView)view.findViewById(R.id.image);
            TextView time=(TextView)view.findViewById(R.id.timestamp);
            TextView source=(TextView)view.findViewById(R.id.source);
            TextView destination=(TextView)view.findViewById(R.id.destination);
            TextView amount=(TextView)view.findViewById(R.id.amount);
//            TextView offer=(TextView)view.findViewById(R.id.offer);
//            TextView parking=(TextView)view.findViewById(R.id.parking);
//            TextView ride=(TextView)view.findViewById(R.id.ride);
            final TextView name=(TextView)view.findViewById(R.id.name);
            TextView status = (TextView)view.findViewById(R.id.sta);

            time.setText(ride_list.get(ride_list.size()-position-1).get("time").toString());
            source.setText(ride_list.get(ride_list.size()-position-1).get("source").toString());
            destination.setText(ride_list.get(ride_list.size()-position-1).get("destination").toString());
            float charge=0;
            if (ride_list.get(ride_list.size()-position-1).containsKey("cancel_charge")) {
                charge = Float.valueOf(ride_list.get(ride_list.size() - position - 1).get("amount").toString()) +
                        Float.valueOf(ride_list.get(ride_list.size() - position - 1).get("cancel_charge").toString());
            }
            else {
                charge = Float.valueOf(ride_list.get(ride_list.size() - position - 1).get("amount").toString());
            }
            float val=Float.valueOf(ride_list.get(ride_list.size() - position - 1).get("amount").toString())+Float.valueOf(ride_list.get(ride_list.size() - position - 1).get("discount").toString());
            amount.setText("Rs. "+val);
//            if (ride_list.get(ride_list.size()-position-1).containsKey("discount"))
//                offer.setText("Rs. "+ride_list.get(ride_list.size()-position-1).get("discount").toString()+" (Offer)");
//            else
//                offer.setText("Rs. 0"+" (Offer)");
//            if (ride_list.get(ride_list.size()-position-1).containsKey("parking"))
//                parking.setText("Rs. "+ride_list.get(ride_list.size()-position-1).get("parking").toString()+" (Parking)");
//            else
//                parking.setText("Rs. 0 (Parking)");
//            if (ride_list.get(ride_list.size()-position-1).containsKey("seat")) {
//                if (ride_list.get(ride_list.size() - position - 1).get("seat").toString().equals("full"))
//                    ride.setText("Full Ride");
//                else
//                    ride.setText(ride_list.get(ride_list.size() - position - 1).get("seat").toString()+" seats");
//            }
            try{
                status.setText(ride_list.get(ride_list.size()-position-1).get("status").toString());
            }catch (Exception e){
                status.setText("Unknown");
            }
            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Users");
            dref.child(ride_list.get(ride_list.size()-position-1).get("customerid").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                    //veh.setText(map.get("veh_type").toString()+" , "+map.get("veh_num").toString());
                    name.setText(map.get("name").toString());
                    if (!map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        img.setImageBitmap(decbyte);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return view;
        }
    }
}
