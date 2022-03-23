package priv.eric.oin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import priv.eric.oin.robot.handler.RobotThreadPoolExecutor;
import priv.eric.oin.service.BvCodeService;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/16 0:43
 * <p>
 * Desc:
 */
@Slf4j
@Service
public class BvCodeServiceImpl implements BvCodeService {

    private static final String DRIVER_URL_KEY = "webdriver.chrome.driver";
    private static final int BV_LENGTH = 12;
    private static final Map<String, Integer> EXIST_URL_MAP = new ConcurrentHashMap<>();

    @Value("${robot.chrome.driver.url}")
    private String chromeDriver;

    private volatile WebDriver webDriver = null;

    private WebDriver getWebDriver() {
        if (null == webDriver) {
            synchronized (this) {
                if (null == webDriver) {
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
        }
        return webDriver;
    }

    @PostConstruct
    private void deleteUselessWebDriver() {
        RobotThreadPoolExecutor.singleInstance().execute(() -> {
            int i = 0;
            while (true) {
                if (null != webDriver && i == 2) {
                    webDriver = null;
                    i = 0;
                    log.info("===== clear webDriver and count i");
                } else if (null != webDriver) {
                    i++;
                }
                try {
                    Thread.sleep(1000L * 60 * 30);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });
    }


    @Override
    public Deque<String> getBvCodesByUrl(String url) {
        return executeDocumentComment(getDoc(url));
    }

    /**
     * 处理Document中的BV号
     *
     * @param doc Document
     */
    private Deque<String> executeDocumentComment(Document doc) {
        Deque<String> bvCodes = new ArrayDeque<>();
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

        // 获取Document中的所有带有bilibili的a标签href地址
//        Elements aElements = doc.select("a[href*=bilibili]");
//        aElements.forEach(aElement -> {
//            String href = aElement.attr("href");
//            // 已经解析过, 不再重复解析
//            if (EXIST_URL_MAP.containsKey(href)) {
//                EXIST_URL_MAP.compute(href, (k, v) -> v = (null == v ? 0 : ++v));
//                return;
//            }
//            // 记录解析url
//            EXIST_URL_MAP.put(href, 1);
//            if (href.startsWith("//")) {
//                href = "https:" + href;
//            }
//            if (href.startsWith("/")) {
//                href = "https://www.bilibili.com/" + href;
//            }
//            log.info("-------------------------------------- 获取到url = {}", href);
//            Document newDoc = getDoc(href);
//            bvCodes.addAll(executeDocumentComment(newDoc));
//        });
//        return bvCodes;
    }

    /**
     * 获取指定url的Document
     *
     * @param url url
     * @return Document
     */
    private Document getDoc(String url) {
        Document doc = null;
        try {
            webDriver = getWebDriver();
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
        } catch (Exception e) {
            log.error("getDoc error: ", e);
        }

        // 返回Document节点
        return doc;
    }

}
