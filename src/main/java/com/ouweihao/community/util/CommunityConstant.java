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
    int DEFAULT_EXPIRED_TIME = 60 * 60 * 4;

    /**
     * 勾选记住我之后的登陆失效时间，四天
     */
    int REMEMBER_EXPIRED_TIME = 60 * 60 * 12 * 4;

    /**
     * 表示实体的类型：帖子
     */
    int ENTITY_POST = 1;

    /**
     * 表示实体的类型：评论
     */
    int ENTITY_COMMENT = 2;

    /**
     * 表示实体的类型：用户
     */
    int ENTITY_USER = 3;

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：帖子
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题：删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 主题：分享
     */
    String TOPIC_SHARE = "share";

    /**
     * 系统用户Id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";

    /**
     * 权限：禁言用户
     */
    String AUTHORITY_FORBIDDEN = "forbidden";

}
