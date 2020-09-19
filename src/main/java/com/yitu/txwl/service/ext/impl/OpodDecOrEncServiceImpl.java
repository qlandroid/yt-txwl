package com.yitu.txwl.service.ext.impl;

import com.alibaba.fastjson.JSONObject;
import com.yitu.txwl.service.ext.OpodDecOrEncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-18 19:37
 */
@Service
public class OpodDecOrEncServiceImpl implements OpodDecOrEncService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${OpodDecOrEnc.url}")
    private String httpUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JSONObject encodedDeviceId(String id) {
        String url = httpUrl + "/opod/v2/debug?type=CameraId&decoded_id=" + id;
        JSONObject object = restTemplate.getForObject(URI.create(url), JSONObject.class);
        log.info("加密接口返回值---> {}", object);
        return object;
    }

    @Override
    public JSONObject decodedDeviceId(String id) {
        String url = httpUrl + "/opod/v2/debug?type=CameraId&encoded_id=" + id;
        JSONObject object = restTemplate.getForObject(URI.create(url), JSONObject.class);
        log.info("解密接口返回值---> {}", object);
        return object;
    }
}
