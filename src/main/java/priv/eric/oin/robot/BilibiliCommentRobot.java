package priv.eric.oin.robot;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import priv.eric.oin.robot.handler.BvCommentParserHandler;

import javax.annotation.Resource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.*;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-16 16:18
 * <p>
 * desc: bilibili BV 弹幕爬虫
 */
@Slf4j
@Service
public class BilibiliCommentRobot {

    @Value("${robot.bilibili.bv.base.url}")
    private String bvBaseInfo;
    @Value("${robot.bilibili.comment.xml.url}")
    private String bvCommentUrl;
    @Value("${robot.bilibili.comment.xml.local}")
    private String bvCommentLocal;

    @Resource
    private BvCommentParserHandler parserHandler;

    private static final String PLACEHOLDER_BVID = "{bvid}";
    private static final String PLACEHOLDER_CID = "{cid}";

//    String bvCode = "BV1ct411H77Z";
//    String bvCode = "BV1XC4y1p7Yn";

    /**
     * 保存BV的弹幕信息
     *
     * @param bvCode BV 号
     */
    public void saveBvComment(String bvCode) {
        // 参数校验
        if (StringUtils.isEmpty(bvCode)) {
            log.info("没有要获取的BV号");
            return;
        }

        // 获取bv对应的cid列表
        Set<String> cidSet = getCid(bvCode);
        if (CollectionUtils.isEmpty(cidSet)) {
            log.info("没有获取到BV号为 {} 对应的cid列表", bvCode);
            return;
        }

        // 遍历处理
        for (String cid : cidSet) {
            // 获取弹幕文件
            File xmlFile;
            try {
                // 获取弹幕xml文件
                xmlFile = getCommentXml(cid);
                // 随机睡眠
                int i = new Random(2).nextInt(10);
                Thread.sleep(i * 1000);
            } catch (Exception e) {
                log.error("get xml file error : ", e);
                continue;
            }
            if (null == xmlFile) {
                continue;
            }

            // 解析文件, 推入消息队列
            saxParserComment(xmlFile, bvCode, cid);
        }
    }

    /**
     * sax解析弹幕xml文件
     *
     * @param xmlFile 弹幕xml文件
     * @param bvCode  BV号
     * @param cid     cid
     */
    private void saxParserComment(File xmlFile, String bvCode, String cid) {
        try {
            // 通过SAXParserFactory的静态方法newInstance()方法获取SAXParserFactory实例对象factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // 通过SAXParserFactory实例的newSAXParser()方法返回SAXParser实例parser
            SAXParser saxParser = factory.newSAXParser();
            // 定义SAXParserHandler对象
            parserHandler.setCid(cid);
            parserHandler.setBvCode(bvCode);
            // 解析XML文档
            saxParser.parse(xmlFile, parserHandler);
        } catch (Exception e) {
            log.error("Exception : ", e);
        }
    }

    /**
     * 获取弹幕xml文件
     *
     * @param cid cid
     * @return 本地保存的xml文件
     */
    private File getCommentXml(String cid) {
        File baseDir = new File(bvCommentLocal);
        // 没有存储文件夹, 创建; 失败返回
        if (!baseDir.exists() && baseDir.isDirectory() && !baseDir.mkdirs()) {
            return null;
        }

        // 存放的地址
        String newFileName = bvCommentLocal + File.separator + cid + "-" + System.currentTimeMillis() + ".xml";
        File newFile = new File(newFileName);

        // 要爬取的地址
        String xmlFileUrl = bvCommentUrl.replace(PLACEHOLDER_CID, cid);
        log.info("xml file url = {}", xmlFileUrl);

        // 爬取xml到存放地址
        HttpUtil.downloadFile(xmlFileUrl, newFile.getAbsoluteFile());
        return newFile;
    }

    /**
     * 获取视频的cid (存在多P情况, 返回list)
     *
     * @param bvCode av号
     * @return List
     */
    private Set<String> getCid(String bvCode) {
        // 参数校验
        if (StringUtils.isEmpty(bvCode)) {
            log.info("avCode is empty.");
            return null;
        }
        Set<String> cidSet = new HashSet<>();

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
