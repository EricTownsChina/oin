package priv.eric.oin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import priv.eric.oin.service.BvCommentService;
import priv.eric.oin.common.component.BvCommentKafkaProducer;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/12 22:37
 * <p>
 * Desc:
 */
@Slf4j
@Service
public class BvCommentServiceImpl implements BvCommentService {

    private static final String PLACEHOLDER_BVID = "{bvid}";
    private static final String PLACEHOLDER_CID = "{cid}";

    @Value("${robot.bilibili.bv.base.url}")
    private String bvBaseInfo;
    @Value("${robot.bilibili.comment.xml.url}")
    private String bvCommentUrl;
    @Value("${robot.bilibili.comment.xml.local}")
    private String bvCommentLocal;

    @Resource
    private BvCommentKafkaProducer bvCommentKafkaProducer;


    @Override
    public void getCommentByBvCode(String bvCode) {
        Deque<String> cids = getCid(bvCode);
        if (CollectionUtil.isEmpty(cids)) {
            return;
        }

        // 处理
        cids.forEach(this::getCommentXml);
    }

    @Override
    public void getCommentByBvCode(Collection<String> bvCodes) {
        if (CollectionUtil.isEmpty(bvCodes)) {
            return;
        }

        for (String bvCode : bvCodes) {
            try {
                getCommentByBvCode(bvCode);
                Thread.sleep(5000);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 获取弹幕xml文件
     *
     * @param cid cid
     */
    private void getCommentXml(String cid) {
        File baseDir = new File(bvCommentLocal);
        // 没有存储文件夹, 创建; 失败返回
        if (!baseDir.exists() && baseDir.isDirectory() && !baseDir.mkdirs()) {
            return;
        }

        // 存放的地址
        String newFileName = bvCommentLocal + File.separator + cid + "-" + System.currentTimeMillis() + ".xml";
        File newFile = new File(newFileName);
        String filePath = newFile.getAbsolutePath();

        // 要爬取的地址
        String xmlFileUrl = bvCommentUrl.replace(PLACEHOLDER_CID, cid);
        log.info("xml file url = {}", xmlFileUrl);

        // 爬取xml到存放地址
        long fileSize = HttpUtil.downloadFile(xmlFileUrl, newFile.getAbsoluteFile());
        if (fileSize > 0L) {
            bvCommentKafkaProducer.send(filePath);
        }
    }

    /**
     * 获取视频的cid (存在多P情况, 返回list)
     *
     * @param bvCode av号
     * @return List
     */
    private Deque<String> getCid(String bvCode) {
        // 参数校验
        if (StringUtils.isEmpty(bvCode)) {
            log.info("avCode is empty.");
            return null;
        }
        Deque<String> cidSet = new ArrayDeque<>();

        // 获取视频基本信息
        String s = HttpUtil.get(bvBaseInfo.replace(PLACEHOLDER_BVID, bvCode));
        if (StringUtils.isEmpty(s)) {
            log.info("获取 {} 视频基本信息失败.", bvCode);
            return null;
        }

        // 解析返回值
        JSONObject baseInfo;
        try {
            baseInfo = JSON.parseObject(s);
            // 没有获取到基本信息
            if (null == baseInfo || !"0".equals(baseInfo.getString("code"))) {
                return null;
            }
            JSONArray data = baseInfo.getJSONArray("data");
            if (null == data || 0 == data.size()) {
                return null;
            }
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                String cid = jsonObject.getString("cid");
                if (StringUtils.isEmpty(cid)) {
                    continue;
                }
                // 添加cid
                cidSet.add(cid);
            }
            return cidSet;

        } catch (Exception e) {
            log.error("Exception : ", e);
            return null;
        }
    }

}
