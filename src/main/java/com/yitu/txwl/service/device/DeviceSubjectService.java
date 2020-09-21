package com.yitu.txwl.service.device;

import com.yitu.txwl.entity.AreaDeviceSubject;
import com.yitu.txwl.entity.DeviceSubject;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 摄像头实时人数接口
 *
 * @author WJ
 * @date   2020-09-18 14:14:50
 */
public interface DeviceSubjectService {

    /**
     * 展示排名前4入口数据
     *
     * @author WJ
     * @date   2020-09-18 14:17:07
     */
    LinkedHashMap<String, List<DeviceSubject>> listTop4CenterStatistics();

    /**
     * 更新当前时间段摄像头人数并缓存
     *
     * @author WJ
     * @date   2020-09-21 10:21:18
     */
    void updateDeviceSubjectData();

    /**
     * 展示所有展馆区域的人数
     *
     * @author WJ
     * @date   2020-09-21 15:58:59
     */
    LinkedHashMap<String, List<AreaDeviceSubject>> listAllAreaStatistics(String areaId);
}
