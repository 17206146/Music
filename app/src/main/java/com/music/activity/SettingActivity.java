package com.music.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;

import com.music.R;
import com.suke.widget.SwitchButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    @Bind(R.id.iv_back)
    ImageView ivBack;
    @Bind(R.id.tb_main)
    Toolbar tbMain;
    @Bind(R.id.switch_button)
    SwitchButton switchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("setting",MODE_PRIVATE);
        switchButton.setChecked(preferences.getBoolean("net",true));
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                SharedPreferences.Editor editor = getSharedPreferences("setting",MODE_PRIVATE).edit();
                editor.putBoolean("net",b);
                editor.apply();
            }
        });
    }


    @OnClick({R.id.iv_back})
    public void onViewClicked(View view) {
        if (view.getId()==R.id.iv_back) {
                finish();
        }
    }
}
