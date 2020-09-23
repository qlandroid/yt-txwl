package com.yitu.txwl.pojo;

import java.util.List;
import java.util.StringJoiner;

/**
 * 国展中心入展时间统计返回数据
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-23 17:45
 */
public class CenterEnterPojo {

    private Integer areaId;

    private String areaName;

    private List<Statistic> statistic;

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<Statistic> getStatistic() {
        return statistic;
    }

    public void setStatistic(List<Statistic> statistic) {
        this.statistic = statistic;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CenterEnterPojo.class.getSimpleName() + "[", "]")
                .add("areaId=" + areaId)
                .add("areaName='" + areaName + "'")
                .add("statistic=" + statistic)
                .toString();
    }
}
