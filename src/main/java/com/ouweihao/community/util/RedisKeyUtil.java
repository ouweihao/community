package com.ouweihao.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String LIKE_ENTITY_PREFIX = "like:entity";
    private static final String LIKE_USER_PREFIX = "like:user";

    // 关注的人

    private static final String FOLLOWEE_PREFIX = "followee";

    // 粉丝

    private static final String FOLLOWER_PREFIX = "follower";

    // 验证码前缀

    private static final String KAPTCHA_PREFIX = "kaptcha";

    // 生成登录凭证的前缀

    private static final String TICKET_PREFIX = "ticket";

    // 生成用户缓存

    private static final String USER_PREFIX = "user";

    // 独立访客前缀，以IP为值存入

    private static final String UV_PREFIX = "uv";

    // 统计日活跃用户

    private static final String DAU_PREFIX = "dau";

    // 关于帖子的前缀

    private static final String POST_PREFIX = "post";

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

    /**
     * 生成验证码的键
     *
     * @param owner 随机字符串，用于标识用户
     * @return 形如kaptcha:owner的字符串
     */
    public static String getKaptchaKey(String owner) {
        return KAPTCHA_PREFIX + SPLIT + owner;
    }

    /**
     * 返回存储登录凭证的键名
     *
     * @param ticket 随机生成的字符串，唯一标识一个用户
     * @return 形如ticket:随机字符串的字符串
     */
    public static String getTicketKey(String ticket) {
        return TICKET_PREFIX + SPLIT + ticket;
    }

    /**
     * 生成存储在redis中的对象的键，以userId为唯一标识符
     *
     * @param userId 用户Id
     * @return 形如user:userId的字符串
     */
    public static String getUserKey(int userId) {
        return USER_PREFIX + SPLIT + userId;
    }

    /**
     * 返回某个日期内独立用户访问ip集合的键名，类型为HyperLogLog
     *
     * @param date 日期格式化后的字符串，格式话Pattern为"yyyyMMdd"
     * @return 键名
     */
    public static String getUVKey(String date) {
        return UV_PREFIX + SPLIT + date;
    }

    /**
     * 返回某日期区间内独立用户访问ip集合的键名，类型为HyperLogLog
     *
     * @param startDate 开始日期，日期格式化后的字符串，格式话Pattern为"yyyyMMdd"
     * @param endDate   结束日期
     * @return 键名
     */
    public static String getUVKey(String startDate, String endDate) {
        return UV_PREFIX + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 某一天活跃用户的bitmap的键名，类型为bitmap，以userId为索引，假设用户在date这天登录，则userId这个索引上的值设为true
     *
     * @param date 某一天，日期格式化后的字符串，格式话Pattern为"yyyyMMdd"
     * @return 键名
     */
    public static String getDAUKey(String date) {
        return DAU_PREFIX + SPLIT + date;
    }

    /**
     * 从startDate开始到endDate这天结束的区间内所有用户的bitmap的键名
     *
     * @param startDate 开始日期，日期格式化后的字符串，格式话Pattern为"yyyyMMdd"
     * @param endDate   结束日期
     * @return 键名
     */
    public static String getDAUKey(String startDate, String endDate) {
        return DAU_PREFIX + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 得到需要更新帖子分数的帖子Id
     *
     * @return 存储需要更新分数帖子Id的集合
     */
    public static String getPostScoreKey() {
        return POST_PREFIX + SPLIT + "score";
    }

}
