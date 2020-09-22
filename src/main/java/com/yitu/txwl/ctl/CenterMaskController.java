package com.yitu.txwl.ctl;

import com.yitu.txwl.pojo.CenterMaskPojo;
import com.yitu.txwl.service.mask.CenterMaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 国展中心口罩指数
 *
 * @author WJ
 * @version 1.0.0
 * @date 2020-09-21 19:32
 */
@RestController
@RequestMapping("/mask")
public class CenterMaskController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CenterMaskService centerMaskService;

    /**
     *  国展中心口罩指数前四数据
     *
     * @author WJ
     * @date   2020-09-19 22:25:05
     */
    @GetMapping("/maskProportion")
    public List<CenterMaskPojo> listTop4MaskProportion() {
        return centerMaskService.getCenterMaskData();
    }

}
