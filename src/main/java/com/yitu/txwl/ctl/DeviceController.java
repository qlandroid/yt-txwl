package com.yitu.txwl.ctl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.entity.DeviceSubject;
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
import java.util.HashMap;
import java.util.List;

/**
 * @author WJ
 * @version 1.0.0
 * @className SampleController
 * @description
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
    private RestTemplate restTemplate;

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

    @GetMapping("/get")
    public List<DeviceSubject> get() {
        Query query = new Query();
        Criteria criteria = new Criteria();
        /*Criteria.where("face_subject_num").is(id);*/
        // 获取排名前4入口人数
        query.addCriteria(criteria)
                .limit(4)
                .with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            System.out.println(e);
        });
        return list;
    }

    @GetMapping("/encoded2")
    public String encoded2() {
        String id = "tAYAAAA=";
        String url = "http://10.40.51.33:9700/opod/v2/debug?type=CameraId&encoded_id=" + id;
        System.out.println(url);
        log.info("地址------------------------------------------" + url);
        JSONObject object = restTemplate.getForObject(URI.create(url), JSONObject.class);
        return object.toJSONString();
    }

}
