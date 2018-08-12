package com.quickliftpilot.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.quickliftpilot.Util.SequenceStack;
import com.quickliftpilot.Util.UserRequestInfo;
import com.quickliftpilot.model.SequenceModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Stack;

public class OngoingRideService extends Service {
    DatabaseReference customerReq;
    UserRequestInfo userRequestInfo;
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    SharedPreferences log_id,ride_info;
    SharedPreferences.Editor editor;
    Intent notificationServ;
    private Stack<SequenceModel> stack;

    public OngoingRideService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.v("TAG","Ongoing ride service !");

        stack = new SequenceStack().getStack();
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        customerReq.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    if (stack.size()==0)
                        Log.v("TAG","Stack size is not already initialised !");
                    else
                        Log.v("TAG","Stack size is already initialised !");

                    Log.i("OK","Login ID in OnGoing Rides : "+log_id.getString("id",null));
                    Log.i("TAG","data present");
                    stack.removeAllElements();
                    final int val=(int) dataSnapshot.getChildrenCount();
                    for (final DataSnapshot data : dataSnapshot.getChildren()){
                        final Map<String,Object> map = (Map<String,Object>)data.getValue();
//                        Log.i("TAG","Request for driver : "+map.toString());

//                        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
//                        editor = ride_info.edit();
//                        editor.putString("accept",map.get("accept").toString());
//                        editor.putString("customer_id",map.get("customer_id").toString());
//                        editor.putString("d_lat",map.get("d_lat").toString());
//                        editor.putString("d_lng",map.get("d_lng").toString());
//                        editor.putString("destination",map.get("destination").toString());
//                        editor.putString("en_lat",map.get("en_lat").toString());
//                        editor.putString("en_lng",map.get("en_lng").toString());
//                        editor.putString("otp",map.get("otp").toString());
//                        editor.putString("price",map.get("price").toString());
//                        editor.putString("seat",map.get("seat").toString());
//                        editor.putString("source",map.get("source").toString());
//                        editor.putString("st_lat",map.get("st_lat").toString());
//                        editor.putString("st_lng",map.get("st_lng").toString());

                        if (map.containsKey("accept") && !map.get("accept").toString().equals("0")) {
                            DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users/" + map.get("customer_id").toString());
                            user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> userMap = (Map<String, Object>) dataSnapshot.getValue();
//                                editor.putString("name",userMap.get("name").toString());
//                                editor.putString("phone",userMap.get("phone").toString());
//                                editor.putString("email",userMap.get("email").toString());
//                                editor.commit();

                                    SequenceModel model = new SequenceModel();
                                    model.setId(map.get("customer_id").toString());
                                    model.setName(userMap.get("name").toString());
                                    model.setType("pick");
                                    model.setOtp(map.get("otp").toString());
                                    model.setAddress(map.get("source").toString());
                                    model.setPhone(userMap.get("phone").toString());
                                    model.setLat(Double.parseDouble(map.get("st_lat").toString()));
                                    model.setLng(Double.parseDouble(map.get("st_lng").toString()));
                                    model.setLatLng(new LatLng(Double.parseDouble(map.get("st_lat").toString()), Double.parseDouble(map.get("st_lng").toString())));

                                    SequenceModel dropModel = new SequenceModel();
                                    dropModel.setId(map.get("customer_id").toString());
                                    dropModel.setName(userMap.get("name").toString());
                                    dropModel.setType("drop");
                                    dropModel.setOtp(map.get("otp").toString());
                                    dropModel.setPhone(userMap.get("phone").toString());
                                    dropModel.setAddress(map.get("destination").toString());
                                    dropModel.setLat(Double.parseDouble(map.get("en_lat").toString()));
                                    dropModel.setLng(Double.parseDouble(map.get("en_lng").toString()));
                                    dropModel.setLatLng(new LatLng(Double.parseDouble(map.get("en_lat").toString()), Double.parseDouble(map.get("en_lng").toString())));

                                    stack.push(dropModel);
//                        Log.i("TAG","Stack Size : "+stack.size());
                                    stack.push(model);
                                    Log.i("TAG", "Stack Size : " + stack.size() + " , " + dataSnapshot.getChildrenCount());

                                    if (val == (stack.size() / 2))
                                        startService(new Intent(OngoingRideService.this, RouteArrangeService.class));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
//                        Log.i("TAG","Stack Size : "+stack.size());
                    }
                }else {
                    stopSelf();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_NOT_STICKY;
    }
}
