package com.quickliftpilot.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by adarsh on 4/1/18.
 */

public class DownloadUrl {
    public String readUrl(String myurl) throws IOException {
        String data="";
        InputStream inputStream=null;
        HttpURLConnection urlConnection=null;

        try {
            URL url=new URL(myurl);
            urlConnection=(HttpURLConnection)url.openConnection();
            urlConnection.connect();

            inputStream=urlConnection.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb=new StringBuffer();

            String line="";
            while ((line=br.readLine())!=null){
                sb.append(line);
            }
            data=sb.toString();
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inputStream.close();
            urlConnection.disconnect();
        }

        return data;
    }
}
