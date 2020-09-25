package com.yitu.txwl.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.StringJoiner;

/**
 * 接口工具调用时间缓存
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-24 21:54
 */
@Document(collection = "fetch_tool_cache")
public class FetchTrackToolCache {

    @Id
    private String id;

    @Field("time")
    private Integer time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FetchTrackToolCache.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("time=" + time)
                .toString();
    }
}
