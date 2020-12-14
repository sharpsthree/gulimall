package com.atguigu.gulimall.product;


import org.junit.Test;

//线程类
public class TestThread extends Thread {
    public void run() {
        while(true){
            try {
                sleep(10*1000);
                //这里可以写你自己要运行的逻辑代码
                System.out.println("一分钟运行一次");
                Print();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    @Test
    public void Print(){
        for (int i = 0; i < 10 ; i++) {
            System.out.println(i);
        }
    }
}
