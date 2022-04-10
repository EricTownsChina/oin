package priv.eric.oin.bv.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import priv.eric.oin.bv.service.BvInfoService;

import javax.annotation.Resource;
import java.util.Deque;

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

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void store() {
        try {
            log.info("===== 存储bv信息开始...");
            Deque<String> bvCodes = bvInfoService.getBvCodesByUrl(bilibiliIndexUrl);
            if (null == bvCodes || bvCodes.isEmpty()) {
                return;
            }
            log.info("===== 获取到 bvCodes size = {}", bvCodes.size());
            bvCodes.forEach(bvCode -> bvInfoService.store(bvCode));
        } catch (Exception e) {
            log.error("===== 存储bv信息失败 : ", e);
        } finally {
            log.info("===== 存储bv信息结束...");
        }
    }

}
