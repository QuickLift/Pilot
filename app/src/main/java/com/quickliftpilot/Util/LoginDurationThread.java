package com.quickliftpilot.Util;

import android.widget.TextView;

import com.quickliftpilot.R;
import com.quickliftpilot.activities.Welcome;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pandey on 2/3/18.
 */

public class LoginDurationThread extends Thread {
    private static Welcome welcome;
    private static String startTime;
    private static Date start_time,current_time;
    private static DateFormat formatter;
    private static TextView login_duration;
    private static long diffHr;

    public LoginDurationThread(Welcome original_welcome){
        welcome = original_welcome;
        login_duration = (TextView)welcome.findViewById(R.id.login_duration);
        start_time = new Date(System.currentTimeMillis());
        formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        startTime = formatter.format(start_time);
    }
    @Override
    public void run() {
        super.run();
        current_time = new Date(System.currentTimeMillis());
        long diff = current_time.getTime() - start_time.getTime();
        diffHr = diff/(60*60*1000)%24;
    }

    public long getDuration(){
        return diffHr;
    }
}
