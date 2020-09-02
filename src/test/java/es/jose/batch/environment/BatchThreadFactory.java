package es.jose.batch.environment;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchThreadFactory implements ThreadFactory {
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final static String namePrefix = "jberet-";

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        t.setDaemon(true);
        //some libs rely on TCCL
        //t.setContextClassLoader(null);
        return t;
    }
}