package com.music.download;


public interface DownloadListener {
    void onProgress(int progress,String name);
    void onSuccess(String name);
    void onFailed();
    void onPause();
    void onCanceled(String name);
}
