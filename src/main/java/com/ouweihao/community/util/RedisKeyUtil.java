package com.ouweihao.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String LIKE_ENTITY_PREFIX = "like:entity";
    private static final String LIKE_USER_PREFIX = "like:user";

    // 形如like:entity:entityType:entityId
    // 帖子或评论的赞在redis中是一个集合，集合名称是like：entity:实体类型:实体id，里面存的是userId，
    // 可以查看谁给你点过赞（通过userId）和赞的总数（返回set中userId的个数）

    public static String getEntityLikeKey(int entityType, int entityId) {
        return LIKE_ENTITY_PREFIX + SPLIT + entityType + SPLIT + entityId;
    }

    // 形如like:user:userId
    // 存放的是值的类型是value

    public static String getUserLikeKey(int userId) {
        return LIKE_USER_PREFIX + SPLIT + userId;
    }

}
