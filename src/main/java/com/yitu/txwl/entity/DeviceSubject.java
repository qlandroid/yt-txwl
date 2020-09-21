package com.yitu.txwl.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.StringJoiner;

/**
 * 摄像头各时刻人数
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-17 18:56
 */
@Document(collection = "opod_statistic_realtime_device_subject")
public class DeviceSubject {

    @Id
    private String id;

    @Field("last_modify_timestamp")
    private Integer lastModifyTimestamp;
    @Field("device_id")
    private String deviceId;
    @Field("area_id")
    private Integer araeId;
    @Field("area_name")
    private String areaName;
    /** 总人数 */
    @Field("face_subject_num")
    private Integer faceSubjectNum;
    /** 当前小时人数 */
    private Integer faceHourNum;
    /** 摄像头名称 */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getLastModifyTimestamp() {
        return lastModifyTimestamp;
    }

    public void setLastModifyTimestamp(Integer lastModifyTimestamp) {
        this.lastModifyTimestamp = lastModifyTimestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getAraeId() {
        return araeId;
    }

    public void setAraeId(Integer araeId) {
        this.araeId = araeId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Integer getFaceSubjectNum() {
        return faceSubjectNum;
    }

    public void setFaceSubjectNum(Integer faceSubjectNum) {
        this.faceSubjectNum = faceSubjectNum;
    }

    public Integer getFaceHourNum() {
        return faceHourNum;
    }

    public void setFaceHourNum(Integer faceHourNum) {
        this.faceHourNum = faceHourNum;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DeviceSubject.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("lastModifyTimestamp=" + lastModifyTimestamp)
                .add("deviceId='" + deviceId + "'")
                .add("araeId=" + araeId)
                .add("areaName='" + areaName + "'")
                .add("faceSubjectNum=" + faceSubjectNum)
                .add("name='" + name + "'")
                .toString();
    }
}
