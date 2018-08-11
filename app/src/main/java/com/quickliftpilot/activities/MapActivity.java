package com.quickliftpilot.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.quickliftpilot.R;
import com.quickliftpilot.Util.GPSTracker;
import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.model.SequenceModel;
import com.quickliftpilot.services.FloatingViewService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener,
        View.OnClickListener {

    private GoogleMap mMap;
    private static Location location;
    public static final int RequestPermissionCode = 1;
    private GoogleApiClient googleApiClient;
    private double latitude,longitude;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    DatabaseReference customer=FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference db,status_db,resp=null;
    SharedPreferences log_id,loc_pref,ride_info;
    Marker marker_pick,marker_drop;
    Address address;
    LatLng curr_loc,dest_loc,pick_loc;
    private static RelativeLayout pickup,locate,start_trip,drop,end_trip,cancel;
    private static TextView pick_name,pick_address,locate_name,drop_location,type,name;
    private static Button locate_btn,start_trip_btn,end_trip_btn,cancel_btn;
    private static ImageButton pick_nav,drop_nav;
    private Stack<SequenceModel> stack;
    private RelativeLayout dest_type;
    SequenceModel model;
    int seat = 0;
    Handler handler;
    private static Runnable r;
    ValueEventListener response_listener=null;
    public static Activity fa;
    Marker marker;
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MapActivity.fa!=null)
            MapActivity.fa.finish();
        fa=this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        startService(new Intent(MapActivity.this,RequestService.class));

//        Log.i("TAG","i am here");
        loc_pref = getSharedPreferences("location",Context.MODE_PRIVATE);
        Intent floatingViewIntent = new Intent(this,FloatingViewService.class);

        pickup = (RelativeLayout)findViewById(R.id.customer_pickup);
        pickup.setVisibility(View.VISIBLE);
        locate = (RelativeLayout)findViewById(R.id.customer_locate);
        locate.setVisibility(View.GONE);
        start_trip = (RelativeLayout)findViewById(R.id.start_trip);
        start_trip.setVisibility(View.GONE);
        drop = (RelativeLayout)findViewById(R.id.customer_drop);
        drop.setVisibility(View.GONE);
        end_trip = (RelativeLayout)findViewById(R.id.end_trip);
        end_trip.setVisibility(View.GONE);
        cancel = (RelativeLayout)findViewById(R.id.cancel);
        cancel.setVisibility(View.VISIBLE);

        pick_name = (TextView)findViewById(R.id.c_pick_name);
        pick_address = (TextView)findViewById(R.id.c_pick_address);
        drop_location = (TextView)findViewById(R.id.c_drop_address);
        type = (TextView)findViewById(R.id.type);
        name = (TextView)findViewById(R.id.name);

        locate_btn = (Button)findViewById(R.id.locate_button);
        locate_btn.setOnClickListener(this);
        start_trip_btn = (Button)findViewById(R.id.start_trip_btn);
        start_trip_btn.setOnClickListener(this);
        end_trip_btn = (Button)findViewById(R.id.end_trip_btn);
        end_trip_btn.setOnClickListener(this);
        cancel_btn = (Button)findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(this);

        pick_nav = (ImageButton)findViewById(R.id.pick_navigation);
        pick_nav.setOnClickListener(this);
        drop_nav = (ImageButton)findViewById(R.id.drop_navigation);
        drop_nav.setOnClickListener(this);
        dest_type = (RelativeLayout) findViewById(R.id.dest_type);
        dest_type.setVisibility(View.VISIBLE);
        dest_type.setOnClickListener(this);

//        Log.v("TAG","map activity");

        SupportMapFragment mapFragment = mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
