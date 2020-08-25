package com.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.music.R;
import com.music.activity.FindActivity;
import com.music.activity.NetMusicActivity;
import com.music.bean.MusicFind;
import com.music.util.HttpUtil;
import com.music.util.MusicFindUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public abstract class FindMusicAdapter extends RecyclerView.Adapter<FindMusicAdapter.FindMusicViewHolder> {
    public final static String TAG = "FindMusicAdapter";
    List<MusicFind> musicFinds;
    Context context;

    public FindMusicAdapter(List<MusicFind> musicFinds, Context context) {
        this.musicFinds = musicFinds;
        this.context = context;

    }

    @Override
    public FindMusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FindMusicViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_music, parent, false));
    }

    public void addData(List<MusicFind> list) {
        for (MusicFind musicFind : list) {
            musicFinds.add(musicFind);
        }
    }

    public void addDataChange() {
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public void onBindViewHolder(FindMusicViewHolder holder, int position) {
        holder.load(musicFinds.get(position), context);
    }

    public void setPlay() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return musicFinds == null ? 0 : musicFinds.size();
    }

    public class FindMusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvFenleiSong;
        TextView tvFenleiSinger;

        public FindMusicViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvFenleiSong = itemView.findViewById(R.id.tv_fenlei_song);
            tvFenleiSinger = itemView.findViewById(R.id.tv_fenlei_singer);
        }

        public void load(final MusicFind musicFind, final Context context) {
            tvFenleiSinger.setText(musicFind.getSingername());
            tvFenleiSong.setText(musicFind.getSongname());
            if (musicFind.getId().equals(MusicFindUtil.getInstance().getID())) {
                tvFenleiSinger.setTextColor(Color.parseColor("#da3318"));
                tvFenleiSong.setTextColor(Color.parseColor("#da3318"));
            } else {
                tvFenleiSinger.setTextColor(Color.parseColor("#959595"));
                tvFenleiSong.setTextColor(Color.parseColor("#000000"));
            }
            Glide.with(context)
                    .load(musicFind.getAlbumpic_small())
                    .crossFade()
                    .into(ivCover);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //请求歌曲详细信息
                    HttpUtil.requestSongDetail(musicFind.getId(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "请求详细失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String str = response.body().string();
                                JSONObject jsonObject = new JSONObject(str);
                                JSONObject song = jsonObject.getJSONArray("songs").getJSONObject(0);
                                String pic = song.getJSONObject("al").getString("picUrl");
                                musicFind.setAlbumpic_big(pic);
                                musicFind.setAlbumpic_small(pic);
                                //获取歌曲播放URL
                                HttpUtil.requestSongUrl(musicFind.getId(), new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.e(TAG, "请求URL失败");
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            String str = response.body().string();
                                            JSONObject json = new JSONObject(str);
                                            String url = json.getJSONArray("data").getJSONObject(0).getString("url");
                                            Log.e(TAG,"url=="+url);
                                            musicFind.setUrl(url);
                                            musicFind.setDownUrl(url);
                                            Log.e(TAG, "url=====" + url);
                                            MusicFindUtil.getInstance().setMusic(musicFind);
                                        } catch (Exception e) {

                                        }
                                        //网络允许，活动跳转
                                        Intent intent = new Intent(context, NetMusicActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("songNetInfo", musicFind);
                                        intent.putExtras(bundle);
                                        context.startActivity(intent);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            });

        }

    }


    protected abstract void getItemView(View itemView, MusicFind musicFind);


}


