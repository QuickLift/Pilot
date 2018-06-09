package com.quickliftpilot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.quickliftpilot.R;
import com.quickliftpilot.model.RideHistory;

import java.util.List;

/**
 * Created by ekagga on 15/6/17.
 **/

public class DriveListAdapter extends BaseAdapter {
    /**
     * custom adapter to display list of drive info
     * */
    private List<RideHistory> feedList;
    private String mRidesName;

    public DriveListAdapter(List<RideHistory> feedList,String mRidesName) {
        this.feedList = feedList;
        this.mRidesName=mRidesName;
    }

    @Override
    public int getCount() {
        return this.feedList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflater the layout
        LayoutInflater inflater =(LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View row = inflater.from(parent.getContext()).inflate(R.layout.custom_feed_item,parent,false);

//                TextView DriveDate =(TextView)row.findViewById(R.id.item_date);
//                TextView DriveCount =(TextView)row.findViewById(R.id.items_count);
//                DriveDate.setText(feedList.get(position).getDate());
//
//                if(mRidesName.equalsIgnoreCase("BookedRideCount")){
//                    Log.i("yogendra","yogendra Adapter: "+mRidesName.toString());
//                    DriveCount.setText(Integer.toString(feedList.get(position).getFeed().getBookedRideCount()));
//                }else if ((mRidesName.equalsIgnoreCase("CanceledRidesCount"))){
//                    DriveCount.setText(Integer.toString(feedList.get(position).getFeed().getCanceledRidesCount()));
//                }else if ((mRidesName.equalsIgnoreCase("TotalEarning"))){
//                    DriveCount.setText(Float.toString(feedList.get(position).getFeed().getTotalEarning()));
//                }

        return row;
    }
}


