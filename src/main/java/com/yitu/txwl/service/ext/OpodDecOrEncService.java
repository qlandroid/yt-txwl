package com.yitu.txwl.service.ext;

import com.alibaba.fastjson.JSONObject;

/**
 * 摄像头加密或解密
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-18 19:34
 */
public interface OpodDecOrEncService {

    /**
     * 获取加密后摄像头
     * @param id
     * @return com.alibaba.fastjson.JSONObject
     *
     * @author WJ
     * @remark
     * @date   2020-09-18 19:39:19
     */
    JSONObject encodedDeviceId(String id);

    /**
     * 获取解密后摄像头
     * @param id
     * @return com.alibaba.fastjson.JSONObject
     *
     * @author WJ
     * @remark
     * @date   2020-09-18 19:40:13
     */
    JSONObject decodedDeviceId(String id);
}
