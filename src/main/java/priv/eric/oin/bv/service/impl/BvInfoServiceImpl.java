package priv.eric.oin.bv.service.impl;

import cn.hutool.core.date.DateUtil;
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
import priv.eric.oin.common.utils.TimeUtil;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

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

    private Set<String> existsUrls = new HashSet<>();

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
    public Set<String> getBvCodesByUrl(String url) {
        existsUrls = new HashSet<>();
        return recursionGetBvCodes(url);
    }

    private Set<String> recursionGetBvCodes(String url) {
        Document doc = chromeServer.getDoc(url);
        Elements bvElements = doc.select("a[href*=/BV]");

        Set<String> bvCodes = bvElements.stream().map(this::getBvCode).collect(Collectors.toSet());

        Elements buckets = doc.select("a[href^=//www.bilibili.com/]");
        log.info("buckets size = {}", buckets.size());
        for (Element bucket : buckets) {
            String bvHref = bucket.attr("href");
            if (bvHref.startsWith("//") && bvHref.endsWith("/")) {
                if (existsUrls.contains(bvHref)) {
                    continue;
                }
                existsUrls.add(bvHref);
                bvHref = "https:" + bvHref;
                Set<String> recBvCodes = recursionGetBvCodes(bvHref);
                bvCodes.addAll(recBvCodes);
            }
        }

        return bvCodes;
    }

    private String getBvCode(Element element) {
        // 提取出BV号信息
        String bvHref = element.attr("href");
        int startIndex = bvHref.indexOf("/BV");
        int endIndex = startIndex + BV_LENGTH + 1;
        if (-1 == startIndex || endIndex > bvHref.length()) {
            log.info("解析bvCode失败, bvHref = {}", bvHref);
            return null;
        }
        return bvHref.substring(startIndex + 1, endIndex);
    }

    @Override
    public void send(String bvid) {
        try {
            String bvBaseInfoStr = getBvBaseInfoStr(bvid);
            BvInfoDoc bvInfoDoc = processBvInfo(bvBaseInfoStr);

            kafkaProducer.send(topicBvBaseInfo, bvid, bvInfoDoc);
        } catch (Exception e) {
            log.error("===== 推送 [{}] bv信息失败 : ", bvid, e);
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

        // 补充源数据
        supplementDataSource(originBvInfoStr, bvInfoDoc);
        // 补充作者
        supplementOwner(bvInfoJsonObj, bvInfoDoc);
        // 补充统计信息
        supplementStats(bvInfoJsonObj, bvInfoDoc);

        // 标准化时间字段
        standardTimeFields(bvInfoDoc);


        // 补充code
        bvInfoDoc.setCode(bvInfoDoc.getBvid());
        log.info("===== bvDocInfo : {}", bvInfoDoc);
        return bvInfoDoc;
    }

    private void standardTimeFields(BvInfoDoc bvInfoDoc) {
        bvInfoDoc.setPubDate(TimeUtil.secondToMillis(bvInfoDoc.getPubDate()));
        bvInfoDoc.setCTime(TimeUtil.secondToMillis(bvInfoDoc.getCTime()));

    }

    private void supplementDataSource(String originBvInfoStr, BvInfoDoc bvInfoDoc) {
        bvInfoDoc.setDataSource(originBvInfoStr);
    }

    private void supplementStats(JSONObject bvInfoJsonObj, BvInfoDoc bvInfoDoc) {
        JSONObject stat = bvInfoJsonObj.getJSONObject("stat");
        bvInfoDoc.setNowRank(stat.getLong("now_rank"));
        bvInfoDoc.setLike(stat.getLong("like"));
        bvInfoDoc.setDislike(stat.getLong("dislike"));
        bvInfoDoc.setView(stat.getLong("view"));
        bvInfoDoc.setDanmaku(stat.getLong("danmaku"));
        bvInfoDoc.setShare(stat.getLong("share"));
        bvInfoDoc.setReply(stat.getLong("reply"));
        bvInfoDoc.setFavorite(stat.getLong("favorite"));
        bvInfoDoc.setCoin(stat.getLong("coin"));
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
