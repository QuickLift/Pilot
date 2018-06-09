package com.quickliftpilot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.SequenceModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FeedbackActivity extends AppCompatActivity {
    private static Button feed;
    SharedPreferences log_id,ride_info;
    TextView fare,cancel,total;
    private RatingBar rate_bar;
    private DatabaseReference feed_rate;
    boolean submit = false;
    String id;
    int price=0;
    private SharedPreferences pref;
    Map<String,Object> datamap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        getSupportActionBar().setTitle("Feedback");

        Log.v("TAG","feedback");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        ride_info = getApplicationContext().getSharedPreferences("ride_info",MODE_PRIVATE);
        pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
        id = getIntent().getStringExtra("customer_id").toString();

        rate_bar = (RatingBar)findViewById(R.id.feed_rate);
        feed_rate = FirebaseDatabase.getInstance().getReference("DriverFeedback/"+id);

        fare = (TextView)findViewById(R.id.fare);
        cancel = (TextView)findViewById(R.id.cancel);
        total = (TextView)findViewById(R.id.total);

        if (MapActivity.fa!=null)
            MapActivity.fa.finish();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+id);
        final DatabaseReference ongoing_rides = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
        ongoing_rides.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    datamap=(Map<String, Object>) dataSnapshot.getValue();

                    SharedPreferences.Editor editor = ride_info.edit();
                    editor.putString("state","start");
                    editor.commit();
                    DatabaseReference rides = FirebaseDatabase.getInstance().getReference("Rides");
                    DatabaseReference LastRide = FirebaseDatabase.getInstance().getReference("LastRide");
