package com.ouweihao.community.service;

import java.util.Date;

public interface DateCountService {

    /**
     * 向redis中键名为uv:date的键中添加数据，以ip为value
     *
     * @param ip 独立访客的ip
     */
    void recordUV(String ip);

    /**
     * 计算从startDate到endDate之内的独立用户数
     *
     * @param startDate 开始日期
     * @param endDate   停止日期
     * @return 独立用户数
     */
    long calculateUV(Date startDate, Date endDate);

    /**
     * 值类型为bitmao，将bitmap中索引为userId的值设为true
     *
     * @param userId 用户Id
     */
    void recordDAU(int userId);

    /**
     * 计算从startDate到endDate中活跃用户的数量
     *
     * @param startDate 开始日期
     * @param endDate   截止日期
     */
    long calculateDAU(Date startDate, Date endDate);

}
