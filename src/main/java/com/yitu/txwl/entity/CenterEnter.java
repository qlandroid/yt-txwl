package com.yitu.txwl.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.StringJoiner;

/**
 * 缓存摄像头数据
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-24 19:57
 */
@Document(collection = "opod_cache")
public class CenterEnter {

    @Id
    private String id;

    /** 摄像头加密id */
    @Field("device_id")
    private String deviceId;

    @Field("name")
    private String name;

    @Field("total_num")
    private int totalNum;

    /** 历史人数 */
    @Field("face_num")
    private List<Integer> faceNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public List<Integer> getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(List<Integer> faceNum) {
        this.faceNum = faceNum;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CenterEnter.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("deviceId='" + deviceId + "'")
                .add("name='" + name + "'")
                .add("totalNum=" + totalNum)
                .add("faceNum=" + faceNum)
                .toString();
    }
}
