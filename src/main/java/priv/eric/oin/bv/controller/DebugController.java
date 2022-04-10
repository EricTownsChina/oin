package priv.eric.oin.bv.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import priv.eric.oin.bv.service.BvInfoService;
import priv.eric.oin.common.entity.Resp;

import javax.annotation.Resource;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/10 0:20
 */
@Slf4j
@RestController
public class DebugController {

    @Resource
    private BvInfoService bvInfoService;

    @GetMapping("/debug/info/get/{bvid}")
    public Resp getBvBaseInfo(@PathVariable String bvid) {
        return Resp.ok(bvInfoService.getBvBaseInfoStr(bvid));
    }

    @GetMapping("/debug/info/store/{bvid}")
    public Resp getBvBaseInfoTest(@PathVariable String bvid) {
        bvInfoService.store(bvid);
        return Resp.ok();
    }

}
