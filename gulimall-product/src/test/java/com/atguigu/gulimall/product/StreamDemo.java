package com.atguigu.gulimall.product;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamDemo {

    public static void main(String[] args) throws InterruptedException {

        List<String> strings = Arrays.asList("aa","fdf","zdf","tdf","rrf");
        List<String> af = strings.stream().filter(arr -> arr.contains("d")).collect(Collectors.toList());
        System.out.println(af);
    }

}