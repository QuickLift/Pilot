package com.quickliftpilot.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by adarsh on 19/2/18.
 */

public class SQLQueries extends SQLiteOpenHelper {
    private static final int version = 5;
    private static final String dbname = "Customer";
    private static final String table1 = "SAVED_PLACES", table2 = "RECENT_PLACES", table3 = "PERSONAL_INFO", table4 = "RIDES", table5 = "LAST_RIDE";
    private static final String col1 = "day", col2 = "stime", col3 = "etime", col4 = "date", col5="time", col6="duration";

    public SQLQueries(Context context) {
        super(context, dbname, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String saved_place_table="CREATE TABLE "+table1+" ( place TEXT, lat TEXT, lng TEXT, name TEXT )";
        String recent_place_table="CREATE TABLE "+table2+" ( place TEXT, lat TEXT, lng TEXT, name TEXT )";
        String last_ride_table="CREATE TABLE "+table5+" ( driver TEXT, date TEXT, destination TEXT )";
        String fare="CREATE TABLE FARE ( amount_base TEXT, dist_base TEXT, amount_first TEXT, dist_first TEXT,amount_second TEXT, time TEXT)";
        String location="CREATE TABLE LOCATION ( latitude TEXT, longitude TEXT, amount TEXT, distance TEXT)";
/*
        String query1 = "CREATE TABLE " +
                table3 + "("
                + col1 + " TEXT," + col2
                + " TEXT,"
                + col3 + " TEXT" + ")";

        String query2 = "CREATE TABLE " +
                table4 + "("
                + col1 + " TEXT," + col2
                + " TEXT,"
                + col3 + " TEXT" + ")";

        String query3 = "CREATE TABLE " +
                table1 + "("
                + col4 + " TEXT," + col5 + " TEXT," + col6
                + " TEXT" + ")";

        String query4 = "CREATE TABLE " +
                table2 + "("
                + col4 + " TEXT," + col5 + " TEXT," + col6
                + " TEXT" + ")";

        db.execSQL(query1);
        db.execSQL(query2);
        db.execSQL(query3);
        db.execSQL(query4);
        */

        db.execSQL(saved_place_table);
        db.execSQL(recent_place_table);
        db.execSQL(last_ride_table);
        db.execSQL(fare);
        db.execSQL(location);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long lastride(String driver,String date,String destination){
        long val=0;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table5;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor!=null && cursor.getCount()!=0 ){
            db=this.getWritableDatabase();
            selectQuery = "DELETE FROM "+ table5;
            db.execSQL(selectQuery);
        }
        db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("amount_base",driver);
        data.put("date",date);
        data.put("destination",destination);
        val = db.insert(table5, null, data);

        return val;
    }

    public long savefare(ArrayList<String> list){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("amount_base",list.get(0));
        data.put("dist_base",list.get(1));
        data.put("amount_first",list.get(2));
        data.put("dist_first",list.get(3));
        data.put("amount_second",list.get(4));
        data.put("time",list.get(5));
        val = db.insert("FARE", null, data);

        return val;
    }

    public long savelocation(ArrayList<String> list){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("latitude",list.get(0));
        data.put("longitude",list.get(1));
        data.put("amount",list.get(2));
        data.put("distance",list.get(3));
        val = db.insert("LOCATION", null, data);

        return val;
    }

    public void lastridedelete(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM "+ table5;
        db.execSQL(selectQuery);
    }

    public void deletefare(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM FARE";
        db.execSQL(selectQuery);
    }

    public void deletelocation(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM LOCATION";
        db.execSQL(selectQuery);
    }

    public Cursor retrievelastride(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table5;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor retrievefare(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM FARE";
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor retrievelocation(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM LOCATION";
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public long saveplace(String place,String lat,String lng,String name){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("place",place);
        data.put("lat",lat);
        data.put("lng",lng);
        data.put("name",name);
        val = db.insert(table1, null, data);

        return val;
    }

    public Cursor retrieve_save_place(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table1;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public long recentplace(String place,String lat,String lng,String name){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("place",place);
        data.put("lat",lat);
        data.put("lng",lng);
        data.put("name",name);
        val = db.insert(table2, null, data);

        return val;
    }

    public Cursor retrieve_recent_place(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table2;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public long create_ring_notification(ArrayList<String> day, ArrayList<String> start, ArrayList<String> end){
        long val=0;

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        for (int i=0;i<day.size();i++) {
            data.put(col1, day.get(i));
            data.put(col2, start.get(i));
            data.put(col3, end.get(i));

            val = db.insert(table3, null, data);
        }
        db.close();
        return val;
    }

    public void ring_notify_del(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM "+ table3;
        db.execSQL(selectQuery);
    }

    public long create_motion_notification(ArrayList<String> day,ArrayList<String> start,ArrayList<String> end){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();
        for (int i=0;i<day.size();i++) {
            data.put(col1, day.get(i));
            data.put(col2, start.get(i));
            data.put(col3, end.get(i));

            val = db.insert(table4, null, data);
        }
        db.close();
        return val;
    }

    public void motion_notify_del(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM "+ table4;
        db.execSQL(selectQuery);
    }

    public Cursor ring_notify_data(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table3;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor motion_notify_data(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table4;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public void motion_block_del(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM "+ table2;
        db.execSQL(selectQuery);
    }

    public void ring_block_del(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM "+ table1;
        db.execSQL(selectQuery);
    }

    public Cursor motion_block_data(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table2;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor ring_block_data(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+ table1;
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public long create_motion_block(String date,String time,String duration){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(col4, date);
        data.put(col5, time);
        data.put(col6, duration);

        val = db.insert(table2, null, data);

        db.close();
        return val;
    }

    public long create_ring_block(String date,String time,String duration){
        long val=0;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(col4, date);
        data.put(col5, time);
        data.put(col6, duration);

        val = db.insert(table1, null, data);

        db.close();
        return val;
    }
}
