package com.yitu.txwl.service.device;

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
    void updateDeviceSubjectData();
}
