package com.music.lrc;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;



public class LrcJsonUtil {
    //返回c歌词的url
    public static String parseJOSNWithGSON(Response response ,int c){
        try{
            String ResponsData = response.body().string();
            JSONObject jsonObject = new JSONObject(ResponsData);

            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count>=c){
                String result = jsonObject.getString("result");
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject1 = jsonArray.getJSONObject(c-1);
                String url = jsonObject1.getString("lrc");
                return url;
            }else {
                return "";
            }
        }catch (Exception e){

        }
        return "";

    }
    //返回歌词的url
    public static String parseNetJOSNWithGSON(Response response){
        try{
            String ResponsData = response.body().string();
            JSONObject jsonObject = new JSONObject(ResponsData);

            String pagebean = jsonObject.getJSONObject("lrc").getString("lyric");
            return pagebean;
            }
        catch (Exception e){

        }
        return "";

    }
}
