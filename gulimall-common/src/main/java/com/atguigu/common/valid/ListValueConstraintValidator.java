package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/5 07:59
 * @Version 1.0
 **/
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    /**
     * 初始化方法
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        if (vals.length > 0) {
            for (int val : vals) {
                set.add(val);
            }
        }
    }

    /**
     * 判断是否校验成功
     * @param value 需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        if (set.size() > 0) {
            if (set.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
