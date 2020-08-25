package com.music.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import com.music.util.MusicFindUtil;
import com.music.util.MusicUtil;


//软件通知栏Notification

public class MusicService extends Service {
    private final String TAG="MusicService";
    private final String MUSIC_NOTIFICATION_ACTION_PLAY = "musicnotificaion.To.PLAY";
    private final String MUSIC_NOTIFICATION_ACTION_NEXT = "musicnotificaion.To.NEXT";
    private final String MUSIC_NOTIFICATION_ACTION_PRE = "musicnotificaion.To.Pre";
    private final String MUSIC_NOTIFICATION_ACTION_NPLAY = "musicnotificaion.To.NPLAY";
    private final String MUSIC_NOTIFICATION_ACTION_NNEXT = "musicnotificaion.To.NNEXT";
    private final String MUSIC_NOTIFICATION_ACTION_NPRE = "musicnotificaion.To.NPre";
    private MusicBroadCast musicBroadCast = null;
    private MusicNotification musicNotifi = null;
    //播放音乐
    public static final String COMPLETE = "4kf";
    public static final String NCOMPLETE = "4kf1";
    public static final String START = "4k1f";
    public static final String NSTART = "4k2f";
    //暂停或者是播放音乐
    public static final String PLAYORPAUSE = "2k5o";
    public static final String NPLAYORPAUSE = "3k5o";
    //上一首音乐
    public static final String PREVIOUSMUSIC = "4si3";
    public static final String NPREVIOUSMUSIC = "42i3";
    //下一首音乐
    public static final String NEXTMUSIC = "2hd3";
    public static final String NNEXTMUSIC = "1hd3";
    //电源锁，使保持屏幕常亮
    private PowerManager.WakeLock wakeLock = null;

    private Intent intent1 = new Intent("com.example.communication.CHANGE");
    private Intent intent2 = new Intent("com.example.communication.LISTCHANGE");
    //设置电源锁
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            if (null != wakeLock) {
                wakeLock.acquire();
                if (wakeLock.isHeld()) {
                } else {
                    Toast.makeText(this, "申请电源锁失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //释放电源锁
    private void releseWakeLock() {
        if ((null != wakeLock)) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"onCreate");
        // 初始化通知栏
        musicNotifi = MusicNotification.getMusicNotification(getApplicationContext());
        musicNotifi.setContext(getBaseContext());
        NotificationManager manager=( NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        musicNotifi.setManager(manager);
        //manager.notify(0,musicNotifi);
        musicBroadCast = new MusicBroadCast();

        //动态注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_NOTIFICATION_ACTION_PLAY);
        filter.addAction(MUSIC_NOTIFICATION_ACTION_NEXT);
        filter.addAction(MUSIC_NOTIFICATION_ACTION_PRE);
        filter.addAction(MUSIC_NOTIFICATION_ACTION_NPLAY);
        filter.addAction(MUSIC_NOTIFICATION_ACTION_NNEXT);
        filter.addAction(MUSIC_NOTIFICATION_ACTION_NPRE);
        registerReceiver(musicBroadCast, filter);

        super.onCreate();
    }

     //service被关闭时回调该方法
    @Override
    public void onDestroy() {
        super.onDestroy();
        releseWakeLock();
        MusicUtil.getInstance().clean();
        MusicFindUtil.getInstance().clean();
        unregisterReceiver(musicBroadCast);//取消注册该广播接收器
        musicNotifi.onCancelMusicNotifi();
    }

    //客户端调用startService（Intent）时回调该方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acquireWakeLock();
        if (intent!=null){
            Log.e(TAG,intent.getStringExtra("action"));
            //根据action采取动作
            switch (intent.getStringExtra("action")) {
                case NSTART:
                    MusicFindUtil.getInstance().start();
                    musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case NPLAYORPAUSE:
                    MusicFindUtil.getInstance().playOrPause();
                    musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case NNEXTMUSIC:
                    MusicFindUtil.getInstance().start();
                    musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case NPREVIOUSMUSIC:
                    MusicFindUtil.getInstance().start();
                    musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case START:
                    MusicUtil.getInstance().prePlayOrNextPlay();
                    musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case NCOMPLETE:
                    MusicFindUtil.getInstance().start();
                    musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case COMPLETE:
                    MusicUtil.getInstance().prePlayOrNextPlay();
                    musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case PLAYORPAUSE:
                    MusicUtil.getInstance().playOrPause();
                    musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case PREVIOUSMUSIC:
                    MusicUtil.getInstance().prePlayOrNextPlay();
                    musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                case NEXTMUSIC:
                    MusicUtil.getInstance().prePlayOrNextPlay();
                    musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
                    sendBroadcast(intent2);
                    break;
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //客户端通过返回的IBinder对象与service组件进行通信
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void changNotifi(int i){
        if (i == 1){
            musicNotifi.onUpdataMusicNotifi(MusicUtil.getInstance().getNewSongInfo(),MusicUtil.getInstance().isPlaying());
        }else
        {
            musicNotifi.onUpdataNetMusicNotifi(MusicFindUtil.getInstance().getNewSongInfo(),MusicFindUtil.getInstance().isPlaying());
        }
    }



    //接收来自Notification的广播并控制音乐播放
    public class MusicBroadCast extends BroadcastReceiver {
        private final String MUSIC_NOTIFICATION_ACTION_PLAY = "musicnotificaion.To.PLAY";
        private final String MUSIC_NOTIFICATION_ACTION_NEXT = "musicnotificaion.To.NEXT";
        private final String MUSIC_NOTIFICATION_ACTION_PRE = "musicnotificaion.To.PRE";
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case MUSIC_NOTIFICATION_ACTION_PLAY :
                    Intent startIntent1 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent1.putExtra("action",MusicService.PLAYORPAUSE);
                    //第一次调用startService的时候：onCreate→onStartCommand
                    //再次调用startService的时候：只执行onStartCommand
                    startService(startIntent1);
                    sendBroadcast(intent1);//接收系统发送的广播
                    break;
                case MUSIC_NOTIFICATION_ACTION_NEXT:
                    MusicUtil.getInstance().next();
                    Intent startIntent3 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent3.putExtra("action",MusicService.NEXTMUSIC);
                    startService(startIntent3);
                    sendBroadcast(intent1);
                    break;
                case MUSIC_NOTIFICATION_ACTION_PRE:
                    MusicUtil.getInstance().pre();
                    Intent startIntent2 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent2.putExtra("action",MusicService.PREVIOUSMUSIC);
                    startService(startIntent2);
                    sendBroadcast(intent1);
                    break;
                case MUSIC_NOTIFICATION_ACTION_NPLAY :
                    Intent startIntent4 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent4.putExtra("action",MusicService.NPLAYORPAUSE);
                    startService(startIntent4);
                    sendBroadcast(intent1);
                    break;
                case MUSIC_NOTIFICATION_ACTION_NNEXT:
                    MusicFindUtil.getInstance().next();
                    Intent startIntent5 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent5.putExtra("action",MusicService.NNEXTMUSIC);
                    startService(startIntent5);
                    sendBroadcast(intent1);
                    break;
                case MUSIC_NOTIFICATION_ACTION_NPRE:
                    MusicFindUtil.getInstance().pre();
                    Intent startIntent6 = new Intent(getApplicationContext(), MusicService.class);
                    startIntent6.putExtra("action",MusicService.NPREVIOUSMUSIC);
                    startService(startIntent6);
                    sendBroadcast(intent1);
                    break;
            }

    }
}
}
