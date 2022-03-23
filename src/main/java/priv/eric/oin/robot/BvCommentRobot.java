package priv.eric.oin.robot;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import priv.eric.oin.service.BvCodeService;
import priv.eric.oin.service.BvCommentService;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/13 1:06
 * <p>
 * Desc:
 */
@Slf4j
@Service
public class BvCommentRobot {

    public static final long expirePeriod = 1000 * 60 * 60 * 3;
    public static final Map<String, Long> BV_MAP = new ConcurrentHashMap<>();
    @Resource
    private BvCodeService bvCodeService;
    @Resource
    private BvCommentService bvCommentService;

    public void saveCommentFiles(String url) {
        // 参数校验
        if (StrUtil.isBlank(url)) {
            return;
        }

        // 获取网页下所有bv号
        Deque<String> bvCodes = bvCodeService.getBvCodesByUrl(url);
        if (CollectionUtil.isEmpty(bvCodes)) {
            log.info("===== bvCodes is empty.");
            return;
        }

        // 过滤重复的bv code
        bvCodes = filter(bvCodes);
        log.info("------------------------------------------  bvCodes = {}", bvCodes);

        // 根据bv code获取弹幕文件
        bvCommentService.getCommentByBvCode(bvCodes);
    }

    private Deque<String> filter(Deque<String> bvCodes) {
        return bvCodes.stream()
                .filter(bvCode -> {
                    if (!BV_MAP.containsKey(bvCode)) {
                        return true;
                    }

                    Long saveTime = BV_MAP.get(bvCode);
                    if (null == saveTime) {
                        BV_MAP.remove(bvCode);
                        return true;
                    }

                    return SystemClock.now() - saveTime > expirePeriod;
                }).collect(Collectors.toCollection(ArrayDeque::new));

    }

}
