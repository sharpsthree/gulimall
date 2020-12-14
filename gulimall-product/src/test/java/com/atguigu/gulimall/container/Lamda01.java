package com.atguigu.gulimall.container;


import java.util.function.Function;

public class Lamda01 {

    public static void main(String[] args) throws InterruptedException {

//        UnaryOperator<Integer> unaryOperator = new UnaryOperator<Integer>() {
//            @Override
//            public Integer apply(Integer integer) {
//                return integer+1;
//            }
//        };
//        System.out.println(unaryOperator.apply(3));

//        int a = new UnaryOperator<Integer>() {
//            @Override
//            public Integer apply(Integer integer) {
//                return integer+2;
//            }
//        }.apply(3) ;
//        System.out.println(a);

//        UnaryOperator<Integer> u = (n)->{return 2*n;};
//
//        System.out.println(u.apply(3));

//
//        UserDao uu = (a,b)->{
//            return a+b;
//        };
//        System.out.println(uu.sum(2, 3));
//        Supplier<String> supplier = ()->{
//            return "dd";
//        };
//        System.out.println(supplier.get());

        Function<String,Integer> function = (text)->{
            return 1;
        };
        function.apply("fe");

    }

}