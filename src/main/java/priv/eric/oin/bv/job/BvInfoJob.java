package priv.eric.oin.bv.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import priv.eric.oin.bv.service.BvInfoService;
import priv.eric.oin.common.thread.MyScheduleThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Deque;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Desc: bv信息任务
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/10 17:06
 */
@Slf4j
@Component
@EnableScheduling
@DisallowConcurrentExecution
@Configuration
public class BvInfoJob {

    @Value("${bilibili.index.url}")
    private String bilibiliIndexUrl;

    @Resource
    private BvInfoService bvInfoService;

    @PostConstruct
    public void init() {
        storeJob();
    }


    public void storeJob() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = MyScheduleThreadPoolExecutor.instance(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new StoreTask(),10, 60, TimeUnit.SECONDS);
    }

    private void store() {
        try {
            log.info("===== 存储bv信息开始...");
            Set<String> bvCodes = bvInfoService.getBvCodesByUrl(bilibiliIndexUrl);
            if (null == bvCodes || bvCodes.isEmpty()) {
                return;
            }
            log.info("===== 获取到 bvCodes size = {}", bvCodes.size());
            bvCodes.forEach(bvCode -> {
                try {
                    long sleep = 1000L * new Random().nextInt(10);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    // ignore
                }
                bvInfoService.send(bvCode);
            });
        } catch (Exception e) {
            log.error("===== 存储bv信息失败 : ", e);
        } finally {
            log.info("===== 存储bv信息结束...");
        }
    }

    private class StoreTask implements Runnable {
        @Override
        public void run() {
            store();
        }
    }

}
