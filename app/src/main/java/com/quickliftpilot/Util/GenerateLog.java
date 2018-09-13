package com.quickliftpilot.Util;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by adarsh on 6/6/18.
 */

public class GenerateLog {
    static File logFile;

    public GenerateLog() {
        logFile = new File(Environment.getExternalStorageDirectory()+"/QuickLift/log_driver.txt");
        if (!logFile.exists()){
            try{
                File log = new File(Environment.getExternalStorageDirectory()+"/QuickLift/");
                log.mkdir();
                logFile.createNewFile();
            }
            catch (IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    static public void appendLog(String tag, String text){
//        File logFile = new File(Environment.getExternalStorageDirectory()+"/QuickLift/log.txt");
//        if (!logFile.exists()){
//            try{
//                logFile.createNewFile();
//            }
//            catch (IOException e){
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        try{
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(getCurrentTime()+tag+" : "+text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        Log.v("Save","hi");
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss : ");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime())+"\t";
    }
}
