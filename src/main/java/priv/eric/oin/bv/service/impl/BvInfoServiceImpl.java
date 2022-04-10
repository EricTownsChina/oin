package priv.eric.oin.bv.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import priv.eric.oin.bv.entity.BvInfoDoc;
import priv.eric.oin.bv.service.BvInfoService;
import priv.eric.oin.common.component.ChromeServer;
import priv.eric.oin.common.component.KafkaProducer;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Desc: 视频信息获取service impl
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/23 21:39
 */
@Slf4j
@Service
public class BvInfoServiceImpl implements BvInfoService {

    /**
     * bv号长度
     */
    private static final int BV_LENGTH = 12;

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private ChromeServer chromeServer;

    @Value("${bilibili.bv.info.url}")
    private String bvBaseInfoUrl;

    @Value("${bilibili.bv.info.kafka-topic}")
    private String topicBvBaseInfo;

    @Override
    public String getBvBaseInfoStr(String bvid) {
        try {
            if (null == bvid || bvid.isEmpty()) {
                throw new IllegalArgumentException("bvid 不能为空");
            }
            // 调用api接口获取返回
            String resp = HttpRequest
                    .get(bvBaseInfoUrl)
                    .form("bvid", bvid)
                    .execute()
                    .body();
            log.info("===== bvBaseInfoUrl response = {}", resp);

            // 处理返回结果
            JSONObject respJsonObject = JSON.parseObject(resp);
            Integer code = respJsonObject.getInteger("code");
            if (0 != code) {
                return null;
            }

            // 返回
            return respJsonObject.getJSONObject("data").toJSONString();
        } catch (Exception e) {
            log.error("===== getBvBaseInfoStr Exception : ", e);
            return null;
        }
    }

    @Override
    public Deque<String> getBvCodesByUrl(String url) {
        Deque<String> bvCodes = new ArrayDeque<>();
        Document doc = chromeServer.getDoc(url);
        // 选择负责条件的元素
        Elements bvElements = doc.select("a[href*=/BV]");

        // 遍历符合条件的元素
        for (Element bvElement : bvElements) {
            // 提取出BV号信息
            String bvHref = bvElement.attr("href");
            int startIndex = bvHref.indexOf("/BV");
            int endIndex = startIndex + BV_LENGTH + 1;
            if (-1 == startIndex || endIndex > bvHref.length()) {
                log.info("解析bvCode失败, bvHref = {}", bvHref);
                continue;
            }
            String bvCode = bvHref.substring(startIndex + 1, endIndex);
            // 统计
            bvCodes.add(bvCode);
        }

        return bvCodes;
    }

    @Override
    public void store(String bvid) {
        try {
            String bvBaseInfoStr = getBvBaseInfoStr(bvid);
            BvInfoDoc bvInfoDoc = processBvInfo(bvBaseInfoStr);

            kafkaProducer.send(topicBvBaseInfo, bvInfoDoc);
        } catch (Exception e) {
            log.error("===== 存储 {} bv信息失败 : ", bvid, e);
        }
    }


    private BvInfoDoc processBvInfo(String originBvInfoStr) {
        // 非空校验
        if (null == originBvInfoStr || originBvInfoStr.isEmpty()) {
            return null;
        }

        // 将信息字符串转换为json obj
        JSONObject bvInfoJsonObj = JSON.parseObject(originBvInfoStr);
        BvInfoDoc bvInfoDoc = bvInfoJsonObj.toJavaObject(BvInfoDoc.class);

        // 补充作者
        supplementOwner(bvInfoJsonObj, bvInfoDoc);

        // 补充code
        bvInfoDoc.setCode(bvInfoDoc.getBvid());
        log.info("===== bvDocInfo : {}", bvInfoDoc);
        return bvInfoDoc;
    }

    private void supplementOwner(JSONObject bvInfoJsonObj, BvInfoDoc bvInfoDoc) {
        JSONObject owner = bvInfoJsonObj.getJSONObject("owner");
        bvInfoDoc.setOwnerFace(owner.getString("face"));
        bvInfoDoc.setOwnerName(owner.getString("name"));
        bvInfoDoc.setOwnerMid(owner.getInteger("mid"));
    }

    @Override
    public List<Map<String, Object>> bulkBvBaseInfo(List<String> bvids) {
        return null;
    }

}
