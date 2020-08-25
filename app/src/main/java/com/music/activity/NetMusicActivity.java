package com.music.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.music.R;
import com.music.activity.fragment.LrcFragment;
import com.music.adapter.FragmentAdapter;
import com.music.bean.MusicFind;
import com.music.data.LrcData;
import com.music.data.LrcDataUtil;
import com.music.lrc.DefaultLrcBulider;
import com.music.lrc.ILrcBulider;
import com.music.lrc.ILrcViewListener;
import com.music.lrc.LrcJsonUtil;
import com.music.lrc.LrcRow;
import com.music.lrc.LrcUtil;
import com.music.lrc.LrcView;
import com.music.service.MusicService;
import com.music.util.HttpUtil;
import com.music.util.MusicFindUtil;
import com.music.util.MusicUtil;
import com.music.widget.IndicatorLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import qiu.niorgai.StatusBarCompat;

import static com.music.util.MusicUtil.TYPE_SINGLE;

public class NetMusicActivity extends AppCompatActivity {
    private static final String TAG="NetMusicActivity";
    @Bind(R.id.iv_back)
    ImageView ivBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_singer)
    TextView tvSinger;
    @Bind(R.id.iv_more)
    ImageView ivMore;
    @Bind(R.id.vp_musci_play)
    ViewPager vpMusciPlay;
    @Bind(R.id.lrc_mLrcView)
    LrcView mLrcView;
    @Bind(R.id.tv_current_time)
    TextView tvCurrentTime;
    @Bind(R.id.sb_progress)
    SeekBar sbProgress;
    @Bind(R.id.tv_total_time)
    TextView tvTotalTime;
    @Bind(R.id.iv_mode)
    ImageView ivMode;
    @Bind(R.id.iv_prev)
    ImageView ivPrev;
    @Bind(R.id.iv_play)
    ImageView ivPlay;
    @Bind(R.id.iv_next)
    ImageView ivNext;
    @Bind(R.id.il_indicator)
    IndicatorLayout ilIndicator;
    FragmentAdapter adapter;
    MusicFind musicFind;
    private static boolean isplay = true;
    private MusicService musicService;
    private int mode = MusicUtil.TYPE_ORDER;
    private MsgReceiver msgReceiver;
    private Timer timer;
    private TimerTask timerTask;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private LrcDataUtil lrcDataUtil;
    String lrc;
    private LrcData lrcdata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#5d5d5d"));//设置系统状态栏颜色
        setContentView(R.layout.activity_net_music);//加载音乐播放
        ButterKnife.bind(this);

        //加载音量调节框
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new LrcFragment());
        adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        vpMusciPlay.setAdapter(adapter);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });//监听返回键
        initData();
        initLRC(musicFind);
        //监听Media Player的播放完成事件
        MusicFindUtil.getInstance().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            //因为没有附着在活动中所以就算活动finish掉，还是可以有监听~喵~
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                MusicFindUtil.getInstance().comNext();
                Intent startIntent4 = new Intent(NetMusicActivity.this, MusicService.class);
                startIntent4.putExtra("action", MusicService.NCOMPLETE);
                startService(startIntent4);
                changInfo();

            }
        });
        //监听音乐缓冲进度
        MusicFindUtil.getInstance().getMediaPlayer().setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                sbProgress.setSecondaryProgress(percent);
            }
        });
        //监听歌词
        mLrcView.setListener(new ILrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (MusicFindUtil.getInstance().getMediaPlayer() != null) {
                    MusicFindUtil.getInstance().getMediaPlayer().seekTo((int) row.time);
                }
            }
        });
    }
    //初始化
    public void initData(){
        musicFind = (MusicFind) getIntent().getSerializableExtra("songNetInfo");
        if (musicFind == null){
            musicFind =MusicFindUtil.getInstance().getMusic();
        }
        lrcDataUtil = new LrcDataUtil(this);
        //若数据库未找到，更新
        if (MusicFindUtil.getInstance().getCurrentSongPosition()== -1){
            MusicFindUtil.getInstance().setCurrentSongPosition(musicFind.getPosition());
            Intent startIntent1 = new Intent(this, MusicService.class);
            startIntent1.putExtra("action", MusicService.NSTART);
            startService(startIntent1);
            isplay = true;
        }
        //若数据库的与当前不匹配，更新
        if (!musicFind.getId().equals(MusicFindUtil.getInstance().getID())) {
            MusicFindUtil.getInstance().setCurrentSongPosition(musicFind.getPosition());
            Intent startIntent1 = new Intent(this, MusicService.class);
            startIntent1.putExtra("action", MusicService.NSTART);
            startService(startIntent1);
            isplay = true;
        } else {
            //播放
            isplay = MusicFindUtil.getInstance().isPlaying();
        }

        MusicFindUtil.getInstance().setCurrentSongPosition(musicFind.getPosition());
        if (isplay == true) {//播放展示暂停按钮
            ivPlay.setImageResource(R.drawable.play_btn_pause_selector);
        } else {
            ivPlay.setImageResource(R.drawable.play_btn_play_selector);
        }
        //更新播放模式
        if (mode != MusicFindUtil.getInstance().getPattern()) {
            mode = MusicFindUtil.getInstance().getPattern();
        }
        tvTitle.setText(musicFind.getSongname());
        tvSinger.setText(musicFind.getSingername());
        ilIndicator.create(vpMusciPlay.getCurrentItem());
        Intent startIntent = new Intent(this, MusicService.class);
        bindService(startIntent, connection, BIND_AUTO_CREATE);//传递connection
        if (mode == MusicUtil.TYPE_ORDER) {
            ivMode.setImageResource(R.drawable.play_btn_loop_selector);
        } else if (mode == TYPE_SINGLE) {
            ivMode.setImageResource(R.drawable.play_btn_one_selector);
        } else {
            ivMode.setImageResource(R.drawable.play_btn_shuffle_selector);
        }
        //音乐进度条设置
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isSeekBarChanging == true) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {//Activity内部更新UI
                        sbProgress.setMax(MusicFindUtil.getInstance().getMediaPlayer().getDuration());
                        tvTotalTime.setText(formatTime("mm:ss", MusicFindUtil.getInstance().getMediaPlayer().getDuration()));
                        final long timePassed = MusicFindUtil.getInstance().getCurrentPosition();
                        sbProgress.setProgress((int) timePassed);//进度条
                        mLrcView.seekLrcToTime(timePassed);//高亮正在播放的那句歌词
                        tvCurrentTime.setText(formatTime("mm:ss", MusicFindUtil.getInstance().getCurrentPosition()));
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);//1000ms刷新一次
        sbProgress.setOnSeekBarChangeListener(new MySeekbar());
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();//实例化
        intentFilter.addAction("com.example.communication.CHANGE");//添加监听广播的类型
        registerReceiver(msgReceiver, intentFilter);//注册广播
        lrcdata = new LrcData(this);
    }
    //初始化歌词
    public void initLRC(final MusicFind musicFind) {
        lrc = lrcDataUtil.findLrc(musicFind.getSongname()+"-"+musicFind.getSingername());
        Log.e(TAG,"数据库歌词"+lrc);
        if (lrc.equals("")) {
            //获取网络歌词
            HttpUtil.requstNetLrcData(musicFind.getId(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG,"歌词失败");
                    lrc = "";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ILrcBulider bulider = new DefaultLrcBulider();
                            List<LrcRow> rows = bulider.getLrcRows(lrc);
                            mLrcView.setLrc(rows);//展示歌词
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    lrc = LrcJsonUtil.parseNetJOSNWithGSON(response);
                    Log.e(TAG,lrc);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //将歌词、歌曲名、歌手名存入数据库
                            lrcDataUtil.addLrc(lrc, musicFind.getSongname()+"-"+musicFind.getSingername());
                            ILrcBulider bulider = new DefaultLrcBulider();
                            List<LrcRow> rows = bulider.getLrcRows(lrc);
                            mLrcView.setLrc(rows);
                        }
                    });
                }
            });
        } else {
            //直接显示歌词
            ILrcBulider bulider = new DefaultLrcBulider();
            List<LrcRow> rows = bulider.getLrcRows(lrc);
            mLrcView.setLrc(rows);
        }
    }

    //广播接收器
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            changInfo();//重新加载歌曲
            isplay = MusicFindUtil.getInstance().isPlaying();
            if (isplay == true) {
                ivPlay.setImageResource(R.drawable.play_btn_pause_selector);
            } else {
                ivPlay.setImageResource(R.drawable.play_btn_play_selector);
            }
        }
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_net, popupMenu.getMenu());
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {

            }
        });
        popupMenu.show();
    }

    //活动结束
    @Override
    protected void onDestroy() {
        unbindService(connection);//断开与服务的连接
        unregisterReceiver(msgReceiver);//注销广播
        super.onDestroy();
    }

    //监听各种事件
    @OnClick({R.id.iv_back, R.id.iv_more, R.id.iv_mode, R.id.iv_prev, R.id.iv_play, R.id.iv_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                break;
            case R.id.iv_more:
                showPopupMenu(ivMore);
                break;
            case R.id.iv_mode:
                if (mode == MusicUtil.TYPE_ORDER) {
                    Toast.makeText(this, "Shuffle Playback", Toast.LENGTH_SHORT).show();
                    mode = MusicUtil.TYPE_RANDOM;
                    ivMode.setImageResource(R.drawable.play_btn_shuffle_selector);
                    MusicUtil.getInstance().setPatten(MusicUtil.TYPE_RANDOM);
                } else if (mode == MusicUtil.TYPE_RANDOM) {
                    Toast.makeText(this, "Single cycle", Toast.LENGTH_SHORT).show();
                    this.mode = TYPE_SINGLE;
                    ivMode.setImageResource(R.drawable.play_btn_one_selector);
                    MusicUtil.getInstance().setPatten(TYPE_SINGLE);

                } else if (mode == TYPE_SINGLE) {
                    Toast.makeText(this, "List cycle", Toast.LENGTH_SHORT).show();
                    this.mode = MusicUtil.TYPE_ORDER;
                    ivMode.setImageResource(R.drawable.play_btn_loop_selector);
                    MusicUtil.getInstance().setPatten(MusicUtil.TYPE_ORDER);
                }
                break;
            case R.id.iv_prev:
                if (!isplay) {
                    isplay = true;
                    ivPlay.setImageResource(R.drawable.play_btn_pause_selector);
                }
                MusicFindUtil.getInstance().pre();
                changInfo();
                Intent startIntent2 = new Intent(this, MusicService.class);
                startIntent2.putExtra("action", MusicService.NPREVIOUSMUSIC);
                startService(startIntent2);
                musicService.changNotifi(2);
                break;
            case R.id.iv_play:
                if (isplay) {
                    ivPlay.setImageResource(R.drawable.play_btn_play_selector);
                    isplay = false;
                } else {
                    isplay = true;
                    ivPlay.setImageResource(R.drawable.play_btn_pause_selector);
                }
                Intent startIntent1 = new Intent(this, MusicService.class);
                startIntent1.putExtra("action", MusicService.NPLAYORPAUSE);
                startService(startIntent1);
                break;
            case R.id.iv_next:
                MusicFindUtil.getInstance().next();
                changInfo();
                if (!isplay) {
                    isplay = true;
                    ivPlay.setImageResource(R.drawable.play_btn_pause_selector);
                }
                Intent startIntent3 = new Intent(this, MusicService.class);
                startIntent3.putExtra("action", MusicService.NNEXTMUSIC);
                startService(startIntent3);
                break;
        }
    }

    //监听拖动条
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        //拖动过程中
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }
        //当按下时
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        //当松开时
        public void onStopTrackingTouch(SeekBar seekBar) {
            MusicUtil.getInstance().getMediaPlayer().seekTo(seekBar.getProgress());
            isSeekBarChanging = false;
        }

    }

    //时间转换
    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }
    //服务连接
    private ServiceConnection connection = new ServiceConnection() {
        //系统会调用该方法以传递服务的onBind()返回的IBinder
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service1) {
            musicService = ((MusicService.MusicBinder) service1).getService();
            musicService.changNotifi(2);
        }
        //Android系统会在与服务的连接以外中断时调用该方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    //重新加载歌曲
    public void changInfo() {
        MusicFind newSong = MusicFindUtil.getInstance().getNewSongInfo();
        musicFind =newSong;
        if (newSong == null)
            return;
        sbProgress.setMax((int) MusicFindUtil.getInstance().getMediaPlayer().getDuration());
        tvTotalTime.setText(formatTime("mm:ss", MusicFindUtil.getInstance().getMediaPlayer().getDuration()));
        tvTitle.setText(newSong.getSongname());
        tvSinger.setText(newSong.getSingername());
        List<LrcRow> rowList = new ArrayList<>();
        mLrcView.setLrc(rowList);
        initLRC(newSong);
    }
}