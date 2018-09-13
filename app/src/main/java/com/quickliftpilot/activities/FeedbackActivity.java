package com.quickliftpilot.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.GenerateLog;
import com.quickliftpilot.Util.GetPriceData;
import com.quickliftpilot.Util.RequestDetails;
import com.quickliftpilot.Util.SQLQueries;
import com.quickliftpilot.Util.SendSms;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.SequenceModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.services.ShareRideCheckingService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    String id,phone="",name;
    int price=0;
    private SharedPreferences pref;
    Map<String,Object> datamap;
    GenerateLog driver_log=new GenerateLog();
    String tag="FB";
    boolean status1,status2;
    int vehicle_case=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        getSupportActionBar().setTitle(R.string.Feedback);

        driver_log.appendLog(tag,"FAP");

//        Log.v("TAG","feedback");
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        ride_info = getApplicationContext().getSharedPreferences("ride_info",MODE_PRIVATE);
        pref = getApplicationContext().getSharedPreferences("loginPref",MODE_PRIVATE);
        id = getIntent().getStringExtra("customer_id").toString();
//        phone = getIntent().getStringExtra("phone").toString();
        if (getIntent().hasExtra("phone"))
            phone = getIntent().getStringExtra("phone").toString();
        name = getIntent().getStringExtra("name").toString();

        ((TextView)findViewById(R.id.feed_text)).setText("Rate "+name);

        rate_bar = (RatingBar)findViewById(R.id.feed_rate);
        feed_rate = FirebaseDatabase.getInstance().getReference("DriverFeedback/"+id);

        fare = (TextView)findViewById(R.id.fare);
        cancel = (TextView)findViewById(R.id.cancel);
        total = (TextView)findViewById(R.id.total);

        if (MapActivity.fa!=null)
            MapActivity.fa.finish();

        status1 = haveNetworkConnection();
        status2 = hasActiveInternetConnection();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+id);
        final DatabaseReference ongoing_rides = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);

        if (status1 && status2) {
            ongoing_rides.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        datamap = (Map<String, Object>) dataSnapshot.getValue();
                        float final_price = 0;

                        SharedPreferences.Editor editor = ride_info.edit();
                        editor.putString("state", "start");
                        editor.commit();
                        DatabaseReference rides = FirebaseDatabase.getInstance().getReference("Rides");
                        DatabaseReference LastRide = FirebaseDatabase.getInstance().getReference("LastRide");
//                        getAddress(Double.parseDouble(datamap.get("st_lat")),Double.parseDouble(ride_info.getString("st_lng",null)));
//                        String source = address.toString();
//                        getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
//                        String destination = address.toString();
                        HashMap<String, Object> map = new HashMap<>();

//                    fare.setText("Rs. "+datamap.get("price").toString());
                        if (!datamap.get("cancel_charge").toString().equals("0")) {
                            cancel.setText("Rs. " + datamap.get("cancel_charge").toString());
                            final_price += Float.valueOf(datamap.get("cancel_charge").toString());
                            Log.v("PRICE", "" + final_price);
                            findViewById(R.id.cancel_text).setVisibility(View.VISIBLE);
                        } else {
                            cancel.setVisibility(View.GONE);
                            findViewById(R.id.cancel_text).setVisibility(View.GONE);
                        }
//                    if (datamap.containsKey("offer") && !datamap.get("offer").toString().equals("0")) {
//                        ((TextView)findViewById(R.id.offer)).setText("Rs. " + datamap.get("offer").toString());
//                        final_price-=Float.valueOf(datamap.get("offer").toString());
//                        Log.v("PRICE",""+final_price);
//                        findViewById(R.id.offer_text).setVisibility(View.VISIBLE);
//                    }else {
//                        findViewById(R.id.offer).setVisibility(View.GONE);
//                        findViewById(R.id.offer_text).setVisibility(View.GONE);
//                    }
                        if (datamap.containsKey("parking_price") && !datamap.get("parking_price").toString().equals("0")) {
                            ((TextView) findViewById(R.id.parking)).setText("Rs. " + datamap.get("parking_price").toString());
                            final_price += Float.valueOf(datamap.get("parking_price").toString());
                            Log.v("PRICE", "" + final_price);
                            findViewById(R.id.parking_text).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.parking).setVisibility(View.GONE);
                            findViewById(R.id.parking_text).setVisibility(View.GONE);
                        }

                        float tot = Float.valueOf(datamap.get("price").toString()) + Float.valueOf(datamap.get("cancel_charge").toString());

                        price = Integer.parseInt(datamap.get("price").toString());

                        map.put("customerid", datamap.get("customer_id").toString());
                        map.put("destination", datamap.get("destination").toString());
                        map.put("driver", log_id.getString("id", null));
                        map.put("source", datamap.get("source").toString());
                        if (datamap.containsKey("offer"))
                            map.put("discount", datamap.get("offer").toString());
                        map.put("cancel_charge", datamap.get("cancel_charge").toString());
                        map.put("paymode", datamap.get("paymode").toString());
                        if (datamap.containsKey("parking_price"))
                            map.put("parking", datamap.get("parking_price").toString());
                        else
                            map.put("parking", "0");
                        map.put("seat", datamap.get("seat").toString());
                        map.put("time", new Date().toString());
                        if (datamap.containsKey("started"))
                            map.put("start_time", datamap.get("started").toString());
                        else
                            map.put("start_time", "");
                        if (datamap.containsKey("pickup_distance"))
                            map.put("pickup_distance", datamap.get("pickup_distance").toString());
                        else
                            map.put("pickup_distance", "0");
                        map.put("status", "Completed");

