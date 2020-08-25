package com.music.util;

import android.os.Build;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class HttpUtil {
    public final static String TAG = "HttpUtil";

    public static void requestStringData(final String topid, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.e(TAG,"http://119.45.120.159:3000/top/playlist/highquality?limit=2&cat=" + topid);
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/top/playlist/highquality?limit=5&cat=" + topid)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 歌单详情
     * @param id
     * @param callback
     */
    public static void requestAll(final String id, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.e(TAG,"http://119.45.120.159:3000/playlist/detail?id=" + id);
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/playlist/detail?id=" + id)
                .get().build();
        client.newCall(request).enqueue(callback);
    }
    public static void requestSongData(final String keyword, final int page, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.e(TAG, "http://119.45.120.159:3000/search?keywords=" + keyword + "&limit=20&offset=" + (page - 1) * 20);
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/search?keywords=" + keyword + "&limit=20&offset=" + (page - 1) * 20)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    public static void requestSongUrl(String id, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.e(TAG,"http://119.45.120.159:3000/song/url?id=" + id);
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/song/url?id=" + id)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    public static void requestSongDetail(String id, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/song/detail?ids=" + id)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    private static String makeUA() {
        final String ua = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
        Log.d("=======", "makeUA: " + ua);
        return ua;
    }

    public static void requstLrcData(final String name, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String name1 = name.trim();
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.0&method=baidu.ting.search.lrcpic&format=json&query=" + name1)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    public static void requstNetLrcData(final String id, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.e(TAG,"http://119.45.120.159:3000/lyric?id=" + id);
        Request request = new Request.Builder().addHeader("User-Agent", makeUA())
                .url("http://119.45.120.159:3000/lyric?id=" + id)
                .get().build();
        client.newCall(request).enqueue(callback);
    }
}

