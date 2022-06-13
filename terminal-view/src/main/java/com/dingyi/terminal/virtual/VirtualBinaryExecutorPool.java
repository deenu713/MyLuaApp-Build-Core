package com.dingyi.terminal.virtual;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualBinaryExecutorPool {


    private static VirtualBinaryExecutorPool INSTANCE;

    private AtomicInteger threadAtomic;

    private VirtualBinaryExecutorPool() {
    }

    private Timer checkExecutorPoolTimer;

    public static VirtualBinaryExecutorPool getInstance() {
        synchronized (VirtualBinaryExecutorPool.class) {
            if (INSTANCE == null) {
                INSTANCE = new VirtualBinaryExecutorPool();
            }
        }
        return INSTANCE;
    }


    private ExecutorService executorPool;

    private void createExecutorPool() {
        executorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        threadAtomic = new AtomicInteger();

        checkExecutorPoolTimer = new Timer("ExecutorService-PoolCheck");

    }


    public void waitFor() throws InterruptedException {
        executorPool.shutdown();
        executorPool.awaitTermination(1, TimeUnit.HOURS);
    }

    boolean canCloseExecutorPool() {
        return threadAtomic.get() >= ((ThreadPoolExecutor) executorPool).getPoolSize();
    }

    public void closeExecutorPool() {
        executorPool.shutdown();
        executorPool = null;

        checkExecutorPoolTimer.cancel();
        checkExecutorPoolTimer = null;
        threadAtomic.set(0);
    }

    synchronized void execBinaryExecutor(VirtualBinaryExecutor binaryExecutor) {
        if (executorPool == null) {
            createExecutorPool();
        }
        executorPool.submit(binaryExecutor);
        if (threadAtomic.get() == 0) {
            checkExecutorPoolTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (canCloseExecutorPool()) {
                        closeExecutorPool();
                    }
                }
            }, 1000, 1000 * 60);
        }
        threadAtomic.incrementAndGet();
    }

}
