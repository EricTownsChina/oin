package priv.eric.oin.robot;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private static final String DRIVER_URL_URL = "D:\\Chrome\\chromedriver.exe";
    private static final int BV_LENGTH = 12;
    private static final Map<String, Integer> EXIST_URL_MAP = new ConcurrentHashMap<>();

    @Resource
    private BilibiliCommentRobot commentRobot;

    public void handleBvHtml() {
        String rootHref= "https://www.bilibili.com";
        Document doc = getDoc(rootHref);
        EXIST_URL_MAP.put(rootHref, 1);
        executeDocumentBvCode(doc);
    }

    /**
     * 处理Document中的BV号
     *
     * @param doc Document
     */
    private void executeDocumentBvCode(Document doc) {
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
                EXIST_URL_MAP.compute(href, (k, v) -> v++);
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
            executeDocumentBvCode(newDoc);
        });
    }

    /**
     * 获取指定url的Document
     *
     * @param url url
     * @return Document
     */
    private Document getDoc(String url) {
        Document doc = null;

        // 设置系统参数
        System.setProperty(DRIVER_URL_KEY, DRIVER_URL_URL);
        //创建chrome参数对象
        ChromeOptions options = new ChromeOptions();
        //浏览器后台运行
        options.addArguments("headless");
        // 新建chrome驱动
        WebDriver webDriver = new ChromeDriver(options);

        webDriver.get(url);

        //等待几秒
        try {
            //向下滚动到页面底部
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("window.scrollTo(0, document.documentElement.clientHeight);");

            // 休息片刻
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        doc = Jsoup.parse(webDriver.getPageSource());

        // 关闭浏览器
        webDriver.close();
        webDriver.quit();

        // 返回Docment节点
        return doc;
    }



}
