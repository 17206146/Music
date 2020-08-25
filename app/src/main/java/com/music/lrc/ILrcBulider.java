package com.music.lrc;

import java.util.List;



public interface ILrcBulider {
    List<LrcRow> getLrcRows(String rawLrc);
}
