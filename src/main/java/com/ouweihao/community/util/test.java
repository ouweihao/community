package com.ouweihao.community.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class test {

    public static String getJsonString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("code", code);
        jsonObject.put("message", msg);
        if(map != null) {
            for(String key : map.keySet()) {
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
        int code = 200;
        String msg = "ok";
        Map<String, Object> map = new HashMap<>();
        map.put("name", "ouweihao");
        map.put("age", 21);
        System.out.println(getJsonString(code, msg, map));
    }

}
