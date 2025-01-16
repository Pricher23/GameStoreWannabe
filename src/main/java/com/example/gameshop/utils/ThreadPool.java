package com.example.gameshop.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public static void execute(Runnable task) {
        executor.execute(task);
    }
    
    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }
    
    public static void shutdown() {
        executor.shutdown();
    }
} 