//        Log.i("OK","d_id : "+log_id.getString("id",null));
        db= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        stopService(floatingViewIntent);

        if (getIntent().hasExtra("cancelled")){
            View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
            TextView title=(TextView)view.findViewById(R.id.title);
            TextView message=(TextView)view.findViewById(R.id.message);
            Button left=(Button) view.findViewById(R.id.left_btn);
            Button right=(Button) view.findViewById(R.id.right_btn);

            left.setVisibility(View.GONE);
            right.setText("Ok");
            title.setText("Trip Cancelled !");
            if (getIntent().getStringExtra("cancelled")!=null)
                message.setText("The trip is cancelled by \n"+getIntent().getStringExtra("cancelled"));
            else
                message.setText("The trip is cancelled by Customer");

            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
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

//        db.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//////                Log.i("TAG","Child Added : "+dataSnapshot.getValue());
////                if (!dataSnapshot.child("seat").getValue().toString().equalsIgnoreCase("full")){
////                    seat = seat+Integer.parseInt(dataSnapshot.child("seat").getValue().toString());
//////                    Log.i("TAG","Child Seat : "+seat);
////                    DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null)+"/seat");
////                    seat_data.setValue(Integer.toString(seat));
////                }else {
////                    DatabaseReference seat_data = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null)+"/seat");
////                    seat_data.setValue("full");
////                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
////                Log.i("TAG","Child Removed : "+dataSnapshot.getKey());
//                String c_id = dataSnapshot.getKey();
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        tracktripstatus();
    }

    private void getCurrentLocation() {
//        Log.i("TAG","Getting Current Location");
//        Log.v("Tag","Uploc");
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//////        Log.i("TAG","Current Location : "+location);
//        if (location != null) {
//            longitude = location.getLongitude();
//            latitude = location.getLatitude();
////
////            SupportMapFragment mapFragment = mapFragment = (SupportMapFragment) getSupportFragmentManager()
////                    .findFragmentById(R.id.map);
////            mapFragment.getMapAsync(this);
//        }
//        else {
////            Log.i("TAG","Current Location is null");
//
//            LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//            boolean gps_enabled = false;
//            boolean network_enabled = false;
//            try {
//                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            } catch(Exception ex) {}
//
//            try {
//                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//            } catch(Exception ex) {}
//
//            if(!gps_enabled && !network_enabled) {
//                displayLocationSettingsRequest(getApplicationContext());
//            }
//        }
    }

    private void moveMap() {

//        mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(latitude,longitude))
//                .draggable(true)
//                .title("Current Location"));
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//
//Log.v("Tag","Updating Location");
//        if (log_id.getString("ride",null).equals("")) {
//
//            String userId = log_id.getString("id",null);
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
//
//            GeoFire geoFire = new GeoFire(ref);
//            geoFire.setLocation(userId, new GeoLocation(latitude,longitude));
//        }
//        else {
//            String userId= log_id.getString("id",null);
//
//            DatabaseReference statref=FirebaseDatabase.getInstance().getReference("Status/");
//
//            GeoFire statGeoFire=new GeoFire(statref);
//            statGeoFire.setLocation(userId,new GeoLocation(latitude,longitude));
//        }
    }
    LocationRequest lct;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        getCurrentLocation();
        lct = LocationRequest.create();
        lct.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lct.setInterval(15000);

        Log.v("Tag","Service up");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, lct, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Toast.makeText(this, "Map Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ride_info = getSharedPreferences("ride_info", MODE_PRIVATE);

//        googleApiClient.connect();
        seat = 0;
        stack = new SequenceStack().getStack();

        model = new SequenceModel();
        responselisteners();
    }

    private void responselisteners(){
//        if (resp!=null)
//            resp.removeEventListener(response_listener);

        if (stack.size()>0) {
            Log.i("TAG","Stack Size in map : "+stack.size());
            //model = null;\\
//            model=new SequenceModel();
            model = stack.pop();
            stack.push(model);

//                Log.i("TAG","Stack Size in map : "+stack.size());
            try {
                resp = FirebaseDatabase.getInstance().getReference("Response/" + model.getId());
                resp.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                            Log.i("TAG","Response Id where it crash : "+resp.getKey());
                        if (dataSnapshot.exists()) {
                            String resp_value = dataSnapshot.child("resp").getValue().toString();
//                            Log.i("TAG","resp_value : "+resp_value);
//                            Log.i("TAG","model type : "+model.getType());
                            if (resp_value.equalsIgnoreCase("Trip Started") && model.getType().equalsIgnoreCase("drop")) {
                                pickup.setVisibility(View.GONE);
                                drop.setVisibility(View.VISIBLE);
                                start_trip();
                            } else if (resp_value.equalsIgnoreCase("Trip Started") && model.getType().equalsIgnoreCase("pick") || resp_value.equalsIgnoreCase("Cancel") || resp_value.equalsIgnoreCase("Trip Ended")) {
//                                        stack.push(model);
                                if (!stack.isEmpty()) {
                                    Log.v("TAG","response pop started trip"+dataSnapshot.getKey());
                                    stack.pop();
                                }
////                                        startActivity(new Intent(MapActivity.this, MapActivity.class));
//                                resp.removeEventListener(response_listener);
//                                startActivity(new Intent(MapActivity.this,MapActivity.class));
//                                MapActivity.fa.finish();
                                responselisteners();
                            } else if (resp_value.equalsIgnoreCase("Located")) {
                                locate();
                                pickup.setVisibility(View.VISIBLE);
                                drop.setVisibility(View.GONE);
                            } else if (resp_value.equalsIgnoreCase("Accept")) {
                                nav_pick();
                                pickup.setVisibility(View.VISIBLE);
                                drop.setVisibility(View.GONE);
                            } else if (resp_value.equalsIgnoreCase("Trip Ended")) {
                                end_trip();
                                Log.v("TAG", "end1");
                                drop.setVisibility(View.VISIBLE);
                                pickup.setVisibility(View.GONE);
                            } else if (resp_value.equalsIgnoreCase("Cancelled")) {
                                ArrayList<SequenceModel> sequenceModels = new ArrayList<>();
                                if (stack.size() > 0) {
                                    while (stack.size() > 0) {
//                        Log.i("TAG","Stak pop : "+stack.size());
                                        SequenceModel deleteModel = stack.pop();
                                        if (deleteModel.getId() != model.getId()) {
                                            sequenceModels.add(deleteModel);
//                        Log.i("TAG","item for pushing : "+sequenceModels.size());
                                        }
                                    }

                                    if (sequenceModels.size() > 0) {
                                        for (int i = sequenceModels.size() - 1; i >= 0; i--) {
//                            Log.i("TAG","Stak push : "+i);
                                            stack.push(sequenceModels.get(i));
                                        }
                                        sequenceModels.clear();
                                    }
                                    else {
                                        Log.v("TAG","Placing DriverAvailable !!!!");
                                        SharedPreferences.Editor editor=log_id.edit();
                                        editor.putString("ride","");
                                        editor.commit();
                                        GPSTracker gps = new GPSTracker(MapActivity.this);
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
                                        GeoFire geoFire = new GeoFire(ref);
                                        geoFire.setLocation(log_id.getString("id",null),new GeoLocation(gps.getLatitude(),gps.getLongitude()));
                                    }
                                }
//                                resp.removeEventListener(response_listener);
                                responselisteners();
                            }
                        } else {
//                                startActivity(new Intent(MapActivity.this,MapActivity.class));

                            Log.v("TAG","response pop");
                            if (!stack.isEmpty())
                            stack.pop();
                            Log.v("TAG", "end");
//                                startActivity(new Intent(MapActivity.this, Welcome.class));
//                            resp.removeEventListener(response_listener);
                            responselisteners();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (NullPointerException ne) {

            }

//                db.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (!dataSnapshot.hasChild(model.getId())){
//                            Log.v("TAG","end2");
////
//                            if (stack.size()==0) {
//                                startActivity(new Intent(MapActivity.this,Welcome.class));
//                                finish();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });

            if (model != null) {
//                Log.i("Model","Model");
                Log.v("TAG","Model is not null.");
                if (model.getType().equalsIgnoreCase("pick")) {
//                    Log.i("Model",""+model.getName()+" "+model.getId()+" "+model.getType());

                    type.setText("Pick ");
                    name.setText(model.getName());
                    pick_name.setText(model.getName());
                    pick_address.setText(model.getAddress());
                    dest_type.setVisibility(View.VISIBLE);
                    pickup.setVisibility(View.VISIBLE);
                } else if (model.getType().equalsIgnoreCase("drop")) {
//                    Log.i("Model",""+model.getName()+" "+model.getId()+" "+model.getType());

                    type.setText("Drop");
                    name.setText(model.getName());
                    pick_name.setText(model.getName());
                    drop_location.setText(model.getAddress());
                    dest_type.setVisibility(View.VISIBLE);
                    drop.setVisibility(View.VISIBLE);
                }
            }
            else {
                Log.v("TAG","Model is null.");
            }

//            String state = ride_info.getString("state",null);
////            Log.i("OK","state on line : "+state);
//            if (state.equalsIgnoreCase("pick_nav")){
//                nav_pick();
//            }else if (state.equalsIgnoreCase("locate")){
//                locate();
//            }else if (state.equalsIgnoreCase("start_trip")){
//                start_trip();
//            }else if (state.equalsIgnoreCase("drop_nav")){
//                drop_nav();
//            }else if (state.equalsIgnoreCase("end_trip")){
//                end_trip();
//            }else if (state.equalsIgnoreCase("start")){
//                dest_type.setVisibility(View.VISIBLE);
//            }else if (state.equalsIgnoreCase("dest_type")){
//                dest_type();
//            }
        }
        else {
            if (Welcome.WelcomeActivity!=null)
                Welcome.WelcomeActivity.finish();

            SharedPreferences.Editor editor=log_id.edit();
            editor.putString("ride", "");
            editor.commit();
            Intent intent=new Intent(MapActivity.this,Welcome.class);
            intent.putExtra("status","true");
            startActivity(intent);
            MapActivity.fa.finish();
        }
    }

    private void dest_type(){
        if (model.getType().equalsIgnoreCase("pick")){
            dest_type.setVisibility(View.GONE);
            locate.setVisibility(View.VISIBLE);
        }else if (model.getType().equalsIgnoreCase("drop")){
            dest_type.setVisibility(View.GONE);
            end_trip.setVisibility(View.VISIBLE);
        }
    }

    private void nav_pick(){
//        dest_type.setVisibility(View.VISIBLE);
        pickup.setVisibility(View.VISIBLE);
        drop.setVisibility(View.GONE);
//        locate.setVisibility(View.VISIBLE);
    }

    private void locate(){
        pickup.setVisibility(View.VISIBLE);
        locate.setVisibility(View.GONE);
        start_trip.setVisibility(View.VISIBLE);

    }

    private void start_trip(){
        start_trip.setVisibility(View.GONE);
        pickup.setVisibility(View.GONE);
        pick_address.setVisibility(View.GONE);
        drop.setVisibility(View.VISIBLE);
    }

    private void drop_nav(){
        pickup.setVisibility(View.GONE);
        pick_address.setVisibility(View.GONE);
        end_trip.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        cancel_btn.setVisibility(View.VISIBLE);
        drop.setVisibility(View.VISIBLE);
//        end_trip.setVisibility(View.VISIBLE);
    }

    private void end_trip(){
        drop.setVisibility(View.GONE);
        end_trip.setVisibility(View.GONE);
        pickup.setVisibility(View.GONE);
        dest_type.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        cancel_btn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        handler = new Handler();
        r = new Runnable() {
            public void run() {
                getCurrentLocation();
                Location l = location;
                onLocationChanged(l);
//                status_db.child("0").setValue(latitude);
//                status_db.child("1").setValue(longitude);
//                handler.postDelayed(this, 10000);
            }
        };

//        handler.postDelayed(r, 10000);

        super.onResume();
//        SequenceModel model = new SequenceModel();
//        model.setLatLng(new LatLng(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null))));
//        model.setName(ride_info.getString("name",null));
//        model.setType("pickup");

//        getAddress(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
//        pick_name.setText(ride_info.getString("name",null));
////        pick_address.setText(new String(address.getFeatureName() + "\n" + address.getLocality() +"\n" + address.getAdminArea() + "\n" + address.getCountryName()));
//        pick_address.setText(ride_info.getString("source",null));
//        getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
////        drop_location.setText(new String(address.getFeatureName() + "\n" + address.getLocality() +"\n" + address.getAdminArea() + "\n" + address.getCountryName()));
//        drop_location.setText(ride_info.getString("destination",null));
//        getCurrentLocation();
//        curr_loc = new LatLng(latitude,longitude);
//        pick_loc = new LatLng(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
//        dest_loc = new LatLng(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
//        //getRouteToMarker(curr_loc,pick_loc);
//        getRouteToMarker(curr_loc,model.getLatLng());
//        if (latitude == Double.parseDouble(ride_info.getString("st_lat",null)) && longitude == Double.parseDouble(ride_info.getString("st_lng",null))){
//            pickup.setVisibility(View.GONE);
//            locate.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
//        handler.removeCallbacks(r);
        if (handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        if (mMap != null){
//            mMap.clear();
//        }
        mMap = googleMap;
//        Log.i("TAG","onMapReady() method start ");
//        LatLng userLocation = new LatLng(latitude,longitude);
        if (checkPermission()){
            mMap.setMyLocationEnabled(true);
        }else {
            requestPermission();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
//        Marker marker = mMap.addMarker(new MarkerOptions()
//                .title("Current Location")
//                .position(userLocation)
//                .draggable(true)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        marker.showInfoWindow();
//        Log.i("TAG","onMapReady() method completed");


//        moveMap();
//        LatLng latLng = new LatLng(Double.parseDouble(ride_info.getString("st_lat",null)), Double.parseDouble(ride_info.getString("st_lng",null)));
//        addMarkers(latLng);
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.v("Tag","Service up2");

        if (location != null) {
            this.location = location;
//            getCurrentLocation();
//            moveMap();
//            Log.v("Tag", "Map" + location.getLatitude() + " " + location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            mMap.getUiSettings().setZoomControlsEnabled(true);
//            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLatitude()), 15);
//            mMap.moveCamera(update);
            if (marker != null) {
                marker.remove();
            }
            MarkerOptions options = new MarkerOptions()
                    .title("Current")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(new LatLng(location.getLatitude(),location.getLongitude()))
                    .snippet("Pick up");
            marker=mMap.addMarker(options);

//            String userId= log_id.getString("id",null);
////
//            DatabaseReference statref=FirebaseDatabase.getInstance().getReference("Status");
//
//            GeoFire statGeoFire=new GeoFire(statref);
//            statGeoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));

        }else {
//            getCurrentLocation();
////            this.location = location;
//            moveMap();
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MapActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,


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
                    boolean BluetoothPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean BluetoothAdminPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    //           boolean SystemAlertPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && BluetoothPermission && BluetoothAdminPermission) {

                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
//                        Toast.makeText(MapActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void displayLocationSettingsRequest(Context context) {
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
//                .addApi(LocationServices.API).build();
//        googleApiClient.connect();
//
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(10000 / 2);
//        Log.v("Tag","Updating Location");
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//        builder.setAlwaysShow(true);
//
//        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        Log.i("TAG", "All location settings are satisfied.");
////                        startActivity(new Intent(MapActivity.this,MapActivity.class));
////                        finish();
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
//
//                        try {
//                            // Show the dialog by calling startResolutionForResult(), and check the result
//                            // in onActivityResult().
//                            status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.i("TAG", "PendingIntent unable to execute request.");
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
//                        break;
//                }
//            }
//        });
    }

    private List<Polyline> polylines=new ArrayList<>();

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
//            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        //for (int i = 0; i <route.size(); i++) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(7);
        polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);

        //time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
        //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteToMarker(LatLng pickupLatLng, LatLng destnLatLng) {
        if (pickupLatLng != null && destnLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void getRouteToMarkerViaThirdPoint(LatLng pickupLatLng, LatLng destnLatLng, LatLng waypoint) {
        if (pickupLatLng != null && destnLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, waypoint,pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    private void tracktripstatus() {
//        getCurrentLocation();
//
//        final String userId= log_id.getString("id",null);
//        final DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status");
//        tripstatus.child(log_id.getString("id",null)).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()){
////                    findViewById(R.id.canceltrip).setVisibility(View.GONE);
//                    SharedPreferences.Editor editor=log_id.edit();
//                    editor.putString("ride","");
//                    editor.commit();
////                    marker_drop.remove();
////                    marker_pick.remove();
//                    erasePolylines();
////                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+userId);
//
////                    GeoFire geoFire=new GeoFire(ref);
////                    geoFire.removeLocation(userId);
//
//                    String userId = log_id.getString("id",null);
//                    DatabaseReference dref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+log_id.getString("type",null));
//
//                    GeoFire gFire = new GeoFire(dref);
//                    gFire.setLocation(userId, new GeoLocation(latitude, longitude));
////                    findViewById(R.id.info).setVisibility(View.GONE);
//                    Toast.makeText(MapActivity.this, "Ride cancelled by customer", Toast.LENGTH_SHORT).show();
////                    startActivity(new Intent(MapActivity.this,Welcome.class));
//                    finish();
//                }else{
////                    GeoFire gFire = new GeoFire(tripstatus);
////                    gFire.setLocation(userId, new GeoLocation(latitude, longitude));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

//    public void cancel_trip(){
//        GregorianCalendar gregorianCalendar=new GregorianCalendar();
//        String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
//        String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
//        String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
//        final String formateDate = year+"-"+month+"-"+date;
//
//        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
//        driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()){
//                    Map<String,Object> map = (Map<String,Object>)data.getValue();
//                    int cancel = Integer.parseInt(map.get("cancel").toString());
//                    cancel = cancel+1;
//                    String key = data.getKey();
//                    try {
//                        driver_acc.child(key).child("cancel").setValue(Integer.toString(cancel));
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
////        getCurrentLocation();
////        moveMap();
//        stack.push(model);
//        DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+ride_info.getString("customer_id",null));
//        resp.child("resp").setValue("Cancel");
//        DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+ride_info.getString("customer_id",null));
//        cus.removeValue();
//        SharedPreferences.Editor editor=log_id.edit();
//        editor.putString("ride","");
//        editor.commit();
//        int size = stack.size();
//        Log.i("TAG","Stak SIze : "+size);
//        ArrayList<SequenceModel> sequenceModels = new ArrayList<>();
//        for(int i = 0;i < size;i++){
//            Log.i("TAG","Stak pop : "+i);
//            SequenceModel deleteModel =  stack.pop();
//            if (deleteModel.getId() != ride_info.getString("customer_id",null)){
//                sequenceModels.add(deleteModel);
//                Log.i("TAG","item for pushing : "+sequenceModels.size());
//            }
//        }
//        if (sequenceModels.size() > 0){
//            for (int i = sequenceModels.size(); i > 0;i--){
//                Log.i("TAG","Stak push : "+i);
//                stack.push(sequenceModels.get(i));
//            }
//            startActivity(new Intent(MapActivity.this,MapActivity.class));
//            finish();
//        }else {
//            DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
//            tripstatus.removeValue();
//            DatabaseReference working = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
//            working.removeValue();
//            finish();
//        }
//    }

    public void getAddress(Double latitude,Double longitude){
        try{
            Geocoder geo = new Geocoder(MapActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
//                Toast.makeText(this, "Waiting for Location", Toast.LENGTH_SHORT).show();
            }
            else {
                if (addresses.size() > 0) {
                    for (int i = 0;i<addresses.size();i++){
//                        Log.d("TAG",i+"th Address Result"+addresses.get(i).getFeatureName() + "," + addresses.get(i).getLocality() +", " + addresses.get(i).getAdminArea() + ", " + addresses.get(i).getCountryName());
                    }
                    address = addresses.get(0);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+model.getId()+"/resp");
        int id = v.getId();
        if (id == locate_btn.getId()){
            db.child(model.getId()+"/located").setValue(sdf.format(new Date()));
            ref.setValue("Located");
            SharedPreferences.Editor editor = ride_info.edit();
            editor.putString("state","locate");
            editor.commit();
            locate.setVisibility(View.GONE);
            pickup.setVisibility(View.VISIBLE);
            start_trip.setVisibility(View.VISIBLE);
        }else if (id == dest_type.getId()){
            SharedPreferences.Editor editor = ride_info.edit();
            editor.putString("state","dest_type");
            editor.commit();
            if (model.getType().equalsIgnoreCase("pick")){
                dest_type.setVisibility(View.GONE);
                locate.setVisibility(View.VISIBLE);
            }else if (model.getType().equalsIgnoreCase("drop")){
//                SharedPreferences.Editor editor = ride_info.edit();
//                editor.putString("state","drop_nav");
//                editor.commit();
                dest_type.setVisibility(View.GONE);
                end_trip.setVisibility(View.VISIBLE);
            }
        }else if(id == start_trip_btn.getId()){
//            db.child(model.getId()+"/started").setValue(sdf.format(new Date()));
//            SharedPreferences.Editor editor = ride_info.edit();
//            editor.putString("state","start_trip");
//            editor.commit();
            Intent i=new Intent(this,OTPActivity.class);
            i.putExtra("otp",model.getOtp());
            i.putExtra("id",model.getId());
            startActivityForResult(i,2);
        }else if(id == pick_nav.getId()){
//            SharedPreferences.Editor editor = ride_info.edit();
//            editor.putString("state","pick_nav");
//            editor.commit();

            getAddress(Double.parseDouble(model.getLat().toString()),Double.parseDouble(model.getLng().toString()));
            Uri nav_uri = Uri.parse("google.navigation:q="+model.getLat().toString()+","+model.getLng().toString());
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,nav_uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent,1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent in = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(in, 4);
            } else {
                startService(new Intent(this, FloatingViewService.class));
            }
        }else if (id == drop_nav.getId()){
//            SharedPreferences.Editor editor = ride_info.edit();
//            editor.putString("state","drop_nav");
//            editor.commit();
            getAddress(Double.parseDouble(model.getLat().toString()),Double.parseDouble(model.getLng().toString()));
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q="+model.getLat().toString()+","+model.getLng().toString()));
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent,3);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent in = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(in, 4);
            } else {
                startService(new Intent(this, FloatingViewService.class));
            }
        }else if (id == end_trip_btn.getId()){
            View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
            TextView title=(TextView)view.findViewById(R.id.title);
            TextView message=(TextView)view.findViewById(R.id.message);
            Button left=(Button) view.findViewById(R.id.left_btn);
            Button right=(Button) view.findViewById(R.id.right_btn);

            left.setVisibility(View.VISIBLE);
            left.setText("No");
            right.setText("Yes");
            title.setText("End Trip !");
            message.setText("Are you sure you want to end trip ?");
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder .setView(view)
                    .setCancelable(false);

            final AlertDialog alert = builder.create();
            alert.show();

            left.setOnClickListener(null);
            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });
            right.setOnClickListener(null);
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.child(model.getId()+"/ended").setValue(sdf.format(new Date()));
//            resp.removeEventListener(response_listener);
                    dest_type.setVisibility(View.GONE);
                    cancel.setVisibility(View.VISIBLE);
                    pickup.setVisibility(View.GONE);
                    drop.setVisibility(View.GONE);

                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+model.getId());
                    databaseReference.child("accept").setValue(3);
                    Log.v("TAG","onclick pop");
                    Log.v("TAG","ended trip");
                    Intent intent=new Intent(MapActivity.this,FeedbackActivity.class);
                    intent.putExtra("customer_id",model.getId());
                    intent.putExtra("phone",model.getPhone());
                    intent.putExtra("name",model.getName());
//            intent.putExtra("price",datamap.get("price").toString());
                    stack.pop();
                    startActivity(intent);
                    MapActivity.fa.finish();
                    alert.dismiss();
                }
            });
        }else if (id == cancel_btn.getId()){
//            SharedPreferences.Editor editor = ride_info.edit();
//            editor.putString("state","start");
//            editor.commit();
//            cancel_trip();
//            startActivity(new Intent(this,Welcome.class));
            startActivity(new Intent(MapActivity.this,RiderListActivity.class));
//            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                pickup.setVisibility(View.GONE);
                locate.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Pick up arrived", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                pickup.setVisibility(View.VISIBLE);
                locate.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Pickup is not yet Arrived", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                SharedPreferences.Editor editor = ride_info.edit();
                editor.putString("state","start");
                editor.commit();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+model.getId()+"/resp");
                ref.setValue("Trip Started");
                cancel.setVisibility(View.VISIBLE);
                start_trip.setVisibility(View.GONE);

                cancel.setVisibility(View.VISIBLE);
//                stack.pop();
//                startActivity(new Intent(MapActivity.this,MapActivity.class));
//                finish();
//                startActivity(getIntent());
//                resp.removeEventListener(response_listener);
                stack.pop();
                responselisteners();
//                Toast.makeText(this, "OTP entered", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
//                Toast.makeText(this, "OTP is not yet entered", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 3){
            if (resultCode == Activity.RESULT_OK){

                Log.v("TAG","map pop");
                stack.pop();
                startActivity(new Intent(this,FeedbackActivity.class));
                MapActivity.fa.finish();
//                Toast.makeText(this, "Feedback completed", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                drop.setVisibility(View.VISIBLE);
                end_trip.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Feedback not yet entered", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 4 && resultCode == RESULT_OK) {
            startService(new Intent(this, FloatingViewService.class));
        } else {
//            Toast.makeText(this, "Draw over other app permission not enable.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarkers(LatLng latLng){
        erasePolylines();
        mMap.clear();
        getCurrentLocation();
        LatLng currLoc = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions()
                .title("Current Location")
                .position(currLoc)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false))));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Destination")
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //cancel_trip();
    }

}