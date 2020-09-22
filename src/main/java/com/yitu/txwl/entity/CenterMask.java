package com.yitu.txwl.entity;

import java.util.StringJoiner;

/**
 * 国展中心口罩指数
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-22 17:18
 */
public class CenterMask implements Comparable<CenterMask>{

    /** 摄像头ID */
    private Integer cameraId;

    /** 戴口罩人数 */
    private Integer maskFace;

    /** 总人数 */
    private Integer totalFace;

    /** 口罩指数 */
    private Integer maskProportion;

    public Integer getCameraId() {
        return cameraId;
    }

    public void setCameraId(Integer cameraId) {
        this.cameraId = cameraId;
    }

    public Integer getMaskFace() {
        return maskFace == null ? 0 : maskFace;
    }

    public void setMaskFace(Integer maskFace) {
        this.maskFace = maskFace;
    }

    public Integer getTotalFace() {
        return totalFace == null ? 0 : totalFace;
    }

    public void setTotalFace(Integer totalFace) {
        this.totalFace = totalFace;
    }

    public Integer getMaskProportion() {
        return maskProportion;
    }

    public void setMaskProportion(Integer maskProportion) {
        this.maskProportion = maskProportion;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CenterMask.class.getSimpleName() + "[", "]")
                .add("cameraId=" + cameraId)
                .add("maskFace=" + maskFace)
                .add("totalFace=" + totalFace)
                .add("maskProportion=" + maskProportion)
                .toString();
    }

    /** 降序排序 */
    @Override
    public int compareTo(CenterMask o) {
        return o.getMaskProportion().compareTo(maskProportion);
    }
}
