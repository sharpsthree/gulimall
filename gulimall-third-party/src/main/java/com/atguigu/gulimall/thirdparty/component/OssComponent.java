package com.atguigu.gulimall.thirdparty.component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/5 02:43
 * @Version 1.0
 **/
@Slf4j
@Component
public class OssComponent {

    @Autowired
    OSS ossClient;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String ALIYUN_OSS_BUCKET_NAME;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String ALIYUN_OSS_ENDPOINT;
    @Value("${spring.cloud.alicloud.access-key}")
    private String ALIYUN_OSS_ACCESS_KEY;



    /**
     * 签名生成
     */
    public Map<String,String> policy() {

        String bucket = ALIYUN_OSS_BUCKET_NAME; // 请填写您的 bucketname 。
        String host = "https://" + bucket + "." + ALIYUN_OSS_ENDPOINT; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
//        String callbackUrl = "http://88.88.88.88:8888";

        // 存储目录
        String format = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String dir = "gulimall/image/" + format + "/"; // 用户上传文件时指定的前缀。
        log.debug("上传的路径为：{}",dir);
        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", ALIYUN_OSS_ACCESS_KEY);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));


        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }
        return respMap;
    }
}
