package com.music.util;

import android.media.MediaPlayer;
import android.util.Log;

import com.music.bean.MusicFind;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Response;


public class MusicFindUtil {
    public final static String TAG = "MusicFindUtil";
    private int preSongPosition = -1;
    private int currentSongPosition = -1;
    private boolean isPrepare = false;
    private static MediaPlayer mediaPlayer = MusicUtil.getInstance().getMediaPlayer();
    private static List<MusicFind> list2;
    private static List<MusicFind> list3;
    private static List<MusicFind> totalList;
    private static int totalPage;
    private static String allPages;
    private static MusicFindUtil musicFindUtil;
    /**
     * 播放模式
     */
    public String playID;
    private int pattern = TYPE_ORDER;
    /**
     * 顺序播放
     */
    public static final int TYPE_ORDER = 4212;
    /**
     * 随机播放
     */
    public static final int TYPE_RANDOM = 4313;
    /**
     * 单曲循环
     */
    public static final int TYPE_SINGLE = 4414;

    public int getPattern() {
        return pattern;
    }

    public MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        return mediaPlayer;

    }

    //播放下一首
    public void next() {
        preSongPosition = currentSongPosition;
        if (pattern == TYPE_ORDER || pattern == TYPE_SINGLE) {
            if (currentSongPosition == list2.size() - 1) {
                currentSongPosition = 0;
            } else {
                currentSongPosition = currentSongPosition + 1;
            }
        }
        if (pattern == TYPE_RANDOM) {
            currentSongPosition = new Random().nextInt(list2.size() - 1);
        }
    }

    //获取当前歌曲的位置
    public MusicFind getNewSongInfo() {
        if (list2 != null)
            return list2.get(currentSongPosition);
        return null;
    }

    //释放资源
    public void clean() {
        mediaPlayer.stop();
        mediaPlayer.release();

    }

    //获取歌曲
    public MusicFind getMusic() {
        if (list2 == null) {
            list2 = new ArrayList<>();
        }
        return list2.get(currentSongPosition);
    }

    //播放上一首
    public void pre() {
        preSongPosition = currentSongPosition;
        if (pattern == TYPE_ORDER || pattern == TYPE_SINGLE) {
            preSongPosition = currentSongPosition;
            if (currentSongPosition == 0) {
                currentSongPosition = list2.size() - 1;
            } else {
                currentSongPosition = currentSongPosition - 1;
            }
        }
        if (pattern == TYPE_RANDOM) {
            currentSongPosition = new Random().nextInt(list2.size() - 1);
        }
    }

    //设置播放模式
    public void setPatten(int i) {
        if (i == TYPE_SINGLE) {
            this.pattern = TYPE_SINGLE;
        }
        if (i == TYPE_ORDER) {
            this.pattern = TYPE_ORDER;
        }
        if (i == TYPE_RANDOM) {
            this.pattern = TYPE_RANDOM;
        }
    }

    public synchronized static MusicFindUtil getInstance() {
        if (musicFindUtil == null) {
            musicFindUtil = new MusicFindUtil();
        }
        return musicFindUtil;
    }

    public String getID() {
        return playID;
    }

    public void setID(String id) {
        playID = id;
    }

    public int getPage() {
        try {
            totalPage = Integer.parseInt(allPages);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return totalPage;
    }

    //返回当前位置
    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    //下一首
    public void comNext() {
        preSongPosition = currentSongPosition;
        if (pattern == TYPE_SINGLE) {
            return;
        } else if (pattern == TYPE_ORDER) {
            if (currentSongPosition == MusicUtil.getInstance().getListCount() - 1) {
                currentSongPosition = 0;
            } else {
                currentSongPosition = currentSongPosition + 1;
            }
        } else if (pattern == TYPE_RANDOM) {
            currentSongPosition = new Random().nextInt(MusicUtil.getInstance().getListCount() - 1);
        }
    }

    //解析查询返回的信息
    public static List<MusicFind> parseFindJOSNWithGSON(Response response) {
        list2 = new ArrayList<>();
        try {//JSONObject存储数据（key-value 结构）
            String responseData = response.body().string();//服务器返回的信息
            //Log.e(TAG,responseData);
            JSONObject jsonObject1 = new JSONObject(responseData);//
            JSONObject result = jsonObject1.getJSONObject("result");
            int songCount = result.getInt("songCount");
            allPages = (songCount % 20 == 0 ? songCount / 20 : songCount / 20 + 1) + "";

            JSONArray jsonArray = result.getJSONArray("songs");
            for (int i = 0; i < jsonArray.length(); i++) {
                MusicFind musicfind = new MusicFind();
                JSONObject jsonObject5 = jsonArray.getJSONObject(i);

                musicfind.setSongname(jsonObject5.getString("name"));
                musicfind.setSingername(jsonObject5.getJSONArray("artists").getJSONObject(0).getString("name"));
                // musicfind.setAlbumpic_small(jsonObject5.getJSONObject("album").getJSONObject("artist").getString("img1v1Url"));
                //   musicfind.setDownUrl(jsonObject5.getString("downUrl"));
                //  musicfind.setUrl(jsonObject5.getString("m4a"));
                musicfind.setId(jsonObject5.get("id").toString());
                musicfind.setPosition(i);
                list2.add(musicfind);
                totalList.add(musicfind);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list2;
    }

    public void deleteDate() {
        totalList = new ArrayList<>();
    }

    public static List<MusicFind> parseJOSNWithGSON(Response response, boolean isRe) {
        list2 = new ArrayList<>();
        try {
            String ResponsData = response.body().string();
            JSONObject jsonObject = new JSONObject(ResponsData);
            String error = jsonObject.getString("showapi_res_error");
            String body = jsonObject.getString("showapi_res_body");
            JSONObject jsonObject1 = new JSONObject(body);
            String pagebean = jsonObject1.getString("pagebean");
            JSONObject jsonObject2 = new JSONObject(pagebean);
            String songlist = jsonObject2.getString("songlist");
            JSONArray jsonArray = new JSONArray(songlist);
            for (int i = 0; i < jsonArray.length(); i++) {
                MusicFind musicfind = new MusicFind();
                JSONObject jsonObject3 = jsonArray.getJSONObject(i);
                musicfind.setDownUrl(jsonObject3.getString("downUrl"));
                musicfind.setSingername(jsonObject3.getString("singername"));
                musicfind.setSongname(jsonObject3.getString("songname"));
                musicfind.setUrl(jsonObject3.getString("url"));
                musicfind.setId(jsonObject3.getString("songid"));
                musicfind.setPosition(i);
                if (isRe == false) {
                    list2.add(musicfind);
                } else {
                    if (list3 == null) {
                        list3 = new ArrayList<>();
                    }
                    list3.add(musicfind);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isRe == false) {
            return list2;
        } else {
            list2 = list3;
            return list3;
        }
    }

    public List<MusicFind> getListRe() {
        if (list3 == null) {
            list3 = new ArrayList<>();
        }
        list2 = list3;
        return list3;

    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void playOrPause() {
        if (mediaPlayer == null) {
            start();
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {

        if (mediaPlayer == null) {
            return false;
        }
        if (mediaPlayer.isPlaying()) {
            return true;
        } else
            return false;
    }

    public void start() {
        if (isPrepare) {
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MusicUtil.getInstance().getMediaPlayer();
        }
        try {
            mediaPlayer.reset();
            Log.e(TAG, "name=" + list2.get(currentSongPosition).getSingername() + ",url=" + list2.get(currentSongPosition).getUrl());
            mediaPlayer.setDataSource(list2.get(currentSongPosition).getUrl());
            isPrepare = true;
            mediaPlayer.prepare();
            mediaPlayer.start();
            playID = list2.get(currentSongPosition).getId();
            MusicUtil.getInstance().setPre(playID);
            isPrepare = false;
            if (currentSongPosition == -1) {
                preSongPosition = currentSongPosition;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void add(ArrayList<MusicFind> list) {
        list2 = new ArrayList<>();
        list2.addAll(list);
        totalList.addAll(list);
    }

    public void setMusic(MusicFind music) {
        for (MusicFind musicFind : list2) {
            if (musicFind.getId() == music.getId()) {
                musicFind.setDownUrl(music.getDownUrl());
                musicFind.setUrl(music.getUrl());
                musicFind.setAlbumpic_small(music.getAlbumpic_small());
                musicFind.setAlbumpic_big(music.getAlbumpic_big());
                break;
            }
        }
    }

    public void setCurrentSongPosition(int p) {
        this.currentSongPosition = p;
    }
}
