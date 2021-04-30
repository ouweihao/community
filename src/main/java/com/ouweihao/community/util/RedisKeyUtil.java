package com.ouweihao.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String LIKE_ENTITY_PREFIX = "like:entity";
    private static final String LIKE_USER_PREFIX = "like:user";

    // 关注的人

    private static final String FOLLOWEE_PREFIX = "followee";

    // 粉丝

    private static final String FOLLOWER_PREFIX = "follower";

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

    /**
     * 返回某个人关注的实体的有序集合的键，因为关注的列表是人关注的列表，所以用userId做键
     *
     * @param userId     某个人
     * @param entityType 实体类型，可以是人，也可以是帖子等
     * @return 形如followee:userId:entityType的字符串
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return FOLLOWEE_PREFIX + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 返回某个实体的粉丝的有序集合的键，
     * 因为只有人才能进行关注操作，而人能进行的关注的实体现在有两种，一种是人，一种是帖子，所以值只可能是userId
     * 所以实体的类型和id作为键
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return 形如follower:entityType:entityId的字符串
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return FOLLOWER_PREFIX + SPLIT + entityType + SPLIT + entityId;
    }

}
