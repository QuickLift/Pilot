package com.quickliftpilot.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.quickliftpilot.R;
import com.quickliftpilot.model.Earning;
import com.quickliftpilot.model.Feed;
import com.quickliftpilot.model.RideHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AccountActivity extends AppCompatActivity {
    /***
     * this class for account info, will get histories from cloud of
     * cancel drive, earning,booked..
     * all these displaying & Sumations , bitween given date
     *
     * */
    private Calendar myCalendar;
    private EditText from_date,to_date;
    private ListView listView;
    private LinearLayout linearLayout;
    private TextView mDriveDate,mDriveName, mTotal, mDriveTotal;
    private Spinner spinner;
    DatabaseReference db;
    SharedPreferences preferences;
    List<RideHistory> rideHistoryList;
    ArrayList<Feed> earnings=new ArrayList<>();
    ArrayList<Earning> income=new ArrayList<>();
    ArrayList<String> date=new ArrayList<>();
    Map<String,Object> checking=new HashMap<>();
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    float total=0;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        // stop auto showing keyboard
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // attetch components with id's
        myCalendar = Calendar.getInstance();
        from_date= (EditText) findViewById(R.id.from_date);
        from_date.setEnabled(false);
        //from_date.setFocusable(false);

        to_date = (EditText)findViewById(R.id.to_date);
        //to_date.setFocusable(false);
        to_date.setEnabled(false);

        listView=(ListView)findViewById(R.id.list);
//        mDriveDate=(TextView)findViewById(R.id.drive_date);
//        mDriveName=(TextView)findViewById(R.id.drive_name);
        mTotal =(TextView)findViewById(R.id.total);
//        mDriveTotal =(TextView)findViewById(R.id.drive_total);
        spinner=(Spinner)findViewById(R.id.select);
        linearLayout=(LinearLayout)findViewById(R.id.list_layout);
        linearLayout.setVisibility(View.GONE);
        //read shared preferences of log-in
        preferences=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    findViewById(R.id.from_linear).setVisibility(View.GONE);
                    findViewById(R.id.to_linear).setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                }else {
                    findViewById(R.id.from_linear).setVisibility(View.VISIBLE);
                    findViewById(R.id.to_linear).setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    mTotal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        db= FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+preferences.getString("id",null));
        linearLayout.setVisibility(View.VISIBLE);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    earnings.clear();
                    total=0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        for (DataSnapshot d : data.getChildren()) {
                            Feed feed = new Feed();
                            feed.setDate(data.getKey());
//                                Log.v("TAG",""+data.getKey()+" , "+d.getKey()+" , "+d.getChildrenCount());
                            float cash=0,earn=0,cancel=0,offer=0,tot=0,tax=0,cess=0,cancelch=0;
                            int count=0;

                            count=Integer.parseInt(d.child("cancel").getValue(String.class));
                            if (d.hasChild("cash"))
                                cash=Float.parseFloat(d.child("cash").getValue(String.class));
                            earn=Float.parseFloat(d.child("earn").getValue(String.class));
                            if (d.hasChild("cancel_charge"))
                                cancel=Float.parseFloat(d.child("cancel_charge").getValue(String.class));
                            if (d.hasChild("offer"))
                                offer=Float.parseFloat(d.child("offer").getValue(String.class));

                            tot=earn+offer+cancel;
                            if (tot!=0) {
                                if (preferences.contains("tax")) {
                                    float tax_amt=Float.valueOf(preferences.getString("tax", null));
//                                    float amt=0;
//                                    tax = tot * Float.valueOf(preferences.getString("tax", null)) / 100;
                                    tax=(tot*tax_amt)/(100+tax_amt);
                                }
                                if (preferences.contains("basevalue")) {
                                    if (tot < Float.parseFloat(preferences.getString("basevalue", null)))
                                        cess = Float.parseFloat(preferences.getString("mincommission", null)) * tot / 100;
                                    else {
                                        cess = Float.parseFloat(preferences.getString("mincommission", null)) * Float.parseFloat(preferences.getString("basevalue", null)) / 100;
                                        cess = cess + Float.parseFloat(preferences.getString("maxcommission", null)) * (tot - Float.parseFloat(preferences.getString("basevalue", null))) / 100;
                                    }
                                }
                                if (preferences.contains("cancelcharge")) {
                                    cancelch = count * Integer.parseInt(preferences.getString("cancelcharge", null));
                                }

                                if (d.hasChild("book"))
                                    feed.setBookedRideCount(d.child("book").getValue(String.class));
                                if (d.hasChild("cash"))
                                    feed.setCash(d.child("cash").getValue(String.class));
                                if (d.hasChild("offer"))
                                    feed.setOffer(d.child("offer").getValue(String.class));
                                if (d.hasChild("cancel"))
                                    feed.setCanceledRidesCount(d.child("cancel").getValue(String.class));
                                if (d.hasChild("earn"))
                                    feed.setTotalEarning(d.child("earn").getValue(String.class));
                                if (d.hasChild("reject"))
                                    feed.setRejectedRideCount(d.child("reject").getValue(String.class));
                                if (d.hasChild("cancel_charge"))
                                    feed.setCancel_charge(d.child("cancel_charge").getValue(String.class));

//                            Float outst = Float.valueOf(feed.getTotalEarning()) - Float.valueOf(feed.getCash());
                                float outst = offer + earn - cash - cancel - cancelch - tax - cess;
                                if (total == 0)
                                    total = outst;
                                else
                                    total = total + outst;

//                            Log.v("OK",""+offer+" "+earn+" "+cash+" "+cancel+" "+cancelch+" "+tax+" "+cess);
                                earnings.add(feed);
                            }
                        }
                    }
                    check_date();
                    if (earnings.size()>0) {
                        findViewById(R.id.nodata).setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        mTotal.setText("Total Amount : Rs. " + (int)total);
                        mTotal.setVisibility(View.VISIBLE);
                        listView.setAdapter(new EarningAdapter());
                    }
                    else {
                        findViewById(R.id.nodata).setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        mTotal.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getearnings(){
        total=0;
        linearLayout.setVisibility(View.VISIBLE);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    earnings.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        for (DataSnapshot d : data.getChildren()) {
                            Feed feed = new Feed();
                            feed.setDate(data.getKey());
//                                Log.v("TAG",""+data.getKey()+" , "+d.getKey()+" , "+d.getChildrenCount());
                            float cash=0,earn=0,cancel=0,offer=0,tot=0,tax=0,cess=0,cancelch=0;
                            int count=0;

                            count=Integer.parseInt(d.child("cancel").getValue(String.class));
                            if (d.hasChild("cash"))
                                cash=Float.parseFloat(d.child("cash").getValue(String.class));
                            earn=Float.parseFloat(d.child("earn").getValue(String.class));
                            if (d.hasChild("cancel_charge"))
                                cancel=Float.parseFloat(d.child("cancel_charge").getValue(String.class));
                            if (d.hasChild("offer"))
                                offer=Float.parseFloat(d.child("offer").getValue(String.class));

                            tot=earn+offer;
                            if (tot!=0) {
                                if (preferences.contains("tax")) {
                                    float tax_amt=Float.valueOf(preferences.getString("tax", null));
//                                    float amt=0;
//                                    tax = tot * Float.valueOf(preferences.getString("tax", null)) / 100;
                                    tax=(tot*tax_amt)/(100+tax_amt);
                                }
                                if (preferences.contains("basevalue")) {
                                    if (tot < Float.parseFloat(preferences.getString("basevalue", null)))
                                        cess = Float.parseFloat(preferences.getString("mincommission", null)) * tot / 100;
                                    else {
                                        cess = Float.parseFloat(preferences.getString("mincommission", null)) * Float.parseFloat(preferences.getString("basevalue", null)) / 100;
                                        cess = cess + Float.parseFloat(preferences.getString("maxcommission", null)) * (tot - Float.parseFloat(preferences.getString("basevalue", null))) / 100;
                                    }
                                }
                                if (preferences.contains("cancelcharge")) {
                                    cancelch = count * Integer.parseInt(preferences.getString("cancelcharge", null));
                                }

                                if (d.hasChild("book"))
                                    feed.setBookedRideCount(d.child("book").getValue(String.class));
                                if (d.hasChild("cash"))
                                    feed.setCash(d.child("cash").getValue(String.class));
                                if (d.hasChild("offer"))
                                    feed.setOffer(d.child("offer").getValue(String.class));
                                if (d.hasChild("cancel"))
                                    feed.setCanceledRidesCount(d.child("cancel").getValue(String.class));
                                if (d.hasChild("earn"))
                                    feed.setTotalEarning(d.child("earn").getValue(String.class));
                                if (d.hasChild("reject"))
                                    feed.setRejectedRideCount(d.child("reject").getValue(String.class));
                                if (d.hasChild("cancel_charge"))
                                    feed.setCancel_charge(d.child("cancel_charge").getValue(String.class));

//                            Float outst = Float.valueOf(feed.getTotalEarning()) - Float.valueOf(feed.getCash());
                                float outst = offer + earn - cash - cancel - cancelch - tax - cess;
                                if (total == 0)
                                    total = outst;
                                else
                                    total = total + outst;

//                            Log.v("OK",""+offer+" "+earn+" "+cash+" "+cancel+" "+cancelch+" "+tax+" "+cess);
                                earnings.add(feed);
                            }
                        }
                    }
                    check_date();
                    if (earnings.size()>0) {
                        findViewById(R.id.nodata).setVisibility(View.GONE);
                        mTotal.setText("Total Amount : Rs. " + (int)total);
                        mTotal.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        listView.setAdapter(new EarningAdapter());
                    }
                    else {
                        findViewById(R.id.nodata).setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        mTotal.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void check_date(){
        for (int i=0;i<earnings.size();i++){
            checking.put(earnings.get(i).getDate(),earnings.get(i));
            date.add(earnings.get(i).getDate());
        }
        try {
            date=sortDates(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        earnings.clear();
        for (int i=date.size()-1;i>=0;i--){
            earnings.add((Feed)checking.get(date.get(i)));
        }
    }

    private ArrayList<String> sortDates(ArrayList<String> dates) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        Map<Date, String> dateFormatMap = new TreeMap<>();
        for (String date: dates)
            dateFormatMap.put(f.parse(date), date);
        return new ArrayList<>(dateFormatMap.values());
    }

    @SuppressWarnings("deprecation")
    public void date_from(View view) {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpDialog = new DatePickerDialog(this, fromDate, mYear, mMonth, mDay);
        dpDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dpDialog.show();
    }

    @SuppressWarnings("deprecation")
    public void date_to(View view) {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpDialog = new DatePickerDialog(this, toDate, mYear, mMonth, mDay);
        dpDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dpDialog.show();
    }

    // load button
    @SuppressWarnings("deprecation")
    public void load_btn(View view) {
        if (spinner.getSelectedItemPosition() != 0) {
            from_date.setError(null);
            to_date.setError(null);
            if (validate()) {
                if (from_date.getText().toString().isEmpty() && to_date.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please enter your field", Toast.LENGTH_SHORT).show();
                } else {
                    rideHistoryList = new ArrayList<>();
                    //fiels is not empty
                    db = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/" + preferences.getString("id", null));
//                db.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        String fromDate=from_date.getText().toString();
//                        String toDate=to_date.getText().toString();
//
//                        for (DataSnapshot data : dataSnapshot.getChildren()) {
//                            RideHistory rideHistory = new RideHistory();
//                            if (fromDate.compareToIgnoreCase(data.getKey()) <= 0 && toDate.compareToIgnoreCase(data.getKey()) >= 0) {
//                                //Log.i("AccountActivity", "data.getKey():::  " + data.getKey());
//                                Feed feed = new Feed();
//                                for(DataSnapshot actualFeed : data.getChildren() ) {
//                                    Map<String, String> feedMap  = (Map<String, String>) actualFeed.getValue();//store value in map
//                                    //set all values in feed class object;
//                                    feed.setBookedRideCount(Integer.parseInt(feedMap.get("book")));
//                                    feed.setCanceledRidesCount(Integer.parseInt(feedMap.get("cancel")));
//                                    feed.setTotalEarning(Float.parseFloat(feedMap.get("earn")));
//                                    feed.setRejectedRideCount(Integer.parseInt(feedMap.get("reject")));
//                                }
//                                /**
//                                 * In rideHistory fields: data and feed
//                                 * set key in data(contain date)
//                                 * and components of value pair store in Feed class object
//                                 * */
//                                rideHistory.setDate(data.getKey());
//                                rideHistory.setFeed(feed);
//
//                                rideHistoryList.add(rideHistory);
//                            } else {
//                               // Toast.makeText(AccountActivity.this, "NO data found", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        //if (getActivity()!=null){
//                        linearLayout.setVisibility(View.VISIBLE);
//                        mDriveName.setText(spinner.getSelectedItem().toString());
//                        mDriveDate.setText("Date");
//                        // call custom adapter to display list of drive info
//                        DriveListAdapter listAdapter=null;
//                        if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Booking History")){
//                            //Log.i("yogendra","yogendra: "+spinner.getSelectedItem().toString().trim());
//                            listAdapter = new DriveListAdapter(rideHistoryList,"BookedRideCount");
//                            //call for total count
//                            totalDriveInfo(rideHistoryList,0);
//                        }else if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Cancel History")) {
//                            listAdapter = new DriveListAdapter(rideHistoryList,"CanceledRidesCount");
//                            //call for total count
//                            totalDriveInfo(rideHistoryList,1);
//                        }else if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Earnings History")) {
//                            listAdapter = new DriveListAdapter(rideHistoryList,"TotalEarning");
//                            //call for total count
//                            totalDriveInfo(rideHistoryList,2);
//                        }
//                        listView.setAdapter(listAdapter);
//                        // }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
                    if (spinner.getSelectedItemPosition() != 0) {
                        linearLayout.setVisibility(View.VISIBLE);
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    earnings.clear();
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        Date fromDate = null, toDate = null, db_date = null;
                                        try {
                                            fromDate = sdf.parse(from_date.getText().toString());
                                            toDate = sdf.parse(to_date.getText().toString());
                                            db_date = sdf.parse(data.getKey());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        Log.v("TAG", db_date.toString() + " " + fromDate.toString() + " " + toDate.toString());
                                        if (db_date != null) {
                                            if (fromDate.compareTo(db_date) <= 0 && toDate.compareTo(db_date) >= 0) {
                                                Log.v("TAG", "Entered");
                                                for (DataSnapshot d : data.getChildren()) {
                                                    Feed feed = new Feed();
                                                    feed.setDate(data.getKey());
//                                Log.v("TAG",""+data.getKey()+" , "+d.getKey()+" , "+d.getChildrenCount());
                                                    if (d.hasChild("book"))
                                                        feed.setBookedRideCount(d.child("book").getValue(String.class));
                                                    if (d.hasChild("cash"))
                                                        feed.setCash(d.child("cash").getValue(String.class));
                                                    if (d.hasChild("offer"))
                                                        feed.setOffer(d.child("offer").getValue(String.class));
                                                    if (d.hasChild("cancel"))
                                                        feed.setCanceledRidesCount(d.child("cancel").getValue(String.class));
                                                    if (d.hasChild("earn"))
                                                        feed.setTotalEarning(d.child("earn").getValue(String.class));
                                                    if (d.hasChild("reject"))
                                                        feed.setRejectedRideCount(d.child("reject").getValue(String.class));
                                                    if (d.hasChild("cancel_charge"))
                                                        feed.setCancel_charge(d.child("cancel_charge").getValue(String.class));

//                                                Float outst = Float.valueOf(feed.getTotalEarning()) - Float.valueOf(feed.getCash());
//                                                if (total == null)
//                                                    total = outst;
//                                                else
//                                                    total = total + outst;

                                                    earnings.add(feed);
                                                }
                                            }
                                        }
                                    }
                                    check_date();
                                    if (earnings.size() > 0) {
                                        findViewById(R.id.nodata).setVisibility(View.GONE);
                                        if (spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Booking History")) {
                                            listView.setVisibility(View.VISIBLE);
                                            listView.setAdapter(new BookingAdapter());
                                            mTotal.setVisibility(View.GONE);
                                        } else if (spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Cancel History")) {
                                            listView.setAdapter(new CancelAdapter());
                                            listView.setVisibility(View.VISIBLE);
                                            mTotal.setVisibility(View.GONE);
                                        } else if (spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Account Statement")) {
                                            mTotal.setText("Total Amount : Rs. " + total);
                                            listView.setVisibility(View.VISIBLE);
                                            mTotal.setVisibility(View.VISIBLE);
                                            listView.setAdapter(new EarningAdapter());
                                        }
                                    } else {
                                        findViewById(R.id.nodata).setVisibility(View.VISIBLE);
                                        listView.setVisibility(View.GONE);
                                        mTotal.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        }
        else {
            listView.setVisibility(View.VISIBLE);
            mTotal.setVisibility(View.VISIBLE);
            getearnings();
        }
    }

    private boolean validate(){
        boolean status = false;
        try{
            if (from_date.getText().toString().isEmpty()){
                from_date.setError("From date should not be empty");
            }else if (to_date.getText().toString().isEmpty()){
                to_date.setError("To date should not be empty");
            }else{
                Date from = sdf.parse(from_date.getText().toString());
                Date to = sdf.parse(to_date.getText().toString());
//                Toast.makeText(this, ""+from.compareTo(to), Toast.LENGTH_SHORT).show();
                if (from.after(to)){
                    from_date.setError("From date should not greater than To date");
                }else {
                    status = true;
                }
            }
        }catch(Exception e){
            Log.e("TAG",""+e.getLocalizedMessage());
            status = false;
        }
        return status;
    }

    private void totalDriveInfo(List<RideHistory> rideHistoryList, int caseCount) {
        /**
         * sum of drive info values
         * */
        mTotal.setText("TOTAL ");
        String temp;
        int possition;
        int cancel=0;
        int booked_rides=0;
        double earning=0;
//        switch (caseCount){
//            case 0: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
//                booked_rides=booked_rides+rideHistoryList.get(possition).getFeed().getBookedRideCount();
//            }
//            mDriveTotal.setText(Integer.toString(booked_rides));break;
//            case 1: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
//                cancel=cancel+rideHistoryList.get(possition).getFeed().getCanceledRidesCount();
//            }
//                mDriveTotal.setText(Integer.toString(cancel));break;
//            case 2: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
//                earning=earning+rideHistoryList.get(possition).getFeed().getTotalEarning();
//            }
//                mDriveTotal.setText(Double.toString(earning));break;
//                default:
//        }
    }

    DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
           updateFromLabel( year, monthOfYear,dayOfMonth);
        }
    };

    DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            updateToLabel( year, monthOfYear,dayOfMonth);
        }
    };

    private void updateFromLabel(int year,int month,int day){
//        from_date.setText(new StringBuilder().append(year).append("-")
//                .append(String.format("%02d", (month+1))).append("-").append(String.format("%02d", (day))));
        from_date.setText(new StringBuilder().append(String.format("%02d", (day))).append("-")
                .append(String.format("%02d", (month+1))).append("-").append(year));

    }

    private void updateToLabel(int year,int month,int day){
//        to_date.setText(new StringBuilder().append(year).append("-")
//                .append(String.format("%02d", (month+1))).append("-").append(String.format("%02d", (day))));
        to_date.setText(new StringBuilder().append(String.format("%02d", (day))).append("-")
                .append(String.format("%02d", (month+1))).append("-").append(year));
    }

    class EarningAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return earnings.size();
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
            view=getLayoutInflater().inflate(R.layout.earnings_layout,null);
            TextView date=(TextView)view.findViewById(R.id.date);
            TextView earning=(TextView)view.findViewById(R.id.earning);
            TextView cash=(TextView)view.findViewById(R.id.cash);
            TextView paytm=(TextView)view.findViewById(R.id.paytm);
            TextView offer=(TextView)view.findViewById(R.id.offer);
            TextView deduction=(TextView)view.findViewById(R.id.deduction);
            TextView tax=(TextView)view.findViewById(R.id.tax);
            TextView cancellation=(TextView)view.findViewById(R.id.cancellation);
            TextView company_cess=(TextView)view.findViewById(R.id.company_cess);
            TextView outstanding=(TextView)view.findViewById(R.id.outstanding);

            float cess=0,cancel=0,tottax=0,total=0,ded=0;

            total=Float.parseFloat(earnings.get(position).getTotalEarning())+Float.parseFloat(earnings.get(position).getOffer());

            date.setText(earnings.get(position).getDate());
            earning.setText("Earning : Rs. "+total);
            cash.setText("Cash : "+String.valueOf(Float.valueOf(earnings.get(position).getCash())+Float.valueOf(earnings.get(position).getCancel_charge())));
            paytm.setText("Paytm : "+String.valueOf(Float.parseFloat(earnings.get(position).getTotalEarning())-Float.parseFloat(earnings.get(position).getCash())));
            offer.setText("Offer : "+earnings.get(position).getOffer());

            if (preferences.contains("basevalue")) {
                if (Float.parseFloat(earnings.get(position).getTotalEarning()) < Float.parseFloat(preferences.getString("basevalue", null))) {
                    cess=Float.valueOf(Float.valueOf(preferences.getString("mincommission", null)) * total/ 100);
                } else {
                    cess=Float.valueOf(Float.valueOf(preferences.getString("mincommission", null)) * Float.parseFloat(preferences.getString("basevalue", null)) / 100);
                    cess=cess+Float.valueOf(Float.valueOf(preferences.getString("maxcommission", null)) * (total-Float.parseFloat(preferences.getString("basevalue", null))) / 100);
                }
            }
            company_cess.setText("Company Cess : " + String.format("%.2f",cess));
            if (preferences.contains("cancelcharge")) {
//                Log.v("OK","true");
                cancel = Integer.parseInt(earnings.get(position).getCanceledRidesCount()) * Integer.parseInt(preferences.getString("cancelcharge", null));
            }
            cancellation.setText("Cancel : " + String.format("%.2f",cancel));
            if (preferences.contains("tax")) {
//                tottax = Float.valueOf(Float.valueOf(preferences.getString("tax", null)) * total / 100);
                float tax_amt=Float.valueOf(preferences.getString("tax", null));
                tottax=(total*tax_amt)/(100+tax_amt);
            }
            tax.setText("Tax : " + String.format("%.2f",tottax));

            ded=cess+tottax+cancel;
            deduction.setText("Deduction : Rs. "+String.format("%.2f",ded));
            float outst=0,receive=0,retrn=0;

            receive=Float.parseFloat(earnings.get(position).getOffer())+Float.parseFloat(earnings.get(position).getTotalEarning())-
                    Float.parseFloat(earnings.get(position).getCash());
            retrn=Float.parseFloat(earnings.get(position).getCancel_charge())+ded;
//            outst=Float.valueOf(earnings.get(position).getCash())-Float.parseFloat(earnings.get(position).getCancel_charge())
//                    -ded+Float.parseFloat(earnings.get(position).getOffer())+
//                    Float.parseFloat(earnings.get(position).getTotalEarning())- Float.parseFloat(earnings.get(position).getCash());
//            outst=Float.parseFloat(earnings.get(position).getTotalEarning())-Float.parseFloat(earnings.get(position).getCash());
//            outst=
            outstanding.setText("Outstanding Amount : Rs. "+String.format("%.2f",(receive-retrn)));
//            Log.v("OK",""+earnings.get(position).getDate()+" "+earnings.get(position).getCanceledRidesCount());
            return view;
        }
    }

    class BookingAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return (earnings.size()+1);
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
            view=getLayoutInflater().inflate(R.layout.account_layout,null);
            TextView date=(TextView)view.findViewById(R.id.date);
            TextView booking=(TextView)view.findViewById(R.id.booking);

            if (position!=0) {
                date.setText(earnings.get(position-1).getDate());
                booking.setText(earnings.get(position-1).getBookedRideCount());
            }

            return view;
        }
    }

    class CancelAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return (earnings.size()+1);
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
            view=getLayoutInflater().inflate(R.layout.account_layout,null);
            TextView date=(TextView)view.findViewById(R.id.date);
            TextView booking=(TextView)view.findViewById(R.id.booking);

            if (position!=0) {
                date.setText(earnings.get(position-1).getDate());
                booking.setText(earnings.get(position-1).getCanceledRidesCount());
            }

            return view;
        }
    }
}
