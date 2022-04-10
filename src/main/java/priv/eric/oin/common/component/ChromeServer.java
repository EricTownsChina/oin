package priv.eric.oin.common.component;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Desc: chrome浏览器相关方法
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/10 11:16
 */
@Slf4j
@Component
public class ChromeServer {

    private static final String DRIVER_URL_KEY = "webdriver.chrome.driver";

    @Value("${chrome-driver.url}")
    private String chromeDriver;

    private volatile WebDriver webDriver;

    public WebDriver getWebDriver() {
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

    /**
     * 获取指定url的Document
     *
     * @param url url
     * @return Document
     */
    public Document getDoc(String url) {
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
