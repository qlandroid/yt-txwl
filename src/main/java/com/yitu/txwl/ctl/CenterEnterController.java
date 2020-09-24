package com.yitu.txwl.ctl;

import com.yitu.txwl.entity.AreaDeviceSubject;
import com.yitu.txwl.pojo.CenterEnterPojo;
import com.yitu.txwl.service.device.DeviceSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 会展中心摄像头数据(模块3)
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-17 17:51
 */
@RestController
@RequestMapping("/device")
public class CenterEnterController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DeviceSubjectService deviceSubjectService;

    /**
     *  会展中心人数前四数据
     *  20200923 修改传回所有数据而不止前4数据
     *
     * @author WJ
     * @date   2020-09-19 22:25:05
     */
    @GetMapping("/centerStatistics")
    public LinkedList<CenterEnterPojo> listTop4CenterStatistics() {
        return deviceSubjectService.listTop4CenterStatistics();
    }

    /**
     *  会展中心所有区域汇总数据
     *
     * @author WJ
     * @date   2020-09-19 22:25:05
     */
    @GetMapping("/allAreaStatistics")
    public HashMap<String, List<AreaDeviceSubject>> listAllAreaStatistics(String areaId) {
        return deviceSubjectService.listAllAreaStatistics(areaId);
    }

    @GetMapping("/update")
    public void testUpdate() {
        deviceSubjectService.updateDeviceSubjectData();
    }

}
