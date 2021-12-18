package priv.eric.oin.job;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-16 1:33
 * <p>
 * desc:
 */
@Slf4j
@Component
public class RobotBilibiliJob {

//    @Scheduled(cron = "${job.bilibili.rank}")
    public void job1() {
        String s = HttpUtil.get("https://api.bilibili.com/pgc/web/rank/list?season_type=1&day=3");
        log.info("get data : {}", s);
    }

}
