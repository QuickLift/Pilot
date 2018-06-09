package com.quickliftpilot.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pandey on 2/3/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final  String DATABASE_NAME = "quick_lift.db";

    public static final String LOGIN_TABLE = "login_detail";
    public static final String RIDES_TABLE = "rides";
    public static final String PROFILE_TABLE = "profile";

    public static final String LOGIN_COL1 = "login_date";
    public static final String LOGIN_COL2 = "login_status";
    public static final String LOGIN_COL3 = "login_duration";

    public static final String RIDES_COL1 = "total_rides";
    public static final String RIDES_COL2 = "earning";
    public static final String RIDES_COL3 = "cancel_rides";

    public static final String PROFILE_COL1 = "name";
    public static final String PROFILE_COL2 = "email";
    public static final String PROFILE_COL3 = "phone";
    public static final String PROFILE_COL4 = "password";
    public static final String PROFILE_COL5 = "vehicle";
    public static final String PROFILE_COL6 = "vehicle_no";
    public static final String PROFILE_COL7 = "img";
    public static final String PROFILE_COL8 = "address";
    public static final String PROFILE_COL9 = "rating";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+LOGIN_TABLE+" (login_date TEXT,login_status TEXT,login_duration TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+RIDES_TABLE+" (total_rides TEXT,earning TEXT,cancel_rides TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+PROFILE_TABLE+" (name TEXT,email TEXT,phone TEXT,password TEXT,vehicle TEXT,vehicle_no TEXT,img TEXT,address TEXT,rating TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+LOGIN_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+RIDES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+PROFILE_TABLE);
    }

    public boolean insertLoginData(String login_date,String login_status,String login_duration){
        boolean status = false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (login_duration.isEmpty())
            login_duration = "NULL";

        contentValues.put(LOGIN_COL1,login_date);
        contentValues.put(LOGIN_COL2,login_status);
        contentValues.put(LOGIN_COL3,login_duration);

        long result = db.insert(LOGIN_TABLE,null,contentValues);

        if (result == -1){
            status = false;
        }else {
            status = true;
        }
        return status;
    }

//    public boolean insertRidesData(String total_rides,String earn,String cancel_rides){
//        boolean status = false;
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//
//        contentValues.put(RIDES_COL1,total_rides);
//        contentValues.put(RIDES_COL2,earn);
//        contentValues.put(RIDES_COL3,cancel_rides);
//
//        long result = db.insert(RIDES_TABLE,null,contentValues);
//
//        if (result == -1){
//            status = false;
//        }else {
//            status = true;
//        }
//
//        return status;
//    }
//
//    public boolean insertLogOutData(String login_date,String login_status,String login_duration){
//        boolean status = false;
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(LOGIN_COL2,login_status);
//        contentValues.put(LOGIN_COL3,login_duration);
//
//        long result = db.update(LOGIN_TABLE,contentValues,"login_date = ?",new String[]{login_date});
//        if (result > 0){
//            status = true;
//        }else {
//            status = false;
//        }
//
//        return status;
//    }
//
//    public Cursor getLoginData(String login_date){
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor res = db.rawQuery("select * from "+LOGIN_TABLE+" WHERE login_date = "+login_date,null);
//
//        return res;
//    }
//
//    public Cursor getRidesDetail(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor res = db.rawQuery("select * from "+RIDES_TABLE,null);
//
//        if (res.getCount() <= 0){
//            boolean status = insertRidesData("0","0","0");
//            if (status){
//                Log.i("TAG","TABLE INITIALIZED");
//                res = db.rawQuery("select * from "+RIDES_TABLE,null);
//            }else {
//                Log.e("TAG","not updated");
//            }
//        }
//
//        return res;
//    }
//
//    public void updateConfirmRide(){
//
//    }
//
//    public boolean updateCancelRide(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        Cursor res = getRidesDetail();
//        String c = null;
//        while(res.moveToNext()) {
//            c = res.getString(2);
//        }
//        int cancel = Integer.parseInt(c);
//
//        cancel = cancel + 1;
//        contentValues.put(RIDES_COL3,Integer.toString(cancel));
//
//        long result = db.update(RIDES_TABLE,contentValues,"cancel_rides = ?",new String[]{c});
//        Log.i("TAG","REsult : "+result);
//        if (result > 0){
//            return true;
//        }else {
//            return false;
//        }
//    }
//
//    public boolean insertProfileInfo(String name,String email,String phone,String password,String vehicle,String vehicle_no,String img,String address,String rating){
//        boolean status = false;
//        if (TextUtils.isEmpty(name))
//            name = "null";
//        else if (TextUtils.isEmpty(email))
//            email = "null";
//        else if (TextUtils.isEmpty(phone))
//            phone = "0000000000";
//        else if (TextUtils.isEmpty(password))
//            password = "null";
//        else if (TextUtils.isEmpty(vehicle))
//            vehicle = "null";
//        else if (TextUtils.isEmpty(vehicle_no))
//            vehicle_no = "null";
//        else if (TextUtils.isEmpty(img))
//            img = "null";
//        else if (TextUtils.isEmpty(address))
//            address = "null";
//        else if (TextUtils.isEmpty(rating))
//            rating = "0";
//        else {
//            SQLiteDatabase db = this.getWritableDatabase();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(PROFILE_COL1,name);
//            contentValues.put(PROFILE_COL2,email);
//            contentValues.put(PROFILE_COL3,phone);
//            contentValues.put(PROFILE_COL4,password);
//            contentValues.put(PROFILE_COL5,vehicle);
//            contentValues.put(PROFILE_COL6,vehicle_no);
//            contentValues.put(PROFILE_COL7,img);
//            contentValues.put(PROFILE_COL8,address);
//            contentValues.put(PROFILE_COL9,rating);
//
//            long result = db.insert(PROFILE_TABLE,null,contentValues);
//            if (result > 0){
//                status = true;
//            }else {
//                status = false;
//            }
//        }
//        return status;
//    }
//
//    public Cursor getProfileInfo(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor res = db.rawQuery("select * from "+PROFILE_TABLE,null);
//        if (res != null && res.getCount() > 0){
//            Log.e("TAG","Res value in getProfileInfo() method : "+res.getCount());
//            return res;
//        }else {
//            boolean status = insertProfileInfo("null","null","null","null","null","null","null","null","0");
//            if (status){
//                res = db.rawQuery("select * from "+PROFILE_TABLE,null);
//            }else {
//                Log.e("TAG","status of insertion : "+status);
//            }
//        }
//        return res;
//    }
}
