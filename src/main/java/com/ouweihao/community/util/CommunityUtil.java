package com.ouweihao.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串，可用于验证注册以及之后的上传的名字

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5算法(只能加密，不能解密)对密码进行加密，密码+Salt --（MD5）--->密码

    public static String MD5(String key) {
        if (StringUtils.isBlank(key)) { // null，空串或者只有空格都判定为空
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static void main(String[] args) {
        System.out.println(MD5("123c357d"));
    }

}
