package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/18 10:36
 * @Version 1.0
 **/
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main...start");
        /**
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程号 -> " + Thread.currentThread().getId());
            int n = 10 / 5;
            System.out.println("运行结果：" + n);
        }, executor); */


        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程号 -> " + Thread.currentThread().getId());
            int n = 10 / 0;
            return n;
        }, executor).whenComplete((result,excption) -> {
            System.out.println("运行结果：" + result + "异常：" + excption);
        }).exceptionally(throwable -> {
            return 10;
        });

        Integer integer = future.get();
        System.out.println("最终运行结果：" + integer);
        System.out.println("main...end");

    }














    public static void thread(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main... start");

        // 1、继承Thread
//        Thread01 thread01 = new Thread01();
//        thread01.start();

        // 2、实现Runnable接口
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();

        // 3、实现Callable接口 + FutureTask (可以拿到返回结果，可以处理异常)
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
        // 等待线程执行完获取结果
//        Integer n = futureTask.get();
//        System.out.println(n);

        // 线程池 --  资源控制
//        service.execute(new Runnable01());
        /**
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());


        System.out.println("main... end");

    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("Runnable01当前线程号 -> " + Thread.currentThread().getId());
            int n = 10 /5;
            return n;
        }

    }


    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("Runnable01当前线程号 -> " + Thread.currentThread().getId());
            int n = 10 /5;
            System.out.println(n);
        }
    }

    public static class Thread01 extends Thread{

        @Override
        public void run() {

            System.out.println("Thread01当前线程号 -> " + Thread.currentThread().getId());
            int n = 10 /5;
            System.out.println(n);
        }
    }
}
