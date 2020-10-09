package com.yitu.txwl.service.mask;

import com.yitu.txwl.pojo.CenterMaskPojo;
import com.yitu.txwl.pojo.CenterMaskSearch;

import java.util.List;

/**
 * 国展中心口罩指数
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-22 11:28
 */
public interface CenterMaskService {

    /**
     * 5分钟执行一次
     * 定时获取FetchTrack工具下的meta数据
     *
     * @author WJ
     * @date   2020-09-22 12:12:33
     */
    void execFetchTrackMeta();

    List<CenterMaskPojo> getCenterMaskData(CenterMaskSearch search);
}
