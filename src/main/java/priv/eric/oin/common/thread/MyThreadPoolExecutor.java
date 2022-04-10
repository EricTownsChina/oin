package priv.eric.oin.common.thread;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author EricTownsChina@outlook.com
 * create 2021-12-20 15:42
 * <p>
 * desc: 自定义线程池
 */
@Slf4j
public class MyThreadPoolExecutor {

    /**
     * CPU核心数
     */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final String THREAD_NAME = "oin";

    private MyThreadPoolExecutor() {

    }

    public static ThreadPoolExecutor singleInstance() {
        return singleInstance(THREAD_NAME);
    }

    public static ThreadPoolExecutor singleInstance(String threadName) {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new MyThreadFactory(threadName));
    }

    public static ThreadPoolExecutor instance() {
        return instance(THREAD_NAME);
    }

    public static ThreadPoolExecutor instance(String threadName) {
        return new ThreadPoolExecutor(
                // 主线程个数 = CPU核心数
                PROCESSORS,
                // 线程池最大线程数 = CPU核心数 * 2
                PROCESSORS * 2,
                // 救急线程空闲存活时间 1分钟
                60L,
                // 救急线程空闲存活时间单位
                TimeUnit.SECONDS,
                // 消息队列
                new ArrayBlockingQueue<>(1000),
                // 线程工厂(设置名称)
                new MyThreadFactory(threadName),
                // 拒绝策略
                (r, executor) -> {
                    // 抄的dubbo的拒绝策略, 增强了日志
                    String msg = String.format("Robot Thread pool is EXHAUSTED!" +
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

    static class MyThreadFactory implements ThreadFactory {
        String threadName;
        AtomicInteger nums = new AtomicInteger(0);

        public MyThreadFactory(String threadName) {
            this.threadName = threadName + "-" + nums.getAndIncrement();
        }

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            return new Thread(r, threadName);
        }
    }

}