//                        getAddress(Double.parseDouble(datamap.get("st_lat")),Double.parseDouble(ride_info.getString("st_lng",null)));
//                        String source = address.toString();
//                        getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
//                        String destination = address.toString();
                    HashMap<String,Object> map= new HashMap<>();

                    fare.setText("Rs. "+datamap.get("price").toString());
                    if (!datamap.get("cancel_charge").toString().equals("0")) {
                        cancel.setText("Rs. " + datamap.get("cancel_charge").toString());
                        findViewById(R.id.cancel_text).setVisibility(View.VISIBLE);
                    }else {
                        cancel.setVisibility(View.GONE);
                        findViewById(R.id.cancel_text).setVisibility(View.GONE);
                    }
                    if (datamap.containsKey("offer") && !datamap.get("offer").toString().equals("0")) {
                        ((TextView)findViewById(R.id.offer)).setText("Rs. " + datamap.get("offer").toString()+" )");
                        findViewById(R.id.offer_text).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.offer).setVisibility(View.GONE);
                        findViewById(R.id.offer_text).setVisibility(View.GONE);
                    }
                    if (datamap.containsKey("parking_price") && !datamap.get("parking_price").toString().equals("0")) {
                        ((TextView)findViewById(R.id.parking)).setText("Rs. " + datamap.get("parking_price").toString()+" )");
                        findViewById(R.id.parking_text).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.parking).setVisibility(View.GONE);
                        findViewById(R.id.parking_text).setVisibility(View.GONE);
                    }

                    Float tot=Float.valueOf(datamap.get("price").toString())+Float.valueOf(datamap.get("cancel_charge").toString());
                    total.setText("Rs. "+(int)((float)tot));

                    price=Integer.parseInt(datamap.get("price").toString());

                    map.put("amount",datamap.get("price").toString());
                    map.put("customerid",datamap.get("customer_id").toString());
                    map.put("destination",datamap.get("destination").toString());
                    map.put("driver",log_id.getString("id",null));
                    map.put("source",datamap.get("source").toString());
                    map.put("discount",datamap.get("offer").toString());
                    map.put("cancel_charge",datamap.get("cancel_charge").toString());
                    map.put("paymode",datamap.get("paymode").toString());
                    if (datamap.containsKey("parking_price"))
                        map.put("parking",datamap.get("parking_price").toString());
                    else
                        map.put("parking","0");
                    map.put("seat",datamap.get("seat").toString());
                    map.put("time",new Date().toString());
                    map.put("status","Completed");

                    String key = rides.push().getKey();
                    rides.child(key).setValue(map);

                    HashMap<String,Object> last_ride = new HashMap<>();
                    last_ride.put("date",(new Date()).toString());
                    last_ride.put("destination",datamap.get("destination").toString());
                    last_ride.put("driver",log_id.getString("id",null));
                    last_ride.put("lat",datamap.get("en_lat").toString());
                    last_ride.put("lng",datamap.get("en_lng").toString());
                    last_ride.put("rideid",key);
                    last_ride.put("status","");

                    LastRide.child(datamap.get("customer_id").toString()).setValue(last_ride);

                    ref.child("resp").setValue("Trip Ended");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        feed = (Button)findViewById(R.id.feed_btn);
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog pdialog=new ProgressDialog(FeedbackActivity.this);
                pdialog.setMessage("Saving. Please wait ...");
                pdialog.setIndeterminate(true);
                pdialog.setCancelable(false);
                pdialog.show();
                submit = true;
                feed_rate.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Map<String,Object> feedback= (Map<String,Object>)dataSnapshot.getValue();
                            int no = Integer.parseInt(feedback.get("no").toString());
                            Float rate = Float.parseFloat(feedback.get("rate").toString());
                            rate = rate * no;
                            Float curr_rate = (Float) ((rate+rate_bar.getRating())/(no+1));

                            HashMap<String,String> current_feed = new HashMap<>();
                            current_feed.put("no",Integer.toString(no+1));
                            current_feed.put("rate",curr_rate.toString());

                            feed_rate.setValue(current_feed);
                            submit = true;

                        }else {
                            HashMap<String,String> current_feed = new HashMap<>();
                            current_feed.put("no",Integer.toString(1));
                            current_feed.put("rate",Float.toString(rate_bar.getRating()));
                            feed_rate.setValue(current_feed);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("OK","failed to create reference");
                    }
                });
                GregorianCalendar gregorianCalendar=new GregorianCalendar();
                String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
                String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
                String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
                final String formateDate = date+"-"+month+"-"+year;

                final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
                driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()){
                            Map<String,Object> map = (Map<String,Object>)data.getValue();
                            Float earn=null,cash=null,offer=null,cancel_charge=null;
                            int confirm=0;
                            if (map.containsKey("book"))
                                confirm = Integer.parseInt(map.get("book").toString());
                            if (map.containsKey("earn"))
                                earn = Float.parseFloat(map.get("earn").toString());
                            if (map.containsKey("cash"))
                                cash = Float.parseFloat(map.get("cash").toString());
                            if (map.containsKey("offer"))
                                offer = Float.parseFloat(map.get("offer").toString());
                            if (map.containsKey("cancel_charge"))
                                cancel_charge = Float.parseFloat(map.get("cancel_charge").toString());

                            confirm = confirm+1;
                            if (earn!=null)
                                earn = earn + Float.parseFloat(String.valueOf(price));
                            else
                                earn= Float.parseFloat(String.valueOf(price));
                            if (offer!=null)
                                offer = offer + Float.parseFloat(datamap.get("offer").toString());
                            else
                                offer=Float.parseFloat(datamap.get("offer").toString());
                            if (cancel_charge!=null)
                                cancel_charge = cancel_charge + Float.parseFloat(datamap.get("cancel_charge").toString());
                            else
                                cancel_charge=Float.parseFloat(datamap.get("cancel_charge").toString());

                            if (datamap.get("paymode").toString().equals("Cash")){
                                if (cash!=null)
                                    cash = cash + Float.parseFloat(String.valueOf(price));
                                else
                                    cash=Float.parseFloat(String.valueOf(price));
                            }

                            String key = data.getKey();
                            try {
                                driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                                driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                                driver_acc.child(key).child("cash").setValue(Float.toString(cash));
                                driver_acc.child(key).child("offer").setValue(Float.toString(offer));
                                driver_acc.child(key).child("cancel_charge").setValue(Float.toString(cancel_charge));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (!datamap.get("cancel_charge").toString().equals("0")){
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("CustomerPendingCharges/"+id+"/cancel_req");
                    reference.child("full").setValue("0");
                    reference.child("share").setValue("0");
                    reference.child("excel").setValue("0");
                }

                DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
                resp.removeValue();
                DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
                cus.removeValue();

                Stack<SequenceModel> stack = new SequenceStack().getStack();
                if (!stack.isEmpty()){
//                stack.pop();
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","ride");
                    editor.commit();
                    Log.v("TAG","Status feedback");
                    SharedPreferences.Editor editor1=pref.edit();
                    editor1.putBoolean("status", true);
                    editor1.commit();
//                    Toast.makeText(FeedbackActivity.this, ""+stack.size(), Toast.LENGTH_SHORT).show();

                    if (pdialog.isShowing())
                        pdialog.dismiss();
                    startActivity(new Intent(FeedbackActivity.this,MapActivity.class));
                    finish();
                }else {
                    Log.v("TAG","Status Removed");
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","");
                    editor.commit();
                    DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                    tripstatus.removeValue();
//                DatabaseReference working= FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("id",null));
//                working.removeValue();
                    DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
                    seat_data.removeValue();

                    GPSTracker gps = new GPSTracker(FeedbackActivity.this);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(log_id.getString("id",null),new GeoLocation(gps.getLatitude(),gps.getLongitude()));

                    SharedPreferences.Editor editor1=pref.edit();
                    editor1.putBoolean("status", true);
                    editor1.commit();

                    if (Welcome.WelcomeActivity!=null)
                        Welcome.WelcomeActivity.finish();

                    if (pdialog.isShowing())
                        pdialog.dismiss();
                    Intent intent=new Intent(FeedbackActivity.this,Welcome.class);
                    intent.putExtra("status","true");
                    startActivity(intent);
                    finish();
                }
                //startActivity(new Intent(FeedbackActivity.this,Welcome.class));
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStop() {
        if (!submit){
            feed_rate.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Map<String,Object> feedback= (Map<String,Object>)dataSnapshot.getValue();
                        int no = Integer.parseInt(feedback.get("no").toString());
                        Float rate = Float.parseFloat(feedback.get("rate").toString());
                        rate = rate * no;
                        Float curr_rate = (Float) ((rate+5)/(no+1));

                        HashMap<String,String> current_feed = new HashMap<>();
                        current_feed.put("no",Integer.toString(no+1));
                        current_feed.put("rate",curr_rate.toString());

                        feed_rate.setValue(current_feed);
                    }else {
                        HashMap<String,String> current_feed = new HashMap<>();
                        current_feed.put("no",Integer.toString(1));
                        current_feed.put("rate",Float.toString(5));
                        feed_rate.setValue(current_feed);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("OK","failed to create reference");
                }
            });
            GregorianCalendar gregorianCalendar=new GregorianCalendar();
            String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
            String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
            String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
            final String formateDate = date+"-"+month+"-"+year;

            final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
            driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        Map<String,Object> map = (Map<String,Object>)data.getValue();
                        Float earn=null,cash=null,offer=null,cancel_charge=null;
                        int confirm=0;
                        if (map.containsKey("book"))
                            confirm = Integer.parseInt(map.get("book").toString());
                        if (map.containsKey("earn"))
                            earn = Float.parseFloat(map.get("earn").toString());
                        if (map.containsKey("cash"))
                            cash = Float.parseFloat(map.get("cash").toString());
                        if (map.containsKey("offer"))
                            offer = Float.parseFloat(map.get("offer").toString());
                        if (map.containsKey("cancel_charge"))
                            cancel_charge = Float.parseFloat(map.get("cancel_charge").toString());

                        confirm = confirm+1;
                        if (earn!=null)
                            earn = earn + Float.parseFloat(String.valueOf(price));
                        else
                            earn= Float.parseFloat(String.valueOf(price));
                        if (offer!=null)
                            offer = offer + Float.parseFloat(datamap.get("offer").toString());
                        else
                            offer=Float.parseFloat(datamap.get("offer").toString());
                        if (cancel_charge!=null)
                            cancel_charge = cancel_charge + Float.parseFloat(datamap.get("cancel_charge").toString());
                        else
                            cancel_charge=Float.parseFloat(datamap.get("cancel_charge").toString());

                        if (datamap.get("paymode").toString().equals("Cash")){
                            if (cash!=null)
                                cash = cash + Float.parseFloat(String.valueOf(price));
                            else
                                cash=Float.parseFloat(String.valueOf(price));
                        }

                        String key = data.getKey();
                        try {
                            driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                            driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                            driver_acc.child(key).child("cash").setValue(Float.toString(cash));
                            driver_acc.child(key).child("offer").setValue(Float.toString(offer));
                            driver_acc.child(key).child("cancel_charge").setValue(Float.toString(cancel_charge));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
            resp.removeValue();
            DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
            cus.removeValue();

            Stack<SequenceModel> stack = new SequenceStack().getStack();
            if (!stack.isEmpty()){
//                stack.pop();
                SharedPreferences.Editor editor=log_id.edit();
                editor.putString("ride","ride");
                editor.commit();
                Log.v("TAG","Status feedback");
                SharedPreferences.Editor editor1=pref.edit();
                editor1.putBoolean("status", true);
                editor1.commit();
//                Toast.makeText(FeedbackActivity.this, ""+stack.size(), Toast.LENGTH_SHORT).show();

                startActivity(new Intent(FeedbackActivity.this,MapActivity.class));
                finish();
            }else {
                Log.v("TAG","Status Removed");
                SharedPreferences.Editor editor=log_id.edit();
                editor.putString("ride","");
                editor.commit();
                DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                tripstatus.removeValue();
//                DatabaseReference working= FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("id",null));
//                working.removeValue();
                DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
                seat_data.removeValue();

                GPSTracker gps = new GPSTracker(FeedbackActivity.this);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(log_id.getString("id",null),new GeoLocation(gps.getLatitude(),gps.getLongitude()));

                SharedPreferences.Editor editor1=pref.edit();
                editor1.putBoolean("status", true);
                editor1.commit();

                if (Welcome.WelcomeActivity!=null)
                    Welcome.WelcomeActivity.finish();
                Intent intent=new Intent(FeedbackActivity.this,Welcome.class);
                intent.putExtra("status","true");
                startActivity(intent);
                finish();
            }
        }
        super.onStop();
    }
}