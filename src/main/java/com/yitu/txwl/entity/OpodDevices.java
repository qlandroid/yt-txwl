package com.yitu.txwl.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.StringJoiner;

/**
 * 摄像头数据表
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-18 12:05
 */
@Document(collection = "opod_devices")
public class OpodDevices {

    @Id
    private String id;

    private String category;

    private String name;

    private Double longitude;

    private Double latitude;

    @Field("address_type")
    private String addressType;

    @Field("_class")
    private String className;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OpodDevices.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("category='" + category + "'")
                .add("name='" + name + "'")
                .add("longitude=" + longitude)
                .add("latitude=" + latitude)
                .add("addressType='" + addressType + "'")
                .add("className='" + className + "'")
                .toString();
    }
}
