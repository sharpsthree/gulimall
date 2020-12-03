package com.atguigu.gulimall.order.listenner;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description 响应支付宝异步通知
 * @Author 鲁班不会飞
 * @Date 2020/5/9 16:20
 * @Version 1.0
 **/
@RestController
public class OrderPayedListenner {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // 只要我们收到了支付宝的异步通知，告诉我们订单支付成功，我们就返回success给支付宝，支付宝就不会在进行通知
        /*Map<String, String[]> map = request.getParameterMap();
        System.out.println("******支付宝异步通知请求参数******");
        for (String key : map.keySet()) {
            String value = request.getParameter(key);
            System.out.println("参数名：" + key + "==>  参数值：" + value);
        }*/

        // 验签
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified) {
            // 验签成功
            System.out.println("*******验签成功******");
            String result = orderService.handlePayResult(payAsyncVo);

            return result;
        } else {
            // 验签失败
            System.out.println("*******验签失败******");
            return "fail";
        }

    }
}
