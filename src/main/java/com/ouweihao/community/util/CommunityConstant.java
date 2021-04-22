package com.ouweihao.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认的登陆失效时间，四小时
     */
    int DEFAULT_EXPIRED_TIME = 3600 * 4;

    /**
     * 勾选记住我之后的登陆失效时间，四天
     */
    int REMEMBER_EXPIRED_TIME = 3600 * 12 * 4;

    /**
     * 表示实体的类型：帖子
     */
    int ENTITY_POST = 1;

    /**
     * 表示实体的类型：评论
     */
    int ENTITY_COMMENT = 2;

}
