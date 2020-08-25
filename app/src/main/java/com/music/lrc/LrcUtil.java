package com.music.lrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class LrcUtil {

    public static String getLrcFromAssets(String Url,int i) {
        if (i == 1) {
            if (Url.equals("")) {
                return "";
            }
            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                InputStream input = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                String line = "";
                String result = "";
                while ((line = in.readLine()) != null) {
                    if (line.trim().equals(""))
                        continue;
                    result += line + "\r\n";
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
        else{
            String str1 = Url.replaceAll("&#58;",":").replaceAll("&#46;",".").replaceAll("&#32;"," ").replaceAll("&#10;","\n").replaceAll("&#13;","").replaceAll("&#45;","-");
            return str1;

        }
    }
}

