package com.yitu.txwl.service.device;

import com.yitu.txwl.pojo.CenterEnterPojo;

import java.util.LinkedList;

/**
 * 国展中心入展时间统计
 *
 * @author WJ
 * @date   2020-09-24 20:16:46
 */
public interface CenterEnterService {

    /**
     * 展示所有入口数据
     *
     * @author WJ
     * @date   2020-09-24 20:17:03
     */
    LinkedList<CenterEnterPojo> listAllDevice();

    /**
     * 每15分钟更新一次数据
     *
     * @author WJ
     * @date   2020-09-24 21:26:48
     */
    void updateDeviceSubjectData();
}
