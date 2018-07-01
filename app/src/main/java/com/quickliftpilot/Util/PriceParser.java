package com.quickliftpilot.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by adarsh on 27/4/18.
 */

public class PriceParser {
    private HashMap<String,String> getDuration(JSONArray jsonArray) {
        HashMap<String,String> googleDirectionsMap=new HashMap<>();
        String duration="";
        String distance="";

        try {
            duration=jsonArray.getJSONObject(0).getJSONObject("duration").getString("value");
            distance=jsonArray.getJSONObject(0).getJSONObject("distance").getString("value");

            googleDirectionsMap.put("duration",duration);
            googleDirectionsMap.put("distance",distance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googleDirectionsMap;
    }

    public HashMap<String,String> parseDirections(String jsonData){
        JSONArray jsonArray=null;
        JSONObject jsonObject;

        try {
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }
}
