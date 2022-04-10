package priv.eric.oin.danmaku.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import priv.eric.oin.common.entity.Resp;
import priv.eric.oin.common.utils.CaffeineUtil;
import priv.eric.oin.danmaku.service.DanmakuService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc: 弹幕文件获取
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/13 23:05
 */
public class DanmakuServiceImpl implements DanmakuService {

    private static final String CACHE_KEY_CID = "oid";

    @Value("${bilibili.danmuku.history.url}")
    private String historyDanmakuUrl;

    @Override
    public Resp fetchDanmakuFile() {
        // 获取视频oid列表
        List<String> oidList = Convert.toList(String.class, CaffeineUtil.get(CACHE_KEY_CID));

        //
        Map<String, Object> params = new HashMap<>();
        params.put("type", 1);
        params.put("oid", 1);
        params.put("date", 1);
        String s = HttpUtil.get(historyDanmakuUrl, params);

        return Resp.ok();
    }


}