//                    float total = 0;
//                    map.put("waiting",datamap.get("seat").toString());
//                    map.put("timing",new Date().toString());
                        String key = rides.push().getKey();

                        if (datamap.containsKey("version") && datamap.containsKey("seat") && datamap.get("seat").toString().equals("full")) {
                            if (dataSnapshot.hasChild("located") && dataSnapshot.hasChild("started") && dataSnapshot.hasChild("waitcharge")) {
                                try {
                                    Date date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dataSnapshot.child("started").getValue().toString());
                                    Date date2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dataSnapshot.child("located").getValue().toString());

                                    long diff = date1.getTime() - date2.getTime();
//                            int days = (int) (diff / (1000*60*60*24));
//                            int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
                                    int min = (int) (diff / (1000 * 60));
                                    if (dataSnapshot.hasChild("waittime") && (min > Integer.parseInt(dataSnapshot.child("waittime").getValue().toString()))) {
//                                tot+=(Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString())*(float)(min-Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())));
                                        map.put("waiting", String.valueOf((int) (Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString()) * (float) (min - Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())))));
                                        final_price += (Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString()) * (float) (min - Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())));
                                        Log.v("PRICE", "" + final_price);
                                    } else {
                                        map.put("waiting", "0");
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                map.put("waiting", "0");
                            }
                            int time = 0;
                            if (dataSnapshot.hasChild("ended") && dataSnapshot.hasChild("started") && dataSnapshot.hasChild("timecharge") && dataSnapshot.hasChild("triptime")) {
                                try {
                                    Date date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dataSnapshot.child("started").getValue().toString());
                                    Date date2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dataSnapshot.child("ended").getValue().toString());

                                    long diff = date2.getTime() - date1.getTime();
//                            int days = (int) (diff / (1000*60*60*24));
//                            int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
                                    int min = (int) (diff / (1000 * 60));
                                    time = min;
                                    if (min > Integer.parseInt(dataSnapshot.child("triptime").getValue().toString())) {
                                        if ((0.25 * min) > 10) {
                                            int dif = min - Integer.parseInt(dataSnapshot.child("triptime").getValue().toString());
                                            if (dif < (int) (0.25 * min)) {
                                                min = Integer.parseInt(dataSnapshot.child("triptime").getValue().toString());
                                            } else {
                                                min = min - (int) (0.25 * min);
                                            }
                                        } else {
                                            int dif = min - Integer.parseInt(dataSnapshot.child("triptime").getValue().toString());
                                            if (dif < 10) {
                                                min = Integer.parseInt(dataSnapshot.child("triptime").getValue().toString());
                                            } else {
                                                min = min - 10;
                                            }
                                        }
                                    }
                                    final_price += ((float) min) * Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString());
