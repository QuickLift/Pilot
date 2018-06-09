package com.quickliftpilot.Util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.quickliftpilot.activities.MapActivity;
import com.quickliftpilot.activities.RidesActivity;
import com.quickliftpilot.activities.TripHandlerActivity;
import com.quickliftpilot.model.SequenceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by adarsh on 6/4/18.
 */

public class BestRoute extends AsyncTask<Object,String,String> {
    private Stack<SequenceModel> stack;
    String url,data;
    ArrayList<SequenceModel> sequence;
    Context context;

    public BestRoute(Context context){
        this.context=context;
    }

    @Override
    protected String doInBackground(Object... objects) {
        url = (String) objects[0];
        sequence=(ArrayList<SequenceModel>) objects[1];
        stack=(Stack<SequenceModel>) objects[2];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            data = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        JSONArray jsonArray=null;
        JSONObject jsonObject = null;
        Log.v("TAG","hi"+stack.size()+" "+sequence.size());
        try {
            jsonObject=new JSONObject(s);
            jsonArray=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").length()-1;i>=0;i--) {
                Log.v("TAG",""+sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getName()+" "+sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getType());
                stack.push(sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)));

//                Log.v("TAG",""+sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getName()+""
//                        +sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getType());
//                sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getName()
            }
            if (MapActivity.fa!=null)
                MapActivity.fa.finish();
            if (RidesActivity.RideActivity!=null)
                RidesActivity.RideActivity.finish();
            if (TripHandlerActivity.TripHandler!=null)
                TripHandlerActivity.TripHandler.finish();
            Intent intent=new Intent(context, MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
//            Log.v("TAG",""+stack.size());
//            for (int i=0;i<jsonArray.length()-1;i++) {
//                Log.v("TAG", jsonArray.getJSONObject(i).getJSONObject("distance").getString("text"));
//               // Log.v("TAG", jsonArray.getJSONObject(1).getJSONObject("distance").getString("text"));
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}