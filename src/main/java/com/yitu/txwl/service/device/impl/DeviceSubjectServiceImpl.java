package com.yitu.txwl.service.device.impl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.core.util.RedisUtil;
import com.yitu.txwl.entity.AreaDeviceSubject;
import com.yitu.txwl.entity.DeviceSubject;
import com.yitu.txwl.entity.OpodDevices;
import com.yitu.txwl.pojo.CenterEnterPojo;
import com.yitu.txwl.pojo.Statistic;
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
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
    public LinkedList<CenterEnterPojo> listTop4CenterStatistics() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        List<DeviceSubject> list;
        // 获取所有人数摄像头
        list = listTop4Device(hour, minute);
        // 根据摄像头获取摄像头的历史数据
        return listVerticalData(hour, minute, list);
    }

    @Override
    public LinkedHashMap<String, List<AreaDeviceSubject>> listAllAreaStatistics(String areaId) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        // 获取所有区域人数摄像头
        List<AreaDeviceSubject> list = listAllAreaDevice(areaId);
        return listAllAreaFiveVerticalData(hour, list);
    }

    /**
     * 从mongo中获取所有人数摄像头
     */
    private List<DeviceSubject> listTop4Device(int hour, int minute) {
        // int hour = LocalDateTime.now().getHour();
        String key = getKey(hour, minute);

        Query query = new Query();
        Criteria criteria = new Criteria();
        // 获取所有入口人数
        query.addCriteria(criteria)
                //.limit(4)
                .with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            OpodDevices opodDevices = getOpodByDeviceId(e.getDeviceId());
            e.setName(opodDevices.getName());
            // 根据总抓拍人数计算当前小时抓拍人数
            calcDeviceHourNum(key, e);
        });
        return list;
    }

    /**
     * 从mongo中获取所有区域摄像头人数
     */
    private List<AreaDeviceSubject> listAllAreaDevice(String areaId) {
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        String key = getKey(hour, minute);
        Query query = new Query();
        Criteria criteria = Criteria.where("date").is(LocalDate.now().toString());
        if (!StringUtils.isEmpty(areaId)) {
            // 如果传入区域ID则根据区域ID查询
            criteria.and("_id").is(areaId);
        }
        query.addCriteria(criteria)
                .with(Sort.by(Sort.Order.desc("area_id")));
        List<AreaDeviceSubject> list = mongotemplate.find(query, AreaDeviceSubject.class);
        list.forEach(e -> {
            // 根据总抓拍人数计算当前小时抓拍人数
            calcAreaDeviceHourNum(key, e);
        });
        return list;
    }

    /**
     * 获取指定摄像头数据
     * 12点之前返回6-12点数据，目前返回6-当前时间(可能大于12)
     * 12点之后返回6-18(待定)点数据，目前返回6-当前时间(可能大于18)
     */
    private LinkedList<CenterEnterPojo> listVerticalData(int hour, int minute, List<DeviceSubject> list) {
        LinkedList<CenterEnterPojo> returnList = new LinkedList<>();
        // 当前需要的最大数据组
        int maxMinute = Integer.parseInt(getKey(hour, minute).split("_")[1]);
        AtomicInteger count = new AtomicInteger(1);
        list.forEach(e -> {
            CenterEnterPojo pojo = new CenterEnterPojo();
            List<Statistic> dataList = new ArrayList<>();
            for (int i = 6; i <= hour; i++) {
                for (int j = 0; j < 4; j++) {
                    DeviceSubject deviceSubject;
                    // 根据当前小时的分钟数来计算需要查询的摄像头数据
                    if (i == hour) {
                        if (j > maxMinute) {
                            // 只返回6点到当前小时以及分钟所属组的数据
                            break;
                        }
                    }
                    String key = i + "_" + j;
                    if (i == hour && j == maxMinute) {
                        // 最新数据使用从mongo中获取的数据
                        deviceSubject = e;
                    } else {
                        // 历史数据从redis中获取
                        deviceSubject = (DeviceSubject) redisUtil.get(key + "_device_" + e.getId());
                    }
                    Statistic statistic = new Statistic();
                    if (null != deviceSubject) {
                        statistic.setFace_num(deviceSubject.getFaceHourNum());
                    }
                    dataList.add(statistic);
                }
            }
            // pojo.setAreaId(e.getAreaId());
            pojo.setAreaId(count.getAndIncrement());
            pojo.setAreaName(e.getName());
            pojo.setStatistic(dataList);
            returnList.add(pojo);
        });

        return returnList;
    }

    /**
     * 获取指定摄像头当前时间的前5个纵轴数据
     */
    @Deprecated
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
                    /* 假如是12-14，则查询13_device_id的缓存数据 */
                    String key = x.contains(spiltStr) ? String.valueOf((Integer.parseInt(x.split("-")[0]) + 1)) : x;
                    DeviceSubject deviceSubject = (DeviceSubject) redisUtil.get(key + "_device_" + e.getId());
                    dataList.add(deviceSubject);
                });
                dataMap.put(x, dataList);
            }
            i.getAndIncrement();
        });

        return dataMap;
    }

    /**
     * 获取所有区域当前时间的前5个纵轴数据
     */
    @Deprecated
    private LinkedHashMap<String, List<AreaDeviceSubject>> listAllAreaFiveVerticalData(int hour, List<AreaDeviceSubject> list) {
        LinkedHashMap<String, List<AreaDeviceSubject>> dataMap = new LinkedHashMap<>(8);
        String spiltStr = "-";
        // 根据当前时间获取纵轴时间点
        String[] verticalAxis = getDataByHour(hour);
        // 组装数据
        AtomicInteger i = new AtomicInteger();
        Arrays.stream(verticalAxis).forEach(x -> {
            if (i.get() == (verticalAxis.length - 1)) {
                dataMap.put(x, list);
            } else {
                List<AreaDeviceSubject> dataList = new ArrayList<>();
                list.forEach(e -> {
                    String key = x.contains(spiltStr) ? String.valueOf((Integer.parseInt(x.split("-")[0]) + 1)) : x;
                    AreaDeviceSubject deviceSubject = (AreaDeviceSubject) redisUtil.get(key + "_area_" + e.getId());
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
     * 每15分钟一条记录
     *
     * @author WJ
     * @date 2020-09-18 14:48:04
     */
    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void updateDeviceSubjectData() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        String key = getKey(hour, minute);
        // 单个摄像头
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("face_subject_num")));
        List<DeviceSubject> list = mongotemplate.find(query, DeviceSubject.class);
        list.forEach(e -> {
            OpodDevices opodDevices = getOpodByDeviceId(e.getDeviceId());
            e.setName(opodDevices.getName());
            /*redisUtil.zSSet("HOUR" + key, e, e.getFaceSubjectNum().doubleValue());*/

            // 根据总抓拍人数计算当前小时抓拍人数
            calcDeviceHourNum(key, e);

            // 缓存当前摄像头人数，key:当前小时数_分钟(0/1/2/3)_device_摄像头ObjectId
            redisUtil.set(key + "_device_" + e.getId(), e);
        });

        // 区域摄像头
        Query areaQuery = new Query();
        Criteria criteria = Criteria.where("date").is(LocalDate.now().toString());
        areaQuery.addCriteria(criteria)
                .with(Sort.by(Sort.Order.desc("area_id")));
        List<AreaDeviceSubject> areaList = mongotemplate.find(areaQuery, AreaDeviceSubject.class);
        areaList.forEach(e -> {
            // 根据总抓拍人数计算当前小时抓拍人数
            calcAreaDeviceHourNum(key, e);

            // 缓存区域当前摄像头人数，key:当前小时数_分钟(0/1/2/3)_area_摄像头ObjectId
            redisUtil.set(key + "_area_" + e.getId(), e);
        });
    }

    /**
     * 因为需要15分钟记录一次数据，所以根据当前分钟分组
     * 0-15  0
     * 15-30 1
     * 30-45 2
     * 45-00 3
     * 返回key = hour_group
     */
    private String getKey(int hour, int minute) {
        String key = String.valueOf(hour);
        if (minute <= 15) {
            key = key + "_0";
        } else if (minute <= 30) {
            key = key + "_1";
        } else if (minute <= 45) {
            key = key + "_2";
        } else {
            key = key + "_3";
        }
        return key;
    }

    /**
     * 根据总抓拍人数计算当前小时抓拍人数
     * 因为每15分钟缓存一次数据，所以根据minute分钟数来分组(0 1 2 3)
     */
    private void calcDeviceHourNum(String key, DeviceSubject subject) {
        String[] s = key.split("_");
        int hour = Integer.parseInt(s[0]);
        int minute = Integer.parseInt(s[1]);
        // 如果当前时间为0点之后，则当前人数为总人数减去上一次缓存的总人数
        if (hour > 0) {
            // 分组 0 1 2 3
            int lastHour = hour;
            if (minute == 0) {
                lastHour = hour - 1;
                minute = 3;
            } else {
                minute = minute - 1;
            }
            String lastKey = lastHour + "_" + minute;
            DeviceSubject sub = (DeviceSubject) redisUtil.get(lastKey + "_device_" + subject.getId());
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
     * 根据总抓拍人数计算当前小时抓拍人数
     */
    private void calcAreaDeviceHourNum(String key, AreaDeviceSubject subject) {
        String[] s = key.split("_");
        int hour = Integer.parseInt(s[0]);
        int minute = Integer.parseInt(s[1]);
        // 如果当前时间为0点之后，则当前人数为总人数减去上一小时的总人数
        if (hour > 0) {
            int lastHour = hour;
            if (minute == 0) {
                lastHour = hour - 1;
                minute = 3;
            } else {
                minute = minute - 1;
            }
            String lastKey = lastHour + "_" + minute;
            AreaDeviceSubject sub = (AreaDeviceSubject) redisUtil.get(lastKey + "_area_" + subject.getId());
            subject.setTotalFaceSubjectNum((subject.getTodayFaceSubjectNum() + subject.getTodayBodySubjectNum()));
            if (null != sub) {
                int num = subject.getTotalFaceSubjectNum() - sub.getTotalFaceSubjectNum();
                subject.setHourFaceSubjectNum(Math.max(num, 0));
            } else {
                subject.setHourFaceSubjectNum(subject.getTotalFaceSubjectNum());
            }
        } else {
            // 0点的当前人数则为总抓拍人数
            subject.setTotalFaceSubjectNum((subject.getTodayFaceSubjectNum() + subject.getTodayBodySubjectNum()));
            subject.setHourFaceSubjectNum(subject.getTotalFaceSubjectNum());
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
