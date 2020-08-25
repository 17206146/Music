package com.music.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import com.music.R;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        requestPermission();
    }

    private void requestPermission(){
        //如访问权限未授权 则进行授权申请
        boolean isAllAllow=true;
        String []permissions=new String[]{ Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.WAKE_LOCK,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,Manifest.permission.DISABLE_KEYGUARD,Manifest.permission.ACCESS_NOTIFICATION_POLICY,Manifest.permission.MODIFY_AUDIO_SETTINGS};
        for(String s:permissions){
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                isAllAllow=false;
                break;
            }
        }
        if (!isAllAllow) {
            ActivityCompat.requestPermissions(this,permissions, 100);
        } else {
            //若权限已授予
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(FirstActivity.this, MainActivity.class));
                    finish();
                }
            }, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==100){
            boolean isAllAllow=true;
            for(int s:grantResults){
                if(s!=PackageManager.PERMISSION_GRANTED){
                    isAllAllow=false;
                    finish();
                }
            }
            if(isAllAllow){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FirstActivity.this, MainActivity.class));
                        finish();
                    }
                }, 1000);
            }
        }
    }
}
