package com.music.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.music.R;
import com.music.activity.FindActivity;
import com.music.activity.MusicListActivity;
import com.music.adapter.MusicGridAdapter;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class NetFragment extends Fragment {
    @Bind(R.id.et_findlocal)//搜索栏
    EditText etFindlocal;
    @Bind(R.id.main_gridview)
    GridView mainGridview;
    private SparseArray<String> gridItems = new SparseArray<String>();

    private Map<Integer,String> maps = new HashMap<>();
    private Map<Integer,String> maps2 = new HashMap<>();
    MusicGridAdapter musicGridAdapter;
    Banner banner;
    private List<Integer> images;
    private List<String> strings;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container, false);

        banner =  view.findViewById(R.id.banner);
        initData();
        banner.setImageLoader(new GlideImagerLoader());
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        banner.setImages(images);
        banner.setBannerTitles(strings);
        banner.isAutoPlay(true);
        banner.setDelayTime(2000);
        banner.start();

        musicGridAdapter = new MusicGridAdapter(gridItems, getContext());
        mainGridview = view.findViewById(R.id.main_gridview);
        mainGridview.setAdapter(musicGridAdapter);
        mainGridview.setOnItemClickListener(new mainGridviewListener());
        ButterKnife.bind(this, view);
        return view;
    }

    private void initData() {
        gridItems.put(0, "Light");
        gridItems.put(1, "Cantonese");
        gridItems.put(2, getString(R.string.music_fenlei_Kge));
        gridItems.put(3, getString(R.string.music_fenlei_liuxing));
        gridItems.put(4, "Antiquity");
        gridItems.put(5, "Folk");
        gridItems.put(6, getString(R.string.music_fenlei_oumei));
        gridItems.put(7, "Sports");
        gridItems.put(8, "Classical");
        getFenlei();
        images = new ArrayList<>();
        strings = new ArrayList<>();
        images.add(R.drawable.one);
        images.add(R.drawable.two);
        images.add(R.drawable.three);
        images.add(R.drawable.four);
        strings.add("You make me feel so happy");
        strings.add("You make me feel so special");
        strings.add("This love is too good to be true");
        strings.add("Rosemary Anne Nash?");
    }

    //搜索点击事件
    @OnClick({R.id.iv_findNet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_findNet:
                Intent intent = new Intent(getActivity(), FindActivity.class);
                intent.putExtra("findName",etFindlocal.getText().toString());
                etFindlocal.setText("");
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


     //GirdView点击事件
    private class mainGridviewListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent(getContext(), MusicListActivity.class);
            intent.putExtra("musictype", maps.get(position));
            intent.putExtra("musictype2", maps2.get(position));
            startActivity(intent);
        }
    }
    public class  GlideImagerLoader extends ImageLoader{

        @Override
        public void displayImage(Context context, Object o, ImageView imageView) {
            Glide.with(context).load(o).into(imageView);
        }
    }
    public void getFenlei() {
        maps.put(0,"Light");
        maps.put(1,"Cantonese");
        maps.put(2,getString(R.string.music_fenlei_Kge));
        maps.put(3,getString(R.string.music_fenlei_liuxing));
        maps.put(4,"Antiquity");
        maps.put(5,"Folk");
        maps.put(6,getString(R.string.music_fenlei_oumei));
        maps.put(7,"Sports");
        maps.put(8,"Classical");

        maps2.put(0,"轻音乐");
        maps2.put(1,"粤语");
        maps2.put(2,"民谣");
        maps2.put(3,"流行");
        maps2.put(4,"古风");
        maps2.put(5,"民族");
        maps2.put(6,"欧美");
        maps2.put(7,"运动");
        maps2.put(8,"经典");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
