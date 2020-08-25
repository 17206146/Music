package com.music.activity.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.music.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class CoverFragment extends Fragment {
    @Bind(R.id.iv_cover)
    ImageView ivCover;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_play_page_cover, container, false);
        ButterKnife.bind(this, view1);
        return view1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
