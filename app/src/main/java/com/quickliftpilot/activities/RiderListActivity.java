package com.quickliftpilot.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.quickliftpilot.R;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.RiderList;
import com.quickliftpilot.model.SequenceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class RiderListActivity extends AppCompatActivity {
    private ListView rider;
    private ArrayList<RiderList> riders;
    private DatabaseReference request,response,users;
    private SharedPreferences log_id;
    private Stack<SequenceModel> stack;
    private SharedPreferences ride_info;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                startActivity(new Intent(RiderListActivity.this,MapActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_list);

        getSupportActionBar().setTitle("Rider List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (MapActivity.fa!=null)
            MapActivity.fa.finish();

        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        request = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        response = FirebaseDatabase.getInstance().getReference("Response");
        users = FirebaseDatabase.getInstance().getReference("Users");
        stack = new SequenceStack().getStack();
        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);

        riders = new ArrayList<>();
        rider = (ListView)findViewById(R.id.rider_list);

        request.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                riders.clear();
                if (dataSnapshot.getChildrenCount() > 0){
                    Log.i("TAG","request listener");
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        final String c_id = data.getKey();
                        response.child(c_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("TAG","response listener");
                                String resp = dataSnapshot.child("resp").getValue().toString();
                                if (!resp.equalsIgnoreCase("Trip Ended") || !resp.equalsIgnoreCase("Cancel")){
                                    Log.i("TAG","response not ended nor canceled");
                                    final RiderList ride = new RiderList();
                                    ride.setC_id(c_id);
                                    if (resp.equalsIgnoreCase("Trip Started")){
                                        Log.i("TAG","response started");
                                        ride.setEnable(false);
                                    }else{
                                        Log.i("TAG","response not started");
                                        ride.setEnable(true);
                                    }
                                    users.child(c_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue().toString();
//                                            Log.i("TAG","user name : "+name);
                                            ride.setC_name(name);
                                            ride.setC_phone(dataSnapshot.child("phone").getValue().toString());
                                            riders.add(ride);
                                            rider.setAdapter(new CustomAdapter());
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
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void cancel_trip(String customer, final String reason){
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
                    int cancel = Integer.parseInt(map.get("cancel").toString());
                    cancel = cancel+1;
                    String key = data.getKey();
                    try {
                        driver_acc.child(key).child("cancel").setValue(Integer.toString(cancel));
                        SharedPreferences.Editor editor = ride_info.edit();
                        editor.putString("state","start");
                        editor.commit();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+customer);
        resp.child("resp").setValue("Cancel");
        final DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+customer);

        cus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DatabaseReference rides = FirebaseDatabase.getInstance().getReference("Rides");

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("amount", dataSnapshot.child("price").getValue().toString());
                    map.put("customerid", dataSnapshot.child("customer_id").getValue().toString());
                    map.put("destination", dataSnapshot.child("destination").getValue().toString());
                    map.put("driver", log_id.getString("id", null));
                    map.put("source", dataSnapshot.child("source").getValue().toString());
                    map.put("discount", dataSnapshot.child("offer").getValue().toString());
                    map.put("cancel_charge", dataSnapshot.child("cancel_charge").getValue().toString());
                    map.put("paymode", dataSnapshot.child("paymode").getValue().toString());
                    map.put("reason", reason);
                    if (dataSnapshot.hasChild("parking_price"))
                        map.put("parking",dataSnapshot.child("parking_price").getValue().toString());
                    else
                        map.put("parking","0");
                    map.put("seat",dataSnapshot.child("seat").getValue().toString());
                    map.put("time", new Date().toString());
                    map.put("status", "Cancelled");
                    map.put("cancelledby", "Driver");

                    String key = rides.push().getKey();
                    rides.child(key).setValue(map);

                    cus.removeValue();
                    startActivity(new Intent(RiderListActivity.this, MapActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        SharedPreferences.Editor editor=log_id.edit();
//        editor.putString("ride","");
//        editor.commit();
//        int size = stack.size();
//        Log.i("TAG","Stak SIze : "+size);
//        ArrayList<SequenceModel> sequenceModels = new ArrayList<>();
//        for(int i = 0;i < size;i++){
//            Log.i("TAG","Stak pop : "+i);
//            SequenceModel deleteModel =  stack.pop();
//            if (!deleteModel.getId().equalsIgnoreCase(customer)){
//                sequenceModels.add(deleteModel);
//                Log.i("TAG","item for pushing : "+sequenceModels.size());
//            }
//        }
//        if (sequenceModels.size() > 0){
//            for (int i = sequenceModels.size()-1; i >= 0;i--){
//                Log.i("TAG","Stak push : "+i);
//                stack.push(sequenceModels.get(i));
//            }
//            startActivity(new Intent(RiderListActivity.this,MapActivity.class));
//            finish();
//        }else {
//            DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
//            tripstatus.removeValue();
//            DatabaseReference working = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
//            working.removeValue();
//            finish();
//        }
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return riders.size();
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
        public View getView(final int position, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.rider_card,null);
            TextView name = (TextView)view.findViewById(R.id.c_name);
            ImageView cancel = (ImageView)view.findViewById(R.id.cancel);
            ImageView call = (ImageView)view.findViewById(R.id.call);

            name.setText(riders.get(position).getC_name());
            Log.i("TAG","name in adapter : "+name.getText().toString());
            if (riders.get(position).isEnable()){
//                cancel.setVisibility(View.VISIBLE);
                cancel.setEnabled(true);
            }else {
                cancel.setEnabled(false);
                ((LinearLayout)view.findViewById(R.id.layout_cancel)).setBackgroundResource(R.drawable.round_button_red_disabled);
//                cancel.setVisibility(View.GONE);
            }
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(RiderListActivity.this,CancelReason.class);
                    intent.putExtra("id",riders.get(position).getC_id());
                    startActivityForResult(intent,1);
//                    Log.i("OK","Id while cancel drive : "+riders.get(position).getC_id());
                }
            });
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+riders.get(position).getC_phone()));

                    if (ActivityCompat.checkSelfPermission(RiderListActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(callIntent);
                }
            });

            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==RESULT_OK){
            cancel_trip(data.getStringExtra("id"),data.getStringExtra("reason"));
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        startActivity(new Intent(RiderListActivity.this,MapActivity.class));
        finish();
    }
}
