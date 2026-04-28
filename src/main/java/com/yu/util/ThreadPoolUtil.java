package com.yu.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor;

    static {
        // 队列大小有限制，防止高并发时OOM
        threadPoolExecutor = new ThreadPoolExecutor(30, 50, 5, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void submit(Runnable runnable) {
        threadPoolExecutor.submit(runnable);
    }
}
