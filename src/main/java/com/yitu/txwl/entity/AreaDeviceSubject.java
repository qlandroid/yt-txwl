package com.yitu.txwl.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.StringJoiner;

/**
 * 各区域摄像头人数
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-21 15:50
 */
@Document(collection = "opod_statistic_realtime_subject")
public class AreaDeviceSubject {

    @Id
    private String id;

    @Field("timestamp")
    private Integer timestamp;
    @Field("date")
    private String date;
    @Field("last_modify_timestamp")
    private Integer lastModifyTimestamp;
    @Field("area_id")
    private Integer araeId;
    @Field("area_name")
    private String areaName;
    @Field("today_face_subject_num")
    private Integer todayFaceSubjectNum;
    @Field("today_body_subject_num")
    private Integer todayBodySubjectNum;
    @Field("today_motor_vehicle_subject_num")
    private Integer todayMotorVehicleSubjectNum;

    /** 今日区域人数 */
    private Integer totalFaceSubjectNum;
    /** 当前小时的区域人数 */
    private Integer hourFaceSubjectNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getLastModifyTimestamp() {
        return lastModifyTimestamp;
    }

    public void setLastModifyTimestamp(Integer lastModifyTimestamp) {
        this.lastModifyTimestamp = lastModifyTimestamp;
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

    public Integer getTodayFaceSubjectNum() {
        return todayFaceSubjectNum;
    }

    public void setTodayFaceSubjectNum(Integer todayFaceSubjectNum) {
        this.todayFaceSubjectNum = todayFaceSubjectNum;
    }

    public Integer getTodayBodySubjectNum() {
        return todayBodySubjectNum;
    }

    public void setTodayBodySubjectNum(Integer todayBodySubjectNum) {
        this.todayBodySubjectNum = todayBodySubjectNum;
    }

    public Integer getTodayMotorVehicleSubjectNum() {
        return todayMotorVehicleSubjectNum;
    }

    public void setTodayMotorVehicleSubjectNum(Integer todayMotorVehicleSubjectNum) {
        this.todayMotorVehicleSubjectNum = todayMotorVehicleSubjectNum;
    }

    public Integer getHourFaceSubjectNum() {
        return hourFaceSubjectNum;
    }

    public void setHourFaceSubjectNum(Integer hourFaceSubjectNum) {
        this.hourFaceSubjectNum = hourFaceSubjectNum;
    }

    public Integer getTotalFaceSubjectNum() {
        return totalFaceSubjectNum;
    }

    public void setTotalFaceSubjectNum(Integer totalFaceSubjectNum) {
        this.totalFaceSubjectNum = totalFaceSubjectNum;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AreaDeviceSubject.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("timestamp=" + timestamp)
                .add("date='" + date + "'")
                .add("lastModifyTimestamp=" + lastModifyTimestamp)
                .add("araeId=" + araeId)
                .add("areaName='" + areaName + "'")
                .add("todayFaceSubjectNum=" + todayFaceSubjectNum)
                .add("todayBodySubjectNum=" + todayBodySubjectNum)
                .add("todayMotorVehicleSubjectNum=" + todayMotorVehicleSubjectNum)
                .add("totalFaceSubjectNum=" + totalFaceSubjectNum)
                .add("hourFaceSubjectNum=" + hourFaceSubjectNum)
                .toString();
    }
}
