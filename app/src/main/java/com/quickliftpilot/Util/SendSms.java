package com.quickliftpilot.Util;

import android.util.Log;

import com.quickliftpilot.activities.Login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendSms extends Thread{
    // Construct data

    String apiKey = "apikey=" + "bqvQUgZIuxg-FeJqx9u1RMutVzVDtLw9haP2VNQ5DH";
    String message = "&message=";
    String sender = "&sender=" + "QIKLFT";          //TXTLCL
    String numbers = "&numbers=";
    Login login;

    public SendSms(String msg, String num) {
        message = message + msg;
        numbers = numbers + num;
    }


    public String sendSms() {
        try {
            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuffer.append(line);
            }
            rd.close();

            return stringBuffer.toString();
        } catch (Exception e) {
            System.out.println("Error SMS "+e);
            return "Error "+e;
        }
    }

    @Override
    public void run() {
        String msg = sendSms();
        Log.v("TAG",msg);
    }
}
