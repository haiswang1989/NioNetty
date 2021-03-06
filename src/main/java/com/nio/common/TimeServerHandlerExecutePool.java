package com.nio.common;

import java.util.concurrent.ExecutorService;

/**
 * 处理线程池
 * 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月12日 上午11:56:08
 */
public class TimeServerHandlerExecutePool {
    
    private ExecutorService executor;
    
    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
//        executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120l, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
    }
    
    public void execute(Runnable task) {
        executor.submit(task);
    }
}
