package com.ouweihao.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    // map用于传输数据

    public static String getJsonString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        // 判空
        if (map != null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJsonString(int code, String msg) {
        return getJsonString(code, msg, null);
    }

    public static String getJsonString(int code) {
        return getJsonString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "nihao");
        map.put("age", 25);
        System.out.println(getJsonString(200, "ok", map));
    }

}
