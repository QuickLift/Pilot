package com.quickliftpilot.Util;

import android.util.Log;

import com.quickliftpilot.model.SequenceModel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

/**
 * Created by pandey on 1/4/18.
 */

public class RequestDetails {
    static ArrayList<Map<String,Object>> request_list = null;

    public static ArrayList<Map<String, Object>> getRequest_list() {
        return request_list;
    }

    public static void setRequest_list(ArrayList<Map<String, Object>> request_list) {
        RequestDetails.request_list = request_list;
    }
}
