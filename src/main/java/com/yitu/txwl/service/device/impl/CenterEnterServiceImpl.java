package com.yitu.txwl.service.device.impl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.entity.CenterEnter;
import com.yitu.txwl.entity.DeviceSubject;
import com.yitu.txwl.entity.OpodDevices;
import com.yitu.txwl.pojo.CenterEnterPojo;
import com.yitu.txwl.pojo.Statistic;
import com.yitu.txwl.service.device.CenterEnterService;
import com.yitu.txwl.service.ext.OpodDecOrEncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WJ
 * @version 1.0.0
 * @className CenterServiceImpl
 * @description
 * @date 2020-09-24 20:16
 */
@Service
public class CenterEnterServiceImpl implements CenterEnterService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongotemplate;
    @Autowired
    private OpodDecOrEncService opodDecOrEncService;

    /**
     * 从mongo中获取所有摄像头
     */
    @Override
    public LinkedList<CenterEnterPojo> listAllDevice() {
        Query query = new Query();
        Criteria criteria = new Criteria();
        // 获取所有入口人数
        query.addCriteria(criteria)
                .with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);

        LinkedList<CenterEnterPojo> pojoList = new LinkedList<>();
        AtomicInteger count = new AtomicInteger(1);
        list.forEach(e -> {
            // 根据摄像头数据获取摄像头历史数据
            CenterEnter centerEnter = getCenterEnterByDeviceId(e);
            // 组装需要返回的数据
            CenterEnterPojo pojo = new CenterEnterPojo();
            List<Statistic> dataList = new ArrayList<>();
            centerEnter.getFaceNum().forEach(faceNum -> {
                Statistic statistic = new Statistic();
                statistic.setFace_num(faceNum);
                dataList.add(statistic);
            });
            pojo.setAreaId(count.getAndIncrement());
            pojo.setAreaName(centerEnter.getName());
            pojo.setStatistic(dataList);
            pojoList.add(pojo);
        });
        return pojoList;
    }

    /**
     * 获取摄像头数据
     * 并缓存摄像头加密解密数据
     */
    private CenterEnter getCenterEnterByDeviceId(DeviceSubject subject) {
        OpodDevices opodDevices;
        Query query = new Query();
        // 根据摄像头加密ID查询数据
        Criteria criteria = Criteria.where("device_id").is(subject.getDeviceId());
        query.addCriteria(criteria);
        CenterEnter centerEnter = mongotemplate.findOne(query, CenterEnter.class);
        if (centerEnter == null) {
            // 从接口处获取解密后摄像头
            JSONObject object = opodDecOrEncService.decodedDeviceId(subject.getDeviceId());
            if (null != object) {
                if (object.containsKey("decoded_id")) {
                    String id = (String) object.get("decoded_id");
                    // mongodb获取摄像头数据
                    Query opodQuery = new Query();
                    Criteria opodCriteria = Criteria.where("id").is(id);
                    opodQuery.addCriteria(opodCriteria);
                    opodDevices = mongotemplate.findOne(opodQuery, OpodDevices.class);
                    // 缓存摄像头加密及解密数据
                    if (null != opodDevices) {
                        // 组装摄像头数据
                        centerEnter = new CenterEnter();
                        centerEnter.setId(id);
                        centerEnter.setDeviceId(subject.getDeviceId());
                        centerEnter.setName(opodDevices.getName());
                        centerEnter.setTotalNum(subject.getFaceSubjectNum());
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(subject.getFaceSubjectNum());
                        centerEnter.setFaceNum(list);

                        // 创建表及插入缓存数据
                        if (!mongotemplate.collectionExists(CenterEnter.class)) {
                            mongotemplate.createCollection(CenterEnter.class);
                        }
                        mongotemplate.insert(centerEnter);
                    }
                }
            }
        } else {
            // 获取当前时间段记录人数= mongo中记录总人数 - 缓存的历史总人数
            int num = subject.getFaceSubjectNum() - centerEnter.getTotalNum();
            List<Integer> historyList = centerEnter.getFaceNum();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
            historyList.add(num);
            centerEnter.setFaceNum(historyList);
        }
        return centerEnter;
    }


    /**
     * 每隔5分钟执行一次, 更新当前时间段摄像头人数并缓存
     * 早上5点到晚上23点每15分钟一条记录
     *
     * @author WJ
     * @date 2020-09-18 14:48:04
     */
    @Override
    @Scheduled(cron = "0 0/15 6-23 * * ?")
    public void updateDeviceSubjectData() {
        // 创建表
        if (!mongotemplate.collectionExists(CenterEnter.class)) {
            mongotemplate.createCollection(CenterEnter.class);
        }

        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            CenterEnter centerEnter = getCenterEnterByDeviceId(e);
            // 缓存数据
           Query updateQuery = new Query();
            // 根据摄像头加密ID查询数据
            Criteria criteria = Criteria.where("device_id").is(centerEnter.getDeviceId());
            updateQuery.addCriteria(criteria);
            // 更新的字段
            Update update = new Update();
            update.set("face_num", centerEnter.getFaceNum());
            mongotemplate.upsert(updateQuery, update, CenterEnter.class);
        });
    }

    /**
     * 每天凌晨1点清空缓存数据
     *
     * @author WJ
     * @date   2020-09-25 10:36:36
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteDeviceSubjectData() {
        List<CenterEnter> list = mongotemplate.findAll(CenterEnter.class);
        if (null != list && !list.isEmpty()) {
            list.forEach(e -> {
                Query updateQuery = new Query();
                Criteria criteria = Criteria.where("device_id").is(e.getDeviceId());
                updateQuery.addCriteria(criteria);
                // 清空缓存的历史记录人数以及总记录人数
                Update update = new Update();
                update.set("totalNum", 0);
                update.set("face_num", new ArrayList<>());
                mongotemplate.upsert(updateQuery, update, CenterEnter.class);
            });
        }
    }

}
