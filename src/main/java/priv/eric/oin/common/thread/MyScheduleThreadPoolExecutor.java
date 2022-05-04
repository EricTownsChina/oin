package priv.eric.oin.common.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/5/4 16:22
 */
public class MyScheduleThreadPoolExecutor {

    private static final String THREAD_NAME_PREFIX = "priv.eric.oin-schedule-thread";

    private MyScheduleThreadPoolExecutor() {
    }

    public static ScheduledThreadPoolExecutor instance() {
        return instance(
                Runtime.getRuntime().availableProcessors(),
                THREAD_NAME_PREFIX,
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    public static ScheduledThreadPoolExecutor instance(int core) {
        return instance(
                core,
                THREAD_NAME_PREFIX,
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    public static ScheduledThreadPoolExecutor instance(int core, String threadName, RejectedExecutionHandler rejectedExecutionHandler) {
        return new ScheduledThreadPoolExecutor(
                core,
                new MyThreadFactory(threadName),
                rejectedExecutionHandler
        );
    }

}
