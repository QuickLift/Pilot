package com.quickliftpilot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickliftpilot.R;

import java.util.ArrayList;

public class CancelReason extends AppCompatActivity {
    ListView list;
    DatabaseReference ref;
    ProgressDialog dialog;
    ArrayList<String> reason=new ArrayList<>();
    int selected=0;
    String id;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent();
        setResult(RESULT_CANCELED, intent);
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
        setContentView(R.layout.activity_cancel_reason);

        getSupportActionBar().setTitle(R.string.Cancel_Reason);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ref= FirebaseDatabase.getInstance().getReference("DriverCancelReason");
        list=(ListView)findViewById(R.id.list);

        id=getIntent().getStringExtra("id");

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Please Wait ...");
        dialog.show();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Toast.makeText(CancelReason.this, "hi", Toast.LENGTH_SHORT).show();
                reason.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        reason.add(data.getValue().toString());
                    }
                    list.setAdapter(new CustomAdapter());
                }
                dialog.cancel();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void select (View view){
        Intent intent=new Intent();
        intent.putExtra("reason",reason.get(selected));
        intent.putExtra("id",id);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancel (View view){
        Intent intent=new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return reason.size();
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
            view=getLayoutInflater().inflate(R.layout.reason_layout,null);
            RadioButton btn=(RadioButton)view.findViewById(R.id.select);
            btn.setText(reason.get(position));

            if (selected==position)
                btn.setChecked(true);

            btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selected = position;
                        list.setAdapter(new CustomAdapter());
                    }
                }
            });

            return view;
        }
    }
}
