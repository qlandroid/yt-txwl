package com.yitu.txwl.ctl;

import com.yitu.txwl.pojo.CenterEnterPojo;
import com.yitu.txwl.service.device.CenterEnterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;

/**
 * 会展中心摄像头数据(模块3)
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-17 17:51
 */
@RestController
@RequestMapping("/device")
public class CenterEnterController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CenterEnterService centerEnterService;
    @Autowired
    private MongoTemplate mongotemplate;

    /**
     * 会展中心人数前四数据
     * 20200923 修改传回所有数据而不止前4数据
     *
     * @author WJ
     * @date 2020-09-19 22:25:05
     */
    @GetMapping("/centerStatistics")
    public LinkedList<CenterEnterPojo> listAllDevice() {
        return centerEnterService.listAllDevice();
    }

    @GetMapping("/update")
    public void testUpdate() {
        centerEnterService.updateDeviceSubjectData();
    }

    /*@GetMapping("/test")
    public void test() {
        // 创建表及插入缓存数据
        if (!mongotemplate.collectionExists(CenterEnter.class)) {
            mongotemplate.createCollection(CenterEnter.class);
        }
        Query updateQuery = new Query();
        Criteria criteria = Criteria.where("device_id").is("EQAAAAAA=");
        updateQuery.addCriteria(criteria);
        // 清空缓存的历史记录人数以及总记录人数
        Update update = new Update();
        update.set("totalNum", 0);
        update.set("face_num", new ArrayList<>());
        mongotemplate.upsert(updateQuery, update, CenterEnter.class);

    }*/

}
