package com.yitu.txwl.pojo;

import java.util.StringJoiner;

/**
 * @author WJ
 * @version 1.0.0
 * @className Statistic
 * @description
 * @date 2020-09-23 19:16
 */
public class Statistic {
    private int start_timestamp;

    private int end_timestamp;

    private int pedestrian_num;

    private int face_num;

    public int getStart_timestamp() {
        return start_timestamp;
    }

    public void setStart_timestamp(int start_timestamp) {
        this.start_timestamp = start_timestamp;
    }

    public int getEnd_timestamp() {
        return end_timestamp;
    }

    public void setEnd_timestamp(int end_timestamp) {
        this.end_timestamp = end_timestamp;
    }

    public int getPedestrian_num() {
        return pedestrian_num;
    }

    public void setPedestrian_num(int pedestrian_num) {
        this.pedestrian_num = pedestrian_num;
    }

    public int getFace_num() {
        return face_num;
    }

    public void setFace_num(int face_num) {
        this.face_num = face_num;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Statistic.class.getSimpleName() + "[", "]")
                .add("start_timestamp=" + start_timestamp)
                .add("end_timestamp=" + end_timestamp)
                .add("pedestrian_num=" + pedestrian_num)
                .add("face_num=" + face_num)
                .toString();
    }
}
