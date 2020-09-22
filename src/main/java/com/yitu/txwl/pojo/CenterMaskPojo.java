package com.yitu.txwl.pojo;

import java.util.StringJoiner;

/**
 * 国展中心口罩指数返回数据
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-22 18:06
 */
public class CenterMaskPojo {

    /** 摄像头ID */
    private Integer id;
    /** 摄像头ID */
    private String label;
    /** 比例(整数) */
    private Integer value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CenterMaskPojo() {
    }

    public CenterMaskPojo(Integer id, Integer value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CenterMaskPojo.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("label='" + label + "'")
                .add("value=" + value)
                .toString();
    }
}