//                                Log.v("PRICE", "" + final_price);
                                    map.put("triptime", String.valueOf(time));
                                    map.put("timing", String.valueOf((int) (Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString()) * (float) (min))));
//                            if ((min>Integer.parseInt(dataSnapshot.child("triptime").getValue().toString()))){
//                                tot+=(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min-Integer.parseInt(dataSnapshot.child("triptime").getValue().toString())));
//                                map.put("timing", (int)(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min)));
//                            }else {
//                                map.put("timing", (int)(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min)));
//                            }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                map.put("timing", "0");
                            }
//                    price=(int) (((float)tot)-Float.valueOf(datamap.get("cancel_charge").toString()));
//                    map.put("amount",String.valueOf(price));
//                    total.setText("Rs. "+(int)((float)tot));

                            Log.v("TAG", "if condition");
                            SQLQueries sqlQueries = new SQLQueries(FeedbackActivity.this);

                            Cursor cursor = sqlQueries.retrievefare();
                            Object[] dataTransfer = new Object[25];
                            String url = null;
                            if (time > (int) (float) (Float.valueOf(dataSnapshot.child("triptime").getValue().toString()) * 1.2)) {
                                url = getDirectionsUrlthreeplaces(datamap.get("st_lat").toString(), datamap.get("st_lng").toString(), datamap.get("en_lat").toString(), datamap.get("en_lng").toString());
                            } else {
                                url = getDirectionsUrltwoplaces(datamap.get("st_lat").toString(), datamap.get("st_lng").toString());
                            }
                            GetPriceData getDirectionsData = new GetPriceData();
                            dataTransfer[0] = url;
                            dataTransfer[1] = fare;
                            dataTransfer[2] = final_price;
                            dataTransfer[3] = map;
                            dataTransfer[4] = key;
                            dataTransfer[5] = rides;
                            dataTransfer[6] = datamap.get("veh_type").toString();
                            dataTransfer[7] = cursor;
                            dataTransfer[8] = FeedbackActivity.this;
                            dataTransfer[9] = total;
                            dataTransfer[10] = datamap.get("cancel_charge").toString();
                            dataTransfer[11] = price;
                            dataTransfer[12] = ref;
                            dataTransfer[13] = ongoing_rides;
                            dataTransfer[14] = log_id;
                            if (datamap.containsKey("parking_price"))
                                dataTransfer[15] = (int) (float) Float.parseFloat(datamap.get("parking_price").toString());
                            else
                                dataTransfer[15] = 0;
                            if (datamap.containsKey("offer")) {
                                dataTransfer[16] = (int) (float) Float.parseFloat(datamap.get("offer").toString());
                            } else if (datamap.containsKey("offer_upto")) {
                                dataTransfer[16] = (int) (float) Float.parseFloat(datamap.get("offer_upto").toString());
                            } else {
                                dataTransfer[16] = 0;
                            }
                            if (datamap.containsKey("offer_disc")) {
                                dataTransfer[17] = (int) (float) Float.parseFloat(datamap.get("offer_disc").toString());
                            } else {
                                dataTransfer[17] = 0;
                            }
                            dataTransfer[18] = ((TextView) findViewById(R.id.offer));
                            dataTransfer[19] = ((TextView) findViewById(R.id.offer_text));
                            if (datamap.containsKey("offer_type"))
                                dataTransfer[20] = datamap.get("offer_type").toString();
                            else
                                dataTransfer[20] = "";
                            dataTransfer[21] = vehicle_case;
                            driver_log.appendLog(tag, "Get Price Data called..");
                            getDirectionsData.execute(dataTransfer);

                        } else {
                            Log.v("TAG", "else condition");
                            map.put("waiting", "0");
                            map.put("timing", "0");

                            float offer_val = 0;
                            if (datamap.containsKey("cancel_charge"))
                                price = (int) (((float) tot) - Float.valueOf(datamap.get("cancel_charge").toString()));
                            else
                                price = (int) ((float) tot);
                            SharedPreferences.Editor editor1 = log_id.edit();
                            editor1.putString("saveprice", String.valueOf(price));
                            if (datamap.containsKey("triptime")){
                                map.put("triptime",datamap.get("triptime").toString());
                            }
                            if (datamap.containsKey("tripdistance")){
                                map.put("trip_distance",datamap.get("tripdistance").toString());
                            }
                            if (datamap.containsKey("tax")){
                                map.put("tax",datamap.get("tax").toString());
                            }
                            if (datamap.containsKey("offer")) {
                                editor1.putString("offervalue", datamap.get("offer").toString());
                                offer_val = Integer.parseInt(datamap.get("offer").toString());
                            } else if (datamap.containsKey("offer_disc")) {
                                if (!datamap.get("offer_disc").toString().equals("0")) {
//                                editor1.putString("offervalue", "0");
//                                offer_val=((float)tot)*Integer.parseInt(datamap.get("offer_disc").toString())/100;
//                                if (offer_val>Float.parseFloat(datamap.get("offer_upto").toString())){
//                                    offer_val=Float.parseFloat(datamap.get("offer_upto").toString());
//                                }
                                    offer_val = Float.parseFloat(datamap.get("offer_value").toString());
                                    editor1.putString("offervalue", String.valueOf((int) offer_val));
                                } else {
                                    editor1.putString("offervalue", "0");
                                }
                            } else {
                                editor1.putString("offervalue", "0");
                            }
                            editor1.commit();

//                        if (price<=(int) offer_val)
//                            offer_val=price;

                            map.put("discount", String.valueOf((int) offer_val));
                            if (offer_val == 0) {
                                ((TextView) findViewById(R.id.offer)).setVisibility(View.GONE);
                                ((TextView) findViewById(R.id.offer_text)).setVisibility(View.GONE);
                            } else {
                                ((TextView) findViewById(R.id.offer)).setVisibility(View.VISIBLE);
                                ((TextView) findViewById(R.id.offer_text)).setVisibility(View.VISIBLE);
                                ((TextView) findViewById(R.id.offer)).setText("Rs. " + offer_val);
                            }
//                        price=price-(int) offer_val;
//                        tot=tot-offer_val;
//                        if (price<0)
//                            price=0;
//                        if (tot<0)
//                            tot=0;

                            float amt = ((float) tot);
                            if (datamap.containsKey("cancel_charge"))
                                amt = amt - Float.valueOf(datamap.get("cancel_charge").toString());
                            if (datamap.containsKey("parking_price"))
                                amt = amt - Float.valueOf(datamap.get("parking_price").toString());
//                        if (datamap.containsKey("offer"))
                            amt = amt + offer_val;
//                        amt=(((float)tot)-Float.valueOf(datamap.get("cancel_charge").toString())-Float.valueOf(datamap.get("parking_price").toString())+Float.valueOf(datamap.get("offer").toString()));
                            map.put("amount", String.valueOf(price));
//                        if (amt<0)
//                            amt=0;
                            fare.setText("Rs. " + (int) ((float) amt));
                            total.setText("Rs. " + (int) ((float) tot));

                            rides.child(key).setValue(map);
                            ref.child("resp").setValue("Trip Ended");
                        }

                        HashMap<String, Object> last_ride = new HashMap<>();
                        last_ride.put("date", (new Date()).toString());
                        last_ride.put("destination", datamap.get("destination").toString());
                        last_ride.put("driver", log_id.getString("id", null));
                        last_ride.put("lat", datamap.get("en_lat").toString());
                        last_ride.put("lng", datamap.get("en_lng").toString());
                        last_ride.put("rideid", key);
                        last_ride.put("status", "");

                        LastRide.child(datamap.get("customer_id").toString()).setValue(last_ride);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            // ride details and last ride
            ArrayList<Map<String,Object>> request_list=RequestDetails.getRequest_list();
            for (int i=0;i<RequestDetails.getRequest_list().size();i++){
                if (request_list.get(i).get("customer_id").toString().equals(id)){
                    datamap=request_list.get(i);
                    break;
                }
            }
            rideDetails();
        }

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

                if (status1 && status2) {
                    driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (Map<String, Object>) data.getValue();
                                Float earn = null, cash = null, offer = null, cancel_charge = null;
                                int confirm = 0;
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

                                confirm = confirm + 1;
                                price = Integer.valueOf(log_id.getString("saveprice", null));
                                Log.v("Price", "" + price);
                                if (earn != null)
                                    earn = earn + Float.parseFloat(String.valueOf(price));
                                else
                                    earn = Float.parseFloat(String.valueOf(price));
                                if (offer != null)
                                    offer = offer + Integer.valueOf(log_id.getString("offervalue", null));
                                else
                                    offer = Float.valueOf(Integer.valueOf(log_id.getString("offervalue", null)));
                                if (cancel_charge != null)
                                    cancel_charge = cancel_charge + Float.parseFloat(datamap.get("cancel_charge").toString());
                                else
                                    cancel_charge = Float.parseFloat(datamap.get("cancel_charge").toString());

                                if (datamap.get("paymode").toString().equals("Cash")) {
                                    if (cash != null)
                                        cash = cash + Float.parseFloat(String.valueOf(price));
                                    else
                                        cash = Float.parseFloat(String.valueOf(price));
                                }

                                String key = data.getKey();
                                try {
                                    driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                                    driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                                    driver_acc.child(key).child("cash").setValue(Float.toString(cash));
                                    driver_acc.child(key).child("offer").setValue(Float.toString(offer));
                                    driver_acc.child(key).child("cancel_charge").setValue(Float.toString(cancel_charge));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    account_info();
                }

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

                if (!phone.equals("")) {
                    String otp_msg = "Thank you for using QuickLift. We hope to see you soon again. Please provide your feedback from https://play.google.com/store/apps/details?id=com.quicklift";
//                    new SendSms(otp_msg, phone).start();
                }
                ArrayList<Map<String,Object>> request_list=RequestDetails.getRequest_list();
                for (int i=0;i<RequestDetails.getRequest_list().size();i++){
                    if (request_list.get(i).get("customer_id").toString().equals(id)){
                        request_list.remove(i);
                        break;
                    }
                }
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

//                    GPSTracker gps = new GPSTracker(FeedbackActivity.this);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(log_id.getString("id",null),new GeoLocation(Double.valueOf(log_id.getString("cur_lat",null)),Double.valueOf(log_id.getString("cur_lng",null))));

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

            if (status1 && status2) {
                driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Map<String, Object> map = (Map<String, Object>) data.getValue();
                            Float earn = null, cash = null, offer = null, cancel_charge = null;
                            int confirm = 0;
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

                            confirm = confirm + 1;
                            price = Integer.valueOf(log_id.getString("saveprice", null));
                            if (earn != null)
                                earn = earn + Float.parseFloat(String.valueOf(price));
                            else
                                earn = Float.parseFloat(String.valueOf(price));
                            if (offer != null)
                                offer = offer + Integer.valueOf(log_id.getString("offervalue", null));
                            else
                                offer = Float.valueOf(log_id.getString("offervalue", null));
                            if (cancel_charge != null)
                                cancel_charge = cancel_charge + Float.parseFloat(datamap.get("cancel_charge").toString());
                            else
                                cancel_charge = Float.parseFloat(datamap.get("cancel_charge").toString());

                            if (datamap.get("paymode").toString().equals("Cash")) {
                                if (cash != null)
                                    cash = cash + Float.parseFloat(String.valueOf(price));
                                else
                                    cash = Float.parseFloat(String.valueOf(price));
                            }

                            String key = data.getKey();
                            try {
                                driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                                driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                                driver_acc.child(key).child("cash").setValue(Float.toString(cash));
                                driver_acc.child(key).child("offer").setValue(Float.toString(offer));
                                driver_acc.child(key).child("cancel_charge").setValue(Float.toString(cancel_charge));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else {
                account_info();
            }

            DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
            resp.removeValue();
            DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
            cus.removeValue();

            Stack<SequenceModel> stack = new SequenceStack().getStack();

            ArrayList<Map<String,Object>> request_list=RequestDetails.getRequest_list();
            for (int i=0;i<RequestDetails.getRequest_list().size();i++){
                if (request_list.get(i).get("customer_id").toString().equals(id)){
                    request_list.remove(i);
                    break;
                }
            }
            if (!phone.equals("")) {
                String otp_msg = "Thank you for using QuickLift. We hope to see you soon again. Please provide your feedback from https://play.google.com/store/apps/details?id=com.quicklift";
//                new SendSms(otp_msg, phone).start();
            }
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

//                GPSTracker gps = new GPSTracker(FeedbackActivity.this);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(log_id.getString("id",null),new GeoLocation(Double.valueOf(log_id.getString("cur_lat",null)),Double.valueOf(log_id.getString("cur_lng",null))));

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

    private String getDirectionsUrltwoplaces(String st_lt,String st_ln) {
//        GPSTracker gpsTracker=new GPSTracker(this);

//        Log.v("DISTANCE",""+gpsTracker.getLatitude()+" , "+gpsTracker.getLongitude());
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+st_lt+","+st_ln);
        googleDirectionsUrl.append("&destination="+log_id.getString("cur_lat",null)+","+log_id.getString("cur_lng",null));
        googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
        return googleDirectionsUrl.toString();
    }

    private String getDirectionsUrlthreeplaces(String st_lt,String st_ln,String en_lt,String en_ln) {
//        GPSTracker gpsTracker=new GPSTracker(this);

//        Log.v("DISTANCE",""+gpsTracker.getLatitude()+" , "+gpsTracker.getLongitude());
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+st_lt+","+st_ln);
        googleDirectionsUrl.append("&destination="+log_id.getString("cur_lat",null)+","+log_id.getString("cur_lng",null));
        googleDirectionsUrl.append("&waypoints=via:");
        googleDirectionsUrl.append(en_lt + "," + en_ln);
        googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
        return googleDirectionsUrl.toString();
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

    public void rideDetails(){
        float final_price = 0;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+id);
        SharedPreferences.Editor editor = ride_info.edit();
        editor.putString("state", "start");
        editor.commit();
        DatabaseReference rides = FirebaseDatabase.getInstance().getReference("Rides");
        DatabaseReference LastRide = FirebaseDatabase.getInstance().getReference("LastRide");
        HashMap<String, Object> map = new HashMap<>();

        if (!datamap.get("cancel_charge").toString().equals("0")) {
            cancel.setText("Rs. " + datamap.get("cancel_charge").toString());
            final_price += Float.valueOf(datamap.get("cancel_charge").toString());
            Log.v("PRICE", "" + final_price);
            findViewById(R.id.cancel_text).setVisibility(View.VISIBLE);
        } else {
            cancel.setVisibility(View.GONE);
            findViewById(R.id.cancel_text).setVisibility(View.GONE);
        }
//                    if (datamap.containsKey("offer") && !datamap.get("offer").toString().equals("0")) {
//                        ((TextView)findViewById(R.id.offer)).setText("Rs. " + datamap.get("offer").toString());
//                        final_price-=Float.valueOf(datamap.get("offer").toString());
//                        Log.v("PRICE",""+final_price);
//                        findViewById(R.id.offer_text).setVisibility(View.VISIBLE);
//                    }else {
//                        findViewById(R.id.offer).setVisibility(View.GONE);
//                        findViewById(R.id.offer_text).setVisibility(View.GONE);
//                    }
        if (datamap.containsKey("parking_price") && !datamap.get("parking_price").toString().equals("0")) {
            ((TextView) findViewById(R.id.parking)).setText("Rs. " + datamap.get("parking_price").toString());
            final_price += Float.valueOf(datamap.get("parking_price").toString());
            Log.v("PRICE", "" + final_price);
            findViewById(R.id.parking_text).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.parking).setVisibility(View.GONE);
            findViewById(R.id.parking_text).setVisibility(View.GONE);
        }

        float tot = Float.valueOf(datamap.get("price").toString()) + Float.valueOf(datamap.get("cancel_charge").toString());

        price = Integer.parseInt(datamap.get("price").toString());

        map.put("customerid", datamap.get("customer_id").toString());
        map.put("destination", datamap.get("destination").toString());
        map.put("driver", log_id.getString("id", null));
        map.put("source", datamap.get("source").toString());
        if (datamap.containsKey("offer"))
            map.put("discount", datamap.get("offer").toString());
        map.put("cancel_charge", datamap.get("cancel_charge").toString());
        map.put("paymode", datamap.get("paymode").toString());
        if (datamap.containsKey("parking_price"))
            map.put("parking", datamap.get("parking_price").toString());
        else
            map.put("parking", "0");
        map.put("seat", datamap.get("seat").toString());
        map.put("time", new Date().toString());
        if (datamap.containsKey("started"))
            map.put("start_time", datamap.get("started").toString());
        else
            map.put("start_time", "");
        if (datamap.containsKey("pickup_distance"))
            map.put("pickup_distance", datamap.get("pickup_distance").toString());
        else
            map.put("pickup_distance", "0");
        map.put("status", "Completed");
        if (datamap.containsKey("triptime")){
            map.put("triptime",datamap.get("triptime").toString());
        }
        if (datamap.containsKey("tripdistance")){
            map.put("trip_distance",datamap.get("tripdistance").toString());
        }
        if (datamap.containsKey("tax")){
            map.put("tax",datamap.get("tax").toString());
        }

//                    float total = 0;
//                    map.put("waiting",datamap.get("seat").toString());
//                    map.put("timing",new Date().toString());
        String key = rides.push().getKey();
        Log.v("TAG", "else condition");
        map.put("waiting", "0");
        map.put("timing", "0");

        float offer_val = 0;
        if (datamap.containsKey("cancel_charge"))
            price = (int) (((float) tot) - Float.valueOf(datamap.get("cancel_charge").toString()));
        else
            price = (int) ((float) tot);
        SharedPreferences.Editor editor1 = log_id.edit();
        editor1.putString("saveprice", String.valueOf(price));
        if (datamap.containsKey("offer")) {
            editor1.putString("offervalue", datamap.get("offer").toString());
            offer_val = Integer.parseInt(datamap.get("offer").toString());
        } else if (datamap.containsKey("offer_disc")) {
            if (!datamap.get("offer_disc").toString().equals("0")) {
//                                editor1.putString("offervalue", "0");
//                                offer_val=((float)tot)*Integer.parseInt(datamap.get("offer_disc").toString())/100;
//                                if (offer_val>Float.parseFloat(datamap.get("offer_upto").toString())){
//                                    offer_val=Float.parseFloat(datamap.get("offer_upto").toString());
//                                }
                offer_val = Float.parseFloat(datamap.get("offer_value").toString());
                editor1.putString("offervalue", String.valueOf((int) offer_val));
            } else {
                editor1.putString("offervalue", "0");
            }
        } else {
            editor1.putString("offervalue", "0");
        }
        editor1.commit();

//                        if (price<=(int) offer_val)
//                            offer_val=price;

        map.put("discount", String.valueOf((int) offer_val));
        if (offer_val == 0) {
            ((TextView) findViewById(R.id.offer)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.offer_text)).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.offer)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.offer_text)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.offer)).setText("Rs. " + offer_val);
        }
