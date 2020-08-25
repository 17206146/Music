package com.music.activity.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.music.R;

import butterknife.Bind;
import butterknife.ButterKnife;

//音量调节的实现

public class LrcFragment extends Fragment {
    @Bind(R.id.sb_volume)
    SeekBar sbVolume;

    private AudioManager mAudioManager;
    private ContentObserver mVoiceObserver;
    private MyVolumeReceiver mVolumeReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_page_lrc, container, false);//加载音乐拖动条
        ButterKnife.bind(this, view);
        initVolume();
        //监听音量拖动条
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                NotificationManager n = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (!n.isNotificationPolicyAccessGranted()) {
                    Intent intent = new Intent(
                            android.provider.Settings
                                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                    startActivity(intent);
                }
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, i, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mVoiceObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
            }
        };
        //屏蔽系统调节音量控件
        View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                }
                return true;
            }
        };
        return view;
    }

    //初始化
    private void initVolume() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        myRegisterReceiver();//注册同步更新的广播
    }

    //注册当音量发生变化时接收的广播
    private void myRegisterReceiver() {
        mVolumeReceiver = new MyVolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    // 处理音量变化时的界面显示
    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前的媒体音量
                sbVolume.setProgress(currVolume);
            }
        }
    }

    //解除注册
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(mVolumeReceiver);
        ButterKnife.unbind(this);
    }
}
