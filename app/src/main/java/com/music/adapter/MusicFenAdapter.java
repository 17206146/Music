package com.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
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


public abstract class MusicFenAdapter extends RecyclerView.Adapter<MusicFenAdapter.MusicFenViewHolder> {
    List<MusicFind> musicFinds;
    Context context;

    public MusicFenAdapter(List<MusicFind> list,Context context) {
        this.musicFinds = list;
        this.context = context;
    }

    @Override
    public MusicFenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new MusicFenViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itme_fenlei_list, parent, false));
    }

    @Override
    public void onBindViewHolder(MusicFenViewHolder holder, int position) {
     holder.load(musicFinds.get(position));
    }
    public void setPlay(){
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return musicFinds==null?0:musicFinds.size();
    }

    public class MusicFenViewHolder extends RecyclerView.ViewHolder {
        TextView tvFenleiSong;
        TextView tvFenleiSinger;

        public MusicFenViewHolder(View itemView) {
            super(itemView);
            tvFenleiSinger = itemView.findViewById(R.id.tv_fenlei_singer);
            tvFenleiSong = itemView.findViewById(R.id.tv_fenlei_song);
        }
        public void load (final MusicFind musicFind){
            tvFenleiSinger.setText(musicFind.getSingername());
            tvFenleiSong.setText(musicFind.getSongname());
            if (musicFind.getId().equals(MusicFindUtil.getInstance().getID())){
                tvFenleiSinger.setTextColor(Color.parseColor("#da3318"));
                tvFenleiSong.setTextColor(Color.parseColor("#da3318"));
            }else
            {
                tvFenleiSinger.setTextColor(Color.parseColor("#959595"));
                tvFenleiSong.setTextColor(Color.parseColor("#000000"));
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //获取歌曲播放URL
                    HttpUtil.requestSongUrl(musicFind.getId(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("66666666", "请求URL失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String re=response.body().string();
                            Log.e("666",re);
                            try {
                                JSONObject json = new JSONObject(re);
                                String url = json.getJSONArray("data").getJSONObject(0).getString("url");
                                musicFind.setUrl(url);
                                musicFind.setDownUrl(url);
                                Log.e("6666", "url=====" + url);
                                MusicFindUtil.getInstance().setMusic(musicFind);
                            } catch (Exception e) {
                                e.printStackTrace();
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

                }
            });

        }
    }

    protected abstract void getItemView(View itemView,final MusicFind musicFind);
}
