package priv.eric.oin.danmaku.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc: 弹幕线程池
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/14 0:04
 */
@Slf4j
public class DanmakuThreadPool {

    public static ThreadPoolExecutor singleInstance() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    public static ThreadPoolExecutor instance(String threadName) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                availableProcessors,
                availableProcessors * 2,
                30,
                TimeUnit.SECONDS,
                // 消息队列
                new ArrayBlockingQueue<>(1000),
                // 线程工厂(设置名称)
                new DanmakuThreadPool.DanmakuThreadFactory(threadName),
                // 拒绝策略
                (r, executor) -> {
                    // 抄的dubbo的拒绝策略, 增强了日志
                    String msg = String.format("Danmaku Thread pool is EXHAUSTED!" +
                                    " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                                    " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)!",
                            threadName, executor.getPoolSize(), executor.getActiveCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getLargestPoolSize(),
                            executor.getTaskCount(), executor.getCompletedTaskCount(), executor.isShutdown(), executor.isTerminated(), executor.isTerminating());
                    log.warn(msg);
                    // 抄的activeMq的拒绝策略, 最大限度尝试
                    try {
                        if (!executor.getQueue().offer(r, 60, TimeUnit.SECONDS)) {
                            throw new RejectedExecutionException("try offer task to queue failure, o(╥﹏╥)o");
                        }
                    } catch (InterruptedException e) {
                        throw new RejectedExecutionException("Interrupted waiting for BrokerService.worker");
                    }
                    throw new RejectedExecutionException("Timed Out while attempting to enqueue Task.");
                }
        );

    }

    static class DanmakuThreadFactory implements ThreadFactory {
        String threadName;
        AtomicInteger nums = new AtomicInteger(0);

        public DanmakuThreadFactory(String threadName) {
            this.threadName = threadName + "-" + nums.getAndIncrement();
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadName);
        }
    }

}
