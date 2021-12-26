package priv.eric.oin.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import priv.eric.oin.dao.BilibiliBvDao;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-17 17:47
 * <p>
 * desc:
 */
@Slf4j
@Service
public class BilibiliBvRobot {

    private static final String DRIVER_URL_KEY = "webdriver.chrome.driver";
    private static final int BV_LENGTH = 12;
    private static final Map<String, Integer> EXIST_URL_MAP = new ConcurrentHashMap<>();

    @Resource
    private BilibiliBvDao bvDao;
    @Resource
    private BilibiliCommentRobot commentRobot;

    @Value("${robot.chrome.driver.url}")
    private String chromeDriver;

    @Scheduled(cron = "${robot.bilibili.comment.time}")
    public void handleBvCommentJob() {
        log.info("------------- BV comment job start...");

        String rootHref = "https://www.bilibili.com";
        // 获取页面元素
        Document doc = getDoc(rootHref);
        // 记录该页面
        EXIST_URL_MAP.put(rootHref, 1);
        // 处理弹幕信息
        executeDocumentComment(doc);

        log.info("------------- BV comment job end.");
    }

    // TODO: 2021-12-23 未完成的爬虫
    public void handleBvBaseInfoJob() {

    }

    /**
     * 处理Document中的BV号
     *
     * @param doc Document
     */
    private void executeDocumentComment(Document doc) {
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
            log.info("------------------------------------------  bvCode = {}", bvCode);

            // TODO: 2021-12-19 执行根据BV号获取弹幕的任务, 后续可以优化为消息队列
            commentRobot.saveBvComment(bvCode);
        }

        // 获取Document中的所有带有bilibili的a标签href地址
        Elements aElements = doc.select("a[href*=bilibili]");
        aElements.forEach(aElement -> {
            String href = aElement.attr("href");
            // 已经解析过, 不再重复解析
            if (EXIST_URL_MAP.containsKey(href)) {
                EXIST_URL_MAP.compute(href, (k, v) -> v = (null == v ? 0 : ++v));
                return;
            }
            // 记录解析url
            EXIST_URL_MAP.put(href, 1);
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (href.startsWith("/")) {
                href = "https://www.bilibili.com/" + href;
            }
            log.info("-------------------------------------- 获取到url = {}", href);
            Document newDoc = getDoc(href);
            executeDocumentComment(newDoc);
        });
    }

    /**
     * 获取指定url的Document
     *
     * @param url url
     * @return Document
     */
    private Document getDoc(String url) {
        Document doc;
        WebDriver webDriver = getWebDriver();
        // 访问指定url页面
        webDriver.get(url);
        // 模拟用户操作
        try {
            //向下滚动到页面底部
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("window.scrollTo(0, document.documentElement.clientHeight);");

            // 休息片刻
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 获取页面源代码
        doc = Jsoup.parse(webDriver.getPageSource());

        // 关闭浏览器
        webDriver.close();
        // 退出
        webDriver.quit();

        // 返回Document节点
        return doc;
    }

    private void parseBvBaseInfo(JSONObject bvBaseInfo) {
        if (null == bvBaseInfo) {
            log.info("BV基本信息为空");
            return;
        }

        // 视频信息
        JSONObject videoData = bvBaseInfo.getJSONObject("videoData");
        // 作者信息
        JSONObject upData = bvBaseInfo.getJSONObject("upData");
        // 标签信息
        JSONArray tags = bvBaseInfo.getJSONArray("tags");


    }

    /**
     * 获取BV号视频的基本信息
     *
     * @param bvCode BV号
     * @return 信息的JSON格式
     */
    private JSONObject getBvBaseInfo(String bvCode) {
        JSONObject baseInfo = null;
        try {
            // 获取BV视频播放页面
            WebDriver webDriver = getWebDriver();
            webDriver.get("view-source:https://www.bilibili.com/video/" + bvCode);

            // 获取页面源代码
            String pageSource = webDriver.getPageSource();
            // 获取BV视频基本信息
            int start = pageSource.indexOf("window.__INITIAL_STATE__=") + "window.__INITIAL_STATE__=".length();
            int end = pageSource.indexOf(";(function(){var s;(s=document.currentScript||document.scripts[document.scripts.length-1]).parentNode.removeChild(s);}());");
            String baseInfoStr = pageSource.substring(start, end);
            // 取消转义/
            baseInfoStr = baseInfoStr.replace("\\u002F", "/");

            // 解析成JSON格式, 返回
            return JSON.parseObject(baseInfoStr);
        } catch (Exception e) {
            log.info("get bv baseInfo error : ", e);
            return baseInfo;
        }
    }

    /**
     * 获取WebDriver
     *
     * @return WebDriver
     */
    private WebDriver getWebDriver() {
        // 设置系统参数
        System.setProperty(DRIVER_URL_KEY, chromeDriver);
        //创建chrome参数对象
        ChromeOptions options = new ChromeOptions();
        //浏览器后台运行
        options.addArguments("headless");
        options.addArguments("disable-dev-shm-usage");
        options.addArguments("no-sandbox");
        options.addArguments("blink-settings=imagesEnabled=false");
        options.addArguments("disable-gpu");
        // 新建chrome驱动
        return new ChromeDriver(options);
    }


}
