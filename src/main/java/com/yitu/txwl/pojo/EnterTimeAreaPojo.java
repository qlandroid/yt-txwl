package com.yitu.txwl.pojo;

import java.util.List;

public class EnterTimeAreaPojo {

    private String areaName;

    private List<String> bindCameraIds;

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<String> getBindCameraIds() {
        return bindCameraIds;
    }

    public void setBindCameraIds(List<String> bindCameraIds) {
        this.bindCameraIds = bindCameraIds;
    }
}
