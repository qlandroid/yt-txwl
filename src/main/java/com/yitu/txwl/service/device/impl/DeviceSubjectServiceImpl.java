package com.yitu.txwl.service.device.impl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.core.util.RedisUtil;
import com.yitu.txwl.entity.DeviceSubject;
import com.yitu.txwl.entity.OpodDevices;
import com.yitu.txwl.service.device.DeviceSubjectService;
import com.yitu.txwl.service.ext.OpodDecOrEncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 摄像头实时人数接口
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-18 14:15
 */
@Service
public class DeviceSubjectServiceImpl implements DeviceSubjectService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongotemplate;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OpodDecOrEncService opodDecOrEncService;

    @Override
    public LinkedHashMap<String, List<DeviceSubject>> listTop4CenterStatistics() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        List<DeviceSubject> list;
        // 获取排名前4人数摄像头
        list = listTop4Device();
        // 根据4个摄像头获取摄像头的前5个纵轴的数据
        return listFiveVerticalData(hour, list);
    }

    /**
     * 从mongo中获取排名前4人数摄像头
     */
    private List<DeviceSubject> listTop4Device() {
        int hour = LocalDateTime.now().getHour();
        Query query = new Query();
        Criteria criteria = new Criteria();
        // 获取排名前4入口人数
        query.addCriteria(criteria)
                .limit(4)
                .with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            OpodDevices opodDevices = getOpodByDeviceId(e.getDeviceId());
            e.setName(opodDevices.getName());
            // 根据总抓拍人数计算当前小时抓拍人数
            calcDeviceHourNum(hour, e);
        });
        return list;
    }

    /**
     * 获取指定摄像头当前时间的前5个纵轴数据
     */
    private LinkedHashMap<String, List<DeviceSubject>> listFiveVerticalData(int hour, List<DeviceSubject> list) {
        LinkedHashMap<String, List<DeviceSubject>> dataMap = new LinkedHashMap<>(8);
        String spiltStr = "-";
        // 根据当前时间获取纵轴时间点
        String[] verticalAxis = getDataByHour(hour);
        // 组装数据
        AtomicInteger i = new AtomicInteger();
        Arrays.stream(verticalAxis).forEach(x -> {
            if (i.get() == (verticalAxis.length - 1)) {
                dataMap.put(x, list);
            } else {
                List<DeviceSubject> dataList = new ArrayList<>();
                list.forEach(e -> {
                    String key = x.contains(spiltStr) ? x.split("-")[1] : x;
                    DeviceSubject deviceSubject = (DeviceSubject) redisUtil.get(key + "_" + e.getId());
                    dataList.add(deviceSubject);
                });
                dataMap.put(x, dataList);
            }
            i.getAndIncrement();
        });

        return dataMap;
    }

    /**
     * 每隔5分钟执行一次, 更新当前时间段摄像头人数并缓存
     *
     * @author WJ
     * @date 2020-09-18 14:48:04
     */
    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void updateDeviceSubjectData() {
        LocalDateTime now = LocalDateTime.now();
        int key = now.getHour();
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            OpodDevices opodDevices = getOpodByDeviceId(e.getDeviceId());
            e.setName(opodDevices.getName());
            log.info("更新OpodDevices---> {}", opodDevices);
            // redisUtil.zSSet("HOUR" + key, e, e.getFaceSubjectNum().doubleValue());

            // 根据总抓拍人数计算当前小时抓拍人数
            calcDeviceHourNum(key, e);

            // 缓存当前摄像头人数，key:当前小时数_摄像头ObjectId
            redisUtil.set(key + "_" + e.getId(), e);
        });
    }

    /** 根据总抓拍人数计算当前小时抓拍人数 */
    private void calcDeviceHourNum(int hour, DeviceSubject subject) {
        // 如果当前时间为0点之后，则当前人数为总人数减去上一小时的总人数
        if (hour > 0) {
            int lastHour = hour - 1;
            DeviceSubject sub = (DeviceSubject)redisUtil.get(lastHour + "_" + subject.getId());
            if (null != sub) {
                int num = subject.getFaceSubjectNum() - sub.getFaceSubjectNum();
                subject.setFaceHourNum(Math.max(num, 0));
            } else {
                subject.setFaceHourNum(subject.getFaceSubjectNum());
            }
        } else {
            // 0点的当前人数则为总抓拍人数
            subject.setFaceHourNum(subject.getFaceSubjectNum());
        }
    }

    /**
     * 获取摄像头数据
     * 并缓存摄像头加密解密数据
     */
    private OpodDevices getOpodByDeviceId(String deviceId) {
        OpodDevices opodDevices = null;
        if (redisUtil.hasKey(deviceId)) {
            // 从redis中获取解密后摄像头
            opodDevices = (OpodDevices) redisUtil.get(deviceId);
        } else {
            // 从接口处获取解密后摄像头
            JSONObject object = opodDecOrEncService.decodedDeviceId(deviceId);
            if (null != object) {
                if (object.containsKey("decoded_id")) {
                    String id = (String) object.get("decoded_id");
                    // mongodb获取摄像头数据
                    Query query = new Query();
                    Criteria criteria = Criteria.where("id").is(id);
                    query.addCriteria(criteria);
                    opodDevices = mongotemplate.findOne(query, OpodDevices.class);
                    // 缓存摄像头加密及解密数据
                    if (null != opodDevices) {
                        redisUtil.set(deviceId, opodDevices);
                        redisUtil.set(opodDevices.getId(), deviceId);
                    }
                }
            }
        }
        return opodDevices;
    }

    /**
     * 根据当前时间获取纵轴时间点
     * 12点之前纵轴间隔1小时，12点后纵轴间隔2小时
     */
    private String[] getDataByHour(int hour) {
        // 1 2 3 4 5 6 7 8 9 10 11
        // 12-14 14-16 16-18 18-20 20-22 22-24
        String[] verticalAxis = new String[6];
        switch (hour) {
            case 0:
                verticalAxis = new String[]{"14-16", "16-18", "18-20", "20-22", "22-24", "1"};
                break;
            case 1:
                verticalAxis = new String[]{"16-18", "18-20", "20-22", "22-24", "1", "2"};
                break;
            case 2:
                verticalAxis = new String[]{"18-20", "20-22", "22-24", "1", "2", "3"};
                break;
            case 3:
                verticalAxis = new String[]{"20-22", "22-24", "1", "2", "3", "4"};
                break;
            case 4:
                verticalAxis = new String[]{"22-24", "1", "2", "3", "4", "5"};
                break;
            case 5:
                verticalAxis = new String[]{"1", "2", "3", "4", "5", "6"};
                break;
            case 6:
                verticalAxis = new String[]{"2", "3", "4", "5", "6", "7"};
                break;
            case 7:
                verticalAxis = new String[]{"3", "4", "5", "6", "7", "8"};
                break;
            case 8:
                verticalAxis = new String[]{"4", "5", "6", "7", "8", "9"};
                break;
            case 9:
                verticalAxis = new String[]{"5", "6", "7", "8", "9", "10"};
                break;
            case 10:
                verticalAxis = new String[]{"6", "7", "8", "9", "10", "11"};
                break;
            case 11:
            case 12:
            case 13:
                verticalAxis = new String[]{"7", "8", "9", "10", "11", "12-14"};
                break;
            case 14:
            case 15:
                verticalAxis = new String[]{"8", "9", "10", "11", "12-14", "14-16"};
                break;
            case 16:
            case 17:
                verticalAxis = new String[]{"9", "10", "11", "12-14", "14-16", "16-18"};
                break;
            case 18:
            case 19:
                verticalAxis = new String[]{"10", "11", "12-14", "14-16", "16-18", "18-20"};
                break;
            case 20:
            case 21:
                verticalAxis = new String[]{"11", "12-14", "14-16", "16-18", "18-20", "20-22"};
                break;
            case 22:
            case 23:
                verticalAxis = new String[]{"12-14", "14-16", "16-18", "18-20", "20-22", "22-24"};
                break;
            default:
                break;
        }

        return verticalAxis;
    }
}
