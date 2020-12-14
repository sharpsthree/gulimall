package com.atguigu.gulimall.product;


import org.junit.Test;

import java.util.Arrays;

public class ET {

    public static void main(String[] args) throws InterruptedException {


    }
    @Test
    public void Test(){
        Integer[] is = new Integer[]{0,1,2,3,4,5,6};
        change(is,1,4);
        System.out.println(Arrays.toString(is));

        String[] ss = new String[]{"零","一","二","三","四"};
        change(ss,0,3);
        System.out.println(Arrays.toString(ss));
    }

    public <T> void change(T[] arr, int a, int b){
        T temp = arr[a];
        arr[a]=arr[b];
        arr[b]=temp;
    }

}