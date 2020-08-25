package com.music.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.music.R;
import com.music.adapter.FindMusicAdapter;
import com.music.bean.MusicFind;
import com.music.util.HttpUtil;
import com.music.util.MusicFindUtil;
import com.music.util.NetStateUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;


import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FindActivity extends AppCompatActivity {
    public final static String TAG="FindActivity";
    @Bind(R.id.iv_back)
    ImageView ivBack;
    @Bind(R.id.tb_main)
    Toolbar tbMain;
    @Bind(R.id.rv_net_song)
    RecyclerView rvNetSong;
    @Bind(R.id.sr_fragment_net)
    SmartRefreshLayout srFragmentNet;
    private int page = 1;
    private int totalPage;
    private String name;
    FindMusicAdapter findMusicAdapter;
    private Dialog progressDialog;//对话框
    MsgReceiver msgReceiver;
    private int netState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        page = 1;
        name = getIntent().getStringExtra("findName");
        MusicFindUtil.getInstance().deleteDate();
        showProgressDialog();//显示加载弹框
        netState = NetStateUtil.getNetWorkState(getBaseContext());//1:只数据 2：都可或只wifi 0：均不可
        //请求数据
        HttpUtil.requestSongData(name, 1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"failed");
                dissmiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                totalPage = MusicFindUtil.getInstance().getPage();
                findMusicAdapter = new FindMusicAdapter(MusicFindUtil.parseFindJOSNWithGSON(response), getBaseContext()) {
                    @Override
                    protected void getItemView(View itemView, final MusicFind musicFind) {

                    }
                };
                ;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvNetSong.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        rvNetSong.setItemAnimator(new DefaultItemAnimator());
                        rvNetSong.setAdapter(findMusicAdapter);
                        initView();
                        srFragmentNet.setRefreshHeader(new ClassicsHeader(getBaseContext()));
                    }
                });
                dissmiss();
            }
        });
        ButterKnife.bind(this);
        if (findMusicAdapter != null) {
            findMusicAdapter.setPlay();
        }
        initView();
        initData();
    }

    //显示加载弹框
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new Dialog(this, R.style.Pro);
            progressDialog.setContentView(R.layout.dialog_loading);//图片显示加载中
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//设置弹框透明度
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);//文本显示
            msg.setText("加载中~");
            progressDialog.show();
        }
        progressDialog.show();
    }

    public void dissmiss() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    public void initData() {
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
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                netState = NetStateUtil.getNetWorkState(context);
            }
            if (findMusicAdapter != null) {
                findMusicAdapter.setPlay();
            }
        }
    }

    @Override
    protected void onResume() {
        if (findMusicAdapter != null) {
            findMusicAdapter.setPlay();
        }
        super.onResume();
    }

    public void initView() {
        srFragmentNet.setOnRefreshLoadmoreListener(new OnRefreshLoadmoreListener() {
            @Override
            public void onLoadmore(final RefreshLayout refreshlayout) {

                MusicFindUtil.getInstance().getPage();
                Log.i("totalPage2", "totalPage " + totalPage);
                page++;
                if (page <= totalPage) {
                    HttpUtil.requestSongData(name, page, new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            refreshlayout.finishLoadmore();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            refreshlayout.finishLoadmore();
                            findMusicAdapter.addData(MusicFindUtil.parseFindJOSNWithGSON(response));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findMusicAdapter.addDataChange();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getBaseContext(), "There is no more~~", Toast.LENGTH_SHORT).show();
                    refreshlayout.finishLoadmore();
                }
            }

            @Override
            public void onRefresh(final RefreshLayout refreshlayout) {
                HttpUtil.requestSongData(name, 1, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        refreshlayout.finishRefresh();
                        Log.i("TAG", "onCreate:2 " + e);
                        dissmiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        refreshlayout.finishRefresh();
                        findMusicAdapter = new FindMusicAdapter(MusicFindUtil.parseFindJOSNWithGSON(response), getBaseContext()) {
                            @Override
                            protected void getItemView(View itemView, final MusicFind musicFind) {
                                itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        SharedPreferences preferences = getSharedPreferences("setting", MODE_PRIVATE);
                                        if (netState == 2 || (netState == 1 && preferences.getBoolean("net", true))) {
                                            Intent intent = new Intent(FindActivity.this, NetMusicActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("songNetInfo", musicFind);
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(FindActivity.this, "Please check the network status(；′⌒`)", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }


                        };
                        Log.i("TAG2", "onCreate:2 ");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("TAG2", "onCreate:3 ");
                                rvNetSong.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                rvNetSong.setItemAnimator(new DefaultItemAnimator());
                                rvNetSong.setAdapter(findMusicAdapter);
                            }
                        });

                    }
                });
            }
        });
    }

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        finish();
    }
}
