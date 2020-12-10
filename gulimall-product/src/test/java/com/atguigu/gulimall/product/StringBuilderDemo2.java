package com.atguigu.gulimall.product;


public class StringBuilderDemo2 {

    public static void main(String[] args) throws InterruptedException {
        String a = new String("fdfs");

        for(int i=0;i<1000;i++){
            a+="1";
        }
        System.out.println(a);
    }

}