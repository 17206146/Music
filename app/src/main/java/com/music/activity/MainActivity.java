package com.music.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.R;
import com.music.activity.fragment.LocalFragment;
import com.music.activity.fragment.NetFragment;
import com.music.adapter.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import qiu.niorgai.StatusBarCompat;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.tb_main)
    Toolbar tbMain;
    @Bind(R.id.tv_local)
    TextView tvLocal;
    @Bind(R.id.tv_network)
    TextView tvNetwork;
    @Bind(R.id.vv_main)//活动条
    LinearLayout vvMain;
    @Bind(R.id.vp_main)//主题
    ViewPager vpMain;
    @Bind(R.id.iv_down)
    ImageView ivDown;
    @Bind(R.id.iv_setting)
    ImageView ivSetting;
    private FragmentAdapter adapter;
    private int currentIndex;
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#da3318"));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Bmob.initialize(this, "732e01a3793251f76d393dd09d5766aa");

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new NetFragment());
        fragments.add(new LocalFragment());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth() - dip2px(this, 20);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vvMain.getLayoutParams();//拖动条的切换
        lp.width = screenWidth / 2;
        vvMain.setLayoutParams(lp);
        adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        vpMain.setAdapter(adapter);
        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //页面开始跳转
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vvMain.getLayoutParams();
                if (currentIndex == 0 && position == 0)// 0->1
                {
                    lp.leftMargin = (int) (positionOffset * screenWidth / 2);
                } else if (currentIndex == 1 && position == 0) {
                    lp.leftMargin = (int) ((1 + positionOffset) * screenWidth / 2);
                }
                vvMain.setLayoutParams(lp);
            }

            //页面跳转结束
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tvLocal.setTextColor(Color.parseColor("#da3318"));
                    tvNetwork.setTextColor(Color.BLACK);
                    currentIndex = 0;
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vvMain.getLayoutParams();
                    lp.leftMargin = 0;
                    vvMain.setLayoutParams(lp);
                } else if (position == 1) {
                    tvLocal.setTextColor(Color.BLACK);
                    tvNetwork.setTextColor(Color.parseColor("#da3318"));
                    currentIndex = 1;
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vvMain.getLayoutParams();
                    lp.leftMargin = screenWidth / 2;
                    vvMain.setLayoutParams(lp);
                }
            }

            //跳转页面时的状态变化
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //默认为网络音乐界面
        vpMain.setCurrentItem(0);
        tvLocal.setTextColor(Color.parseColor("#da3318"));
        tvNetwork.setTextColor(Color.BLACK);
        currentIndex = 0;

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @OnClick({R.id.tv_local, R.id.tv_network, R.id.iv_down,R.id.iv_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_local:
                vpMain.setCurrentItem(0);
                break;
            case R.id.tv_network:
                vpMain.setCurrentItem(1);
                break;
            case R.id.iv_setting:
                Intent intent1 = new Intent();
                intent1.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent1);
        }
    }

}
