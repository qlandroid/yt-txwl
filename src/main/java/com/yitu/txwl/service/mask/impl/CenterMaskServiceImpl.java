package com.yitu.txwl.service.mask.impl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.entity.CenterMask;
import com.yitu.txwl.entity.FetchTrackToolCache;
import com.yitu.txwl.pojo.CenterMaskPojo;
import com.yitu.txwl.pojo.CenterMaskSearch;
import com.yitu.txwl.service.mask.CenterMaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 国展中心口罩指数
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-22 11:28
 */
@Service
public class CenterMaskServiceImpl implements CenterMaskService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${fetchTrack.filePath}")
    private String filePath;
    @Autowired
    private MongoTemplate mongotemplate;

    @Override
    @Scheduled(cron = "0 0/5 * * * ?" )
    public void execFetchTrackMeta() {
        // 初始化工具地址
        initFilePath();

        // 修改接口工具配置文件
        updateFetchTrackConf();

        // 缓存当前调用时间戳 10位
        Long milli = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        updateCacheTime(milli);

        // 执行脚本之前先清空pedestrian_meta目录下文件
        deleteFiles();

        // 执行shell脚本
        execShell();

        // 读取pedestrian_meta文件下的meta文件并缓存结果
        // readFile();
    }

    @Override
    public List<CenterMaskPojo> getCenterMaskData(CenterMaskSearch search) {
        // TODO 先从缓存获取数据
        List<CenterMaskPojo> list;
        // 如果redis为空，则去读取接口文件目录meta下数据
        list = readFile(search);
        // 如果依然为空， 则重新调用定时任务，获取meta数据
        if (list.isEmpty()) {
            execFetchTrackMeta();
            list = readFile(search);
        }

        return list;
    }

    /**
     * 初始化接口工具目录地址
     * 如果impl配置了地址则使用配置文件的地址
     * 否则默认使用项目同级目录下的fetch_track地址
     */
    private void initFilePath() {
        // 如果已打成jar包，则返回jar包所在目录
        // 如果未打成jar，则返回项目所在目录
        String path = System.getProperty("user.dir") + File.separator + "fetch_track";
        filePath = StringUtils.isEmpty(filePath) ? path : filePath;
    }

    /**
     * 修改接口工具配置文件
     */
    private void updateFetchTrackConf() {
        String path = filePath + File.separator + "fetch_track_tool" + File.separator + "fetch_track_meta.conf";
        // 创建表及插入缓存数据
        if (!mongotemplate.collectionExists(FetchTrackToolCache.class)) {
            mongotemplate.createCollection(FetchTrackToolCache.class);
        }
        Query query = new Query();
        // 根据摄像头加密ID查询数据
        Criteria criteria = Criteria.where("id").is("fetch_track_start_time");
        query.addCriteria(criteria);
        FetchTrackToolCache cache = mongotemplate.findOne(query, FetchTrackToolCache.class);
        // 如果缓存中不存在说明还未调用过接口工具
        // 不修改配置文件
        if (null == cache) {
            return;
        }
        Integer startTime = cache.getTime();
        BufferedWriter bw = null;
        BufferedReader br = null;
        String line;
        //保存修改过后的所有内容，不断增加
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new Exception("配置文件不存在");
            }
            br = new BufferedReader(new FileReader(path));
            StringBuffer buf = new StringBuffer();
            while ((line = br.readLine()) != null) {
                //修改内容核心代码
                if (line.contains("start_time")) {
                    String[] split = line.split(":");
                    // 将配置的开始时间替换为上次的时间戳
                    String replace = line.replace(split[1], startTime.toString() + ",");
                    buf.append(replace);
                } else {
                    buf.append(line);
                }
                buf.append(System.getProperty("line.separator"));
            }
            bw = new BufferedWriter(new FileWriter(path));
            bw.write(buf.toString().toCharArray());
            bw.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
    }

    /**
     * 执行sh脚本
     */
    private void execShell() {
        String path = filePath + File.separator;
        String command = "bash run_fetch_track.sh";
        Process pid;
        try {
            // 执行linux sh命令
            log.info(command);
            pid = Runtime.getRuntime().exec(command, null, new File(path));
            if (null != pid) {
                pid.getOutputStream();
                // 等待进程结束
                pid.waitFor();
                if (pid.exitValue() == 0) {
                    log.info("execShell---------------> sh脚本执行成功");
                } else {
                    log.info("execShell---------------> sh脚本执行完成");
                }
            } else {
                log.error("execShell---------------> sh脚本执行失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     */
    private void deleteFiles() {
        String path = filePath + File.separator + "pedestrian_meta";
        File dirFile = new File(path);
        File[] files = dirFile.listFiles();
        if (null != files && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * 读取文件并缓存结果
     */
    private List<CenterMaskPojo> readFile(CenterMaskSearch search) {
        String path = filePath + File.separator + "pedestrian_meta";
        File dirFile = new File(path);
        File[] files = dirFile.listFiles();
        LinkedHashMap<Integer, CenterMask> map = new LinkedHashMap<>();
        if (null != files && files.length > 0) {
            BufferedReader br = null;
            String line;
            for (File file : files) {
                try {
                    if (file.isFile()) {
                        br = new BufferedReader(new FileReader(file));
                       /* if((line = br.readLine()) != null){
                            log.info("从meta里面读取了数据");
                        }*/
                        while ((line = br.readLine()) != null) {
                            // 每一行是一个行人数据
                            JSONObject object = JSONObject.parseObject(line);
                            Integer cameraId = (Integer) object.get("camera_id");
                            Integer recMask = (Integer) object.get("rec_mask");
                            CenterMask data = map.getOrDefault(cameraId, new CenterMask());
                            data.setCameraId(cameraId);
                            data.setTotalFace(data.getTotalFace() + 1);
                            // rec_mask 1是戴口罩，0是不戴口罩，-1是未知
                            if (null != recMask && recMask == 1) {
                                data.setMaskFace(data.getMaskFace() + 1);
                            }
                            map.put(cameraId, data);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            br = null;
                        }
                    }
                }
            }
        }


        List<CenterMaskPojo> reList = new ArrayList<CenterMaskPojo>();


        List<CenterMaskPojo> queryList = search.getQuery();
        Integer alltotalNum = 0;
        Integer allmaskNum = 0;

        if (null != queryList && queryList.size() > 0) {
            for (CenterMaskPojo msk : queryList) {
                String areaName = msk.getAreaName();
                String ids = msk.getIds();
                Integer value = 0; //当前展馆的比例
                Integer totalNum = 0;//全部人员
                Integer maskNum = 0;//带口罩的总数


                if (!StringUtils.isEmpty(ids)) {
                    String[] allid = ids.split(",");
                    for (int i = 0; i < allid.length; i++) {
                        Integer id = Integer.valueOf(allid[i]);
                        CenterMask data = map.getOrDefault(id, new CenterMask());
                        //查询单个 摄像头 id
                        if (null != data.getMaskFace()) {
                            maskNum = maskNum + data.getMaskFace();
                        }
                        if (null != data.getTotalFace()) {
                            totalNum = totalNum + data.getTotalFace();
                        }
                    }
                    alltotalNum += totalNum;
                    allmaskNum += maskNum;
                    // 计算总比例
                    if (totalNum > 0) {
                        BigDecimal totalMaskProportion = new BigDecimal(maskNum).multiply(new BigDecimal(100))
                                .divide(new BigDecimal(totalNum), BigDecimal.ROUND_HALF_UP);
                        value = totalMaskProportion.intValue();
                    }
                }
                msk.setLabel(areaName);
                msk.setValue(value);
                reList.add(msk);
            }
        }

        List<CenterMaskPojo> flaList = new ArrayList<CenterMaskPojo>();
        //统计总的数据
        String allLabel = search.getAllLabel();
        if (!StringUtils.isEmpty(allLabel)) {
            CenterMaskPojo mask = new CenterMaskPojo();
            Integer value = 0;
            if (alltotalNum > 0) {
                // 计算总比例
                BigDecimal totalMaskProportion = new BigDecimal(allmaskNum).multiply(new BigDecimal(100))
                        .divide(new BigDecimal(alltotalNum), BigDecimal.ROUND_HALF_UP);
                value = totalMaskProportion.intValue();
            }
            mask.setLabel(allLabel);
            mask.setValue(value);
            flaList.add(mask);
        }

        //循环reList 排序

        Collections.sort(reList, new Comparator<CenterMaskPojo>() {

            @Override
            public int compare(CenterMaskPojo o1, CenterMaskPojo o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                }
                if (o1.getValue() == o2.getValue()) {
                    return 0;
                }
                return 1;
            }

        });

        for (CenterMaskPojo deData : reList) {
            flaList.add(deData);
        }


        // TODO 将结果缓存
        return flaList;
    }

    /**
     * 缓存当前时间戳
     */
    private void updateCacheTime(Long milli) {
        // 缓存数据
        Query updateQuery = new Query();
        // 根据摄像头加密ID查询数据
        Criteria criteria = Criteria.where("id").is("fetch_track_start_time");
        updateQuery.addCriteria(criteria);
        // 更新的字段
        Update update = new Update();
        update.set("time", milli.intValue());
        mongotemplate.upsert(updateQuery, update, FetchTrackToolCache.class);
    }

}
