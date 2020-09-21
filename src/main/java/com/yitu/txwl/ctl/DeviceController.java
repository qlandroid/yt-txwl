package com.yitu.txwl.ctl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.core.util.RedisUtil;
import com.yitu.txwl.entity.DeviceSubject;
import com.yitu.txwl.entity.OpodDevices;
import com.yitu.txwl.service.device.DeviceSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * 会展中心摄像头数据
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-17 17:51
 */
@RestController
@RequestMapping("/device")
public class DeviceController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongotemplate;
    @Autowired
    private DeviceSubjectService deviceSubjectService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     *  会展中心
     *
     * @author WJ
     * @date   2020-09-19 22:25:05
     */
    @GetMapping("/centerStatistics")
    public HashMap<String, List<DeviceSubject>> listTop4CenterStatistics() {
        return deviceSubjectService.listTop4CenterStatistics();
    }

    @GetMapping("/update")
    public void testUpdate() {
        deviceSubjectService.updateDeviceSubjectData();
    }

    @GetMapping("/set")
    public void testSet(int hour) {
        int key = hour;
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            // 缓存当前摄像头人数，key:当前小时数_摄像头ObjectId
            // redisUtil.zSSet("HOUR" + key, e, e.getFaceSubjectNum().doubleValue());
            redisUtil.set(key + "_" + e.getId(), e);
        });
    }

}
