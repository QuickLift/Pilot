package com.quickliftpilot.Util;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.quickliftpilot.model.SequenceModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by adarsh on 6/4/18.
 */

public class CheckRoute extends AsyncTask<Object,String,String> {
    private Stack<SequenceModel> stack;
    String url,url2,data,data2,key;
    ArrayList<SequenceModel> sequence;
    SharedPreferences log_id;

    @Override
    protected String doInBackground(Object... objects) {
        url = (String) objects[0];
        url2 = (String) objects[1];
        log_id=(SharedPreferences) objects[2];
        key = (String) objects[3];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            data = downloadUrl.readUrl(url);
            data2 = downloadUrl.readUrl(url2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data2;
    }

    @Override
    protected void onPostExecute(String s) {

        Log.v("TAG",""+data);
        Log.v("TAG",""+data2);
        JSONArray jsonArray=null,jsonArray2=null;
        JSONObject jsonObject = null,jsonObject2 = null;

        try {
            jsonObject=new JSONObject(data);
            jsonObject2=new JSONObject(data2);
            jsonArray=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            jsonArray2=jsonObject2.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
//            for (int i=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").length()-1;i>=0;i--) {
//                stack.push(sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)));
//            }
            Float tot1=null,tot2=null;
            for (int i=0;i<jsonArray.length()-1;i++) {
                if (tot1==null){
                    tot1=Float.valueOf(jsonArray.getJSONObject(i).getJSONObject("distance").getString("value"));
                    Log.v("Total",jsonArray.getJSONObject(i).getJSONObject("distance").getString("value"));
                }
                else {
                    tot1=tot1+Float.valueOf(jsonArray.getJSONObject(i).getJSONObject("distance").getString("value"));
                    Log.v("Total",jsonArray.getJSONObject(i).getJSONObject("distance").getString("value"));
                }
//                Log.v("TAG", "hi "+jsonArray.getJSONObject(i).getJSONObject("distance").getString("text"));
                // Log.v("TAG", jsonArray.getJSONObject(1).getJSONObject("distance").getString("text"));
            }
            for (int i=0;i<jsonArray2.length()-1;i++) {
                if (tot2==null){
                    tot2=Float.valueOf(jsonArray2.getJSONObject(i).getJSONObject("distance").getString("value"));
                }
                else {
                    tot2=tot2+Float.valueOf(jsonArray2.getJSONObject(i).getJSONObject("distance").getString("value"));
                }
//                Log.v("TAG", "hello "+jsonArray2.getJSONObject(i).getJSONObject("distance").getString("text"));
                // Log.v("TAG", jsonArray.getJSONObject(1).getJSONObject("distance").getString("text"));
            }
//            Log.v("Total",""+data+" "+data2+" "+tot1+" "+tot2+" "+(tot2-tot1));
            Log.v("Total",""+tot1+" "+tot2+" "+(tot2-tot1));
            if ((tot2-tot1)<5000 && (tot2-tot1)>-5000){
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Share/"+key+"/drivers");
                ref.child(log_id.getString("id",null)).setValue("True");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}