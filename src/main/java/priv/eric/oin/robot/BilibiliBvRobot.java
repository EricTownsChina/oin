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

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-17 17:47
 * <p>
 * desc:
 */
@Slf4j
public class BilibiliBvRobot {

    private static final String DRIVER_URL_KEY = "webdriver.chrome.driver";
    private static final String DRIVER_URL_URL = "D:\\Chrome\\chromedriver.exe";

    public static void main(String[] args) {
        Document doc = getDoc("https://www.bilibili.com/v/popular/rank/guochan");

        System.out.println("===== end");
    }

    private static Document getDoc(String url) {
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
            //向下滚动  方法一
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("scrollTo(0,20000)");
            Thread.sleep(10000);

            /*
            //向下滚动 方法二
            ((JavascriptExecutor)webDriver).executeScript("scrollTo(0,10000)");*/

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

    public static void parseB(Document doc) {
        if (null == doc) {
            log.info("doc is null");
            return;
        }

    }

    /**
     * 解析传过来的doc
     * @param doc jsoup document
     */
    public static void parse(Document doc){
        if(doc == null){
            log.info("doc is null, unable to continue! ");
            return ;
        }
        Elements content = doc.select("div.content");

        // 计数器
        int i = 0;
        for (Element element : content) {
            i++;
            log.info("===== 读取到第 " + i + " 篇文章简介");
            //获取文章标题
            String title = element.select("a.title").text();
            //获取获取帖子网址
            String url = element.select("a.title").attr("href");
            url = "https://www.jianshu.com" + url;
            //获取文章的摘要
            String digest = element.select("p.abstract").text();
            //获取文章作者名称
            String author = element.select("a.nickname").text();
            //获取作者网址
            String authorUrl = element.select("a.nickname").attr("href");
            authorUrl = "https://www.jianshu.com" + authorUrl;

            log.info("title: " + title);
            log.info("url: " + url);
            log.info("digest:  " + digest);
            log.info("author: " + author);
            log.info("authorUrl: " + authorUrl);
            log.info("--------------\n");
        }

        log.info("===== 共获取到 " + i + " 篇文章简介");
    }



}
