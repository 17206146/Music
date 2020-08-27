package com.music.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.music.R;
import com.music.adapter.MusicFenAdapter;
import com.music.bean.MusicFind;
import com.music.util.HttpUtil;
import com.music.util.MusicFindUtil;
import com.music.util.NetStateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MusicListActivity extends BaseActivity {
    @Bind(R.id.tv_toolbar)
    TextView tvToolbar;
    @Bind(R.id.tb_main)
    Toolbar tbMain;
    private String typeid ;
    @Bind(R.id.rv_fenlei_list)
    RecyclerView rvFenleiList;
    MusicFenAdapter musicFenAdapter;
    MsgReceiver msgReceiver;
    private int netState = 0;
    private SparseArray<String> maps = new SparseArray<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        ButterKnife.bind(this);

        typeid = getIntent().getStringExtra("musictype2");

        setToolBar(R.id.tb_main);
        tvToolbar.setText(getIntent().getStringExtra("musictype"));
        initWhiteHome();

        netState = NetStateUtil.getNetWorkState(getBaseContext());
        showProgressDialog();
        HttpUtil.requestStringData(typeid, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dissmiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ArrayList<String> songListIds=new ArrayList<>();
                try {
                    JSONObject jsonObject=new JSONObject(response.body().string());
                    JSONArray array=jsonObject.getJSONArray("playlists");
                    for(int i=0;i<array.length();i++){
                        songListIds.add(array.getJSONObject(i).getString("id"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CountDownLatch latch = new CountDownLatch(songListIds.size());
                //找出两个歌单所有歌曲
                final ArrayList<MusicFind> songs=new ArrayList<>();
                for(String ids:songListIds){
                    HttpUtil.requestAll(ids, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject jsonObject =new JSONObject(response.body().string());
                                JSONArray jsonArray=jsonObject.getJSONObject("playlist").getJSONArray("tracks");
                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject t=jsonArray.getJSONObject(i);
                                    MusicFind musicFind=new MusicFind();
                                    musicFind.setId(t.get("id").toString());
                                    musicFind.setSongname(t.getString("name"));
                                    musicFind.setPosition(songs.size());
                                    musicFind.setAlbumpic_big(t.getJSONObject("al").getString("picUrl"));
                                    musicFind.setAlbumpic_small(t.getJSONObject("al").getString("picUrl"));
                                    musicFind.setSingername(t.getJSONArray("ar").getJSONObject(0).getString("name"));
                                    songs.add(musicFind);
                                }
                                MusicFindUtil.getInstance().add(songs);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            latch.countDown();
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                musicFenAdapter = new MusicFenAdapter(songs, getBaseContext()) {
                    @Override
                    protected void getItemView(View itemView,final MusicFind musicFind) {

                    }
                };
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvFenleiList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        rvFenleiList.setItemAnimator(new DefaultItemAnimator());
                        rvFenleiList.setAdapter(musicFenAdapter);
                        dissmiss();
                    }
                });

            }
        });
        if (musicFenAdapter!=null){
            musicFenAdapter.setPlay();
        }

        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.communication.CHANGE");
        intentFilter.addAction("com.example.communication.LISTCHANGE");
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(msgReceiver, intentFilter);
    }
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                netState = NetStateUtil.getNetWorkState(context);}
                if (musicFenAdapter!=null){
                    musicFenAdapter.setPlay();
                }
            }
       
        }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicFenAdapter!=null){
            musicFenAdapter.setPlay();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(msgReceiver);
    }
}
