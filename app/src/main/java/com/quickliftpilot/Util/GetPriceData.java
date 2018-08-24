package com.quickliftpilot.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by adarsh on 27/4/18.
 */

public class GetPriceData extends AsyncTask<Object,String,String> {
    GoogleMap mmap;
    String url,googleDirectionsData,duration,distance;
    //    Data data;
    Marker marker_drop;
    Cursor cursor,spec_location;
    Context context;
    int spec_package=-1,vehicle_case=1;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private TextView fare_text;
    private float final_price=0;
    private HashMap<String, Object> map;
    private DatabaseReference rides,ref,ongoing_rides;
    private String key,veh_type;
    private TextView total_text,offer_text,offer_value;
    private String cancel_charge;
    int park,offer;
    private int price;
    int discount;

    @Override
    protected String doInBackground(Object... objects) {
        url=(String)objects[0];
        fare_text=(TextView)objects[1];
        final_price=(float)objects[2];
        map=(HashMap<String,Object>)objects[3];
        key=(String)objects[4];
        rides=(DatabaseReference)objects[5];
        veh_type=(String)objects[6];
        cursor=(Cursor)objects[7];
        context=(Context) objects[8];
        total_text=(TextView)objects[9];
        cancel_charge=(String)objects[10];
        price=(int)objects[11];
        ref=(DatabaseReference)objects[12];
        ongoing_rides=(DatabaseReference)objects[13];
        pref=(SharedPreferences)objects[14];
        park=(int)objects[15];
        offer=(int)objects[16];
        discount=(int)objects[17];
        offer_value=(TextView)objects[18];
        offer_text=(TextView)objects[19];
//        data=(Data) objects[17];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl=new DownloadUrl();

        try {
            googleDirectionsData=downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String,String> directionsList=null;
        PriceParser parser=new PriceParser();
        directionsList=parser.parseDirections(s);
        duration=directionsList.get("duration");
        distance=directionsList.get("distance");

//        pref = context.getSharedPreferences("Login",MODE_PRIVATE);
        editor=pref.edit();

        //editText.setText(distance + " : "+duration);
//        Toast.makeText(this, ""+cursor.getCount()+" , "+cursor.getColumnCount(), Toast.LENGTH_SHORT).show();

        SimpleDateFormat dt=new SimpleDateFormat("HH:mm");
        int index=1;
        try {
            if ((dt.parse("11:00").after(dt.parse(dt.format(new Date()))) && dt.parse("08:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("23:59").after(dt.parse(dt.format(new Date()))) && dt.parse("17:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("05:00").after(dt.parse(dt.format(new Date()))) && dt.parse("00:00").before(dt.parse(dt.format(new Date()))))){
                index=2;
            }
            else {
                index=1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.v("DISTANCE",String.valueOf(Float.valueOf(distance)/1000));
        if (veh_type.equals("full"))
            pricecar(-1,spec_location,index, cursor,Float.valueOf(distance)/1000);
        else if (veh_type.equals("share"))
            pricesharecar(-1,spec_location,index, cursor,Float.valueOf(distance)/1000);
        else if (veh_type.equals("excel"))
            priceexcel(-1,spec_location,index, cursor,Float.valueOf(distance)/1000);
//        data.setTriptime(String.valueOf(Integer.valueOf(duration)/60));
//        pricebike(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);

//        priceauto(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);

//        pricerickshaw(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
//        priceshareauto(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);

//        pricesharerickshaw(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);

//        editText.setText(duration);
//        editText1.setText(duration);
//        editText2.setText(duration);
//        editText3.setText(duration);
//        editText4.setText(duration);
//        editText5.setText(duration);
//        editText6.setText(duration);

        map.put("trip_distance",String.valueOf(Float.valueOf(distance)/1000));
        if (pref.contains("tax")) {
            float tax=final_price * Float.valueOf(pref.getString("tax", null))/100;
            map.put("tax",String.valueOf(tax));
            final_price += tax;
        }
        else {
            map.put("tax","0");
        }

        price=(int)(final_price-Float.valueOf(cancel_charge));
//        Log.v("PRICE",""+final_price);
        if (discount!=0) {
            float off = final_price * discount / 100;
            if (off < offer) {
                offer = (int) off;
            }
        }

        final_price=final_price-offer;
        price=price-offer;
        if (price<0)
            price=0;
        if (final_price<0)
            final_price=0;
        if (offer!=0){
            offer_text.setVisibility(View.VISIBLE);
            offer_value.setVisibility(View.VISIBLE);
            offer_value.setText("Rs. "+offer);
        }
        else {
            offer_text.setVisibility(View.GONE);
            offer_value.setVisibility(View.GONE);
        }
        map.put("discount", String.valueOf(offer));
        if (((int)(final_price-Float.valueOf(cancel_charge)))<0)
            map.put("amount","0");
        else
            map.put("amount",String.valueOf((int)(final_price-Float.valueOf(cancel_charge))));

        editor.putString("saveprice",String.valueOf(price));
        Log.v("TAG",""+offer);
        if (offer==0) {
            editor.putString("offervalue", "0");
            Log.v("TAG","1"+offer);
        }
        else {
            editor.putString("offervalue", String.valueOf(offer));
            Log.v("TAG","2"+offer);
        }
        editor.commit();
        int amt=((int)(float)(final_price-Float.valueOf(cancel_charge)))-park+offer;

        if (amt<0)
            amt=0;
        fare_text.setText("Rs. "+amt);
        if (((int)final_price)<0)
            final_price=0;
        total_text.setText("Rs. "+(int)final_price);

        ongoing_rides.child("price").setValue(String.valueOf((int)(final_price-Float.valueOf(cancel_charge))));
        ref.child("resp").setValue("Trip Ended");
        rides.child(key).setValue(map);
    }

    private void priceexcel(int pckg, Cursor sloc, int index, Cursor cursor, float distanceValue) {
//        Toast.makeText(this, ""+distanceValue+" "+time, Toast.LENGTH_SHORT).show();

//        Log.v("Address",String.valueOf(cursor.getCount()));
        cursor.moveToFirst();
        for (int x=0;x<8;x++){
            cursor.moveToNext();
        }
//            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=final_price;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
                Log.v("TAG",""+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
                Log.v("TAG",""+fare);
            } else {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
                Log.v("TAG",""+fare);
            }
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));
        }
        else {
            if (distanceValue <= Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
                Log.v("TAG",""+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
                Log.v("TAG",""+fare);
//                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
                Log.v("TAG",""+fare);
            }
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));

//            fare=fare+(Float.parseFloat(pref.getString("excel",null))*parking);
//            parkingpriceexcel=String.valueOf((int)(Float.parseFloat(pref.getString("excel",null))*parking));

//            editor.putString("parkexcel",parkingpriceexcel);
//            editor.commit();
        }
            Log.v("TAG",""+fare);

        if (vehicle_case==1) {
            int val=(int)fare;
            val=(int)(((float)val)* Float.parseFloat(pref.getString("ratemultiplier",null)));
            final_price=val;
            fare_text.setText("Rs. " + val);
//            price_excel.setText("Rs. " + val);
        }
        else if (vehicle_case==2) {
            int add=(int)(fare/100);
            int val=(int)fare;
            val=(int)(((float)val)* Float.parseFloat(pref.getString("ratemultiplier",null)))+((int)((float)add* Float.parseFloat(pref.getString("ratemultiplier",null)))*Integer.parseInt(pref.getString("outsidetripextraamount",null)));
//            price_excel.setText("Rs. " + val);

            Log.v("TAG",""+distanceValue+" "+fare+" "+val);
        }
    }
//    private void pricebike(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_bike.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
//        //time_bike.setText(String.valueOf(time/60)+" min");
//    }
//    private void priceauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
//        //time_auto.setText(String.valueOf(time/60)+" min");
//    }
    private void pricecar(int pckg,Cursor sloc, int index, Cursor cursor, float distanceValue) {
        cursor.moveToFirst();
//            for (int x=0;x<1;x++){
//                cursor.moveToNext();
//            }

//            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=final_price;
        Log.v("TAG","fare : "+fare);
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
                Log.v("TAG","p step 1: "+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG","1 "+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
                Log.v("TAG","p step 2: "+fare);
            } else {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
                Log.v("TAG","p step 3: "+fare);
            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));
        }
        else {
            if (distanceValue <= Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
                Log.v("TAG","step 1: "+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
                Log.v("TAG","step 2: "+fare);
//                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
                Log.v("TAG","step 3: "+fare);
            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));
//            fare=fare+(Float.parseFloat(pref.getString("fullcar",null))*parking);
//            parkingpricefull=String.valueOf((int)(Float.parseFloat(pref.getString("fullcar",null))*parking));
//
//            editor.putString("parkfull",parkingpricefull);
//            editor.commit();
        }

        if (vehicle_case==1){
            int val=(int)fare;
            val=(int)(((float)val)* Float.parseFloat(pref.getString("ratemultiplier",null)));
            final_price=val;
            fare_text.setText("Rs. " + val);
            Log.v("TAG","step 4: "+val);
//            price_car.setText("Rs. " + val);
        }
        else if (vehicle_case==2) {
            int add=(int)(fare/100);
            int val=(int)fare;
            val=(int)(((float)val)* Float.parseFloat(pref.getString("ratemultiplier",null)))+((int)((float)add* Float.parseFloat(pref.getString("ratemultiplier",null)))*Integer.parseInt(pref.getString("outsidetripextraamount",null)));
//            price_car.setText("Rs. " + val);

            Log.v("TAG","step 4: "+distanceValue+" "+fare+" "+val);
        }
        //time_car.setText(String.valueOf(time/60)+" min");
    }
    private void pricerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, float distanceValue,int time, int parking) {
        cursor.moveToFirst();
        for (int x=0;x<2;x++){
            cursor.moveToNext();
        }

//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
            fare=fare+Float.parseFloat(pref.getString("fullrickshaw",null));
        }
        int val=(int)fare;
        val=val* Integer.parseInt(pref.getString("ratemultiplier",null));
//        price_rickshaw.setText("Rs. " + val);
        //time_rickshaw.setText(String.valueOf(time/60)+" min");
    }
//    private void priceshareauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
//        //time_shareAuto.setText(String.valueOf(time/60)+" min");
//    }
    private void pricesharecar(int pckg,Cursor sloc, int index, Cursor cursor, float distanceValue) {
        cursor.moveToFirst();
        for (int x=0;x<4;x++){
            cursor.moveToNext();
        }
//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=final_price;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare += Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));
        }
        else {
            if (distanceValue <= Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
//                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first"))) - Float.parseFloat(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Float.parseFloat(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
//                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//            data.setTimecharge(cursor.getString(cursor.getColumnIndex("time")));
//            fare=fare+(Float.parseFloat(pref.getString("sharecar",null))*parking);
//            parkingpriceshare=String.valueOf((int)(Float.parseFloat(pref.getString("sharecar",null))*parking));
//            editor.putString("parkshare",parkingpriceshare);
//            editor.commit();
        }
        int val=(int)fare;
        val=(int)(((float)val)* Float.parseFloat(pref.getString("ratemultiplier",null)));
        final_price=val;
        fare_text.setText("Rs. " + val);
//        price_shareCar.setText("Rs. " + val);
        //time_shareCar.setText(String.valueOf(time/60)+" min");
    }
    private void pricesharerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, float distanceValue,int time, int parking) {
        cursor.moveToFirst();
        for (int x=0;x<6;x++){
            cursor.moveToNext();
        }
//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
            fare=fare+Float.parseFloat(pref.getString("sharerickshaw",null));
        }
        int val=(int)fare;
        val=val* Integer.parseInt(pref.getString("ratemultiplier",null));
//        price_shareRickshaw.setText("Rs. " + val);
        //time_shareRickshaw.setText(String.valueOf(time/60)+" min");
    }
}
