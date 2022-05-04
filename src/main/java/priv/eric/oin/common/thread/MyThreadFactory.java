package priv.eric.oin.common.thread;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/5/4 16:24
 */
public class MyThreadFactory implements ThreadFactory {
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