//                        price=price-(int) offer_val;
//                        tot=tot-offer_val;
//                        if (price<0)
//                            price=0;
//                        if (tot<0)
//                            tot=0;

        float amt = ((float) tot);
        if (datamap.containsKey("cancel_charge"))
            amt = amt - Float.valueOf(datamap.get("cancel_charge").toString());
        if (datamap.containsKey("parking_price"))
            amt = amt - Float.valueOf(datamap.get("parking_price").toString());
//                        if (datamap.containsKey("offer"))
        amt = amt + offer_val;
//                        amt=(((float)tot)-Float.valueOf(datamap.get("cancel_charge").toString())-Float.valueOf(datamap.get("parking_price").toString())+Float.valueOf(datamap.get("offer").toString()));
        map.put("amount", String.valueOf(price));
//                        if (amt<0)
//                            amt=0;
        fare.setText("Rs. " + (int) ((float) amt));
        total.setText("Rs. " + (int) ((float) tot));

        if (key==null)
            key="key";
        rides.child(key).setValue(map);
        ref.child("resp").setValue("Trip Ended");

        HashMap<String, Object> last_ride = new HashMap<>();
        last_ride.put("date", (new Date()).toString());
        last_ride.put("destination", datamap.get("destination").toString());
        last_ride.put("driver", log_id.getString("id", null));
        last_ride.put("lat", datamap.get("en_lat").toString());
        last_ride.put("lng", datamap.get("en_lng").toString());
        last_ride.put("rideid", key);
        last_ride.put("status", "");

        LastRide.child(datamap.get("customer_id").toString()).setValue(last_ride);
    }

    public void account_info(){
        SharedPreferences account_info = getSharedPreferences("account",MODE_PRIVATE);
        SharedPreferences.Editor acc_edit=account_info.edit();
        GregorianCalendar gregorianCalendar=new GregorianCalendar();
        String date = String.format("%02d",gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
        String month = String.format("%02d",(gregorianCalendar.get(GregorianCalendar.MONTH)+1));
        String year = String.format("%02d",gregorianCalendar.get(GregorianCalendar.YEAR));
//                    StringBuilder builder=new StringBuilder().append(String.format("%02d", (date))).append("-")
//                            .append(String.format("%02d", (month+1))).append("-").append(year);
        final String formateDate = date+"-"+month+"-"+year;

        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);

        Float earn = null, cash = null, offer = null, cancel_charge = null;
        int confirm = 0;

            confirm = Integer.parseInt(account_info.getString("book",null));
            earn = Float.parseFloat(account_info.getString("earn",null));
            cash = Float.parseFloat(account_info.getString("cash",null));
            offer = Float.parseFloat(account_info.getString("offer",null));
            cancel_charge = Float.parseFloat(account_info.getString("cancel_charge",null));

        confirm = confirm + 1;
        price = Integer.valueOf(log_id.getString("saveprice", null));
        if (earn != null)
            earn = earn + Float.parseFloat(String.valueOf(price));
        else
            earn = Float.parseFloat(String.valueOf(price));
        if (offer != null)
            offer = offer + Integer.valueOf(log_id.getString("offervalue", null));
        else
            offer = Float.valueOf(log_id.getString("offervalue", null));
        if (cancel_charge != null)
            cancel_charge = cancel_charge + Float.parseFloat(datamap.get("cancel_charge").toString());
        else
            cancel_charge = Float.parseFloat(datamap.get("cancel_charge").toString());

        if (datamap.get("paymode").toString().equals("Cash")) {
            if (cash != null)
                cash = cash + Float.parseFloat(String.valueOf(price));
            else
                cash = Float.parseFloat(String.valueOf(price));
        }

        String key = account_info.getString("key",null);
        try {
            driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
            driver_acc.child(key).child("earn").setValue(Float.toString(earn));
            driver_acc.child(key).child("cash").setValue(Float.toString(cash));
            driver_acc.child(key).child("offer").setValue(Float.toString(offer));
            driver_acc.child(key).child("cancel_charge").setValue(Float.toString(cancel_charge));

            acc_edit.remove("book");
            acc_edit.remove("earn");
            acc_edit.remove("cash");
            acc_edit.remove("offer");
            acc_edit.remove("cancel_charge");
            acc_edit.putString("book",String.valueOf(confirm));
            acc_edit.putString("earn",String.valueOf(earn));
            acc_edit.putString("cash",String.valueOf(cash));
            acc_edit.putString("offer",String.valueOf(offer));
            acc_edit.putString("cancel_charge",String.valueOf(cancel_charge));
            acc_edit.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
