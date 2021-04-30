package com.ouweihao.community.service;

public interface FollowService {

    void follow(int userId, int entityType, int entityId);

    void unfollow(int userId, int entityType, int entityId);

    /**
     * 返回某用户关注某一实体类型（帖子或用户）的个数
     *
     * @param userId     用户Id
     * @param entityType 实体类型
     * @return 关注个数
     */
    long findFolloweeCount(int userId, int entityType);

    /**
     * 返回某实体的粉丝数量
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @return 粉丝数量
     */
    long findFollowerCount(int entityType, int entityId);

    /**
     * 查询某个用户是否关注实体类型为entityType，实体id为entityId的实体
     *
     * @param userId     当前用户Id
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return 是否关注
     */
    boolean hasFollowed(int userId, int entityType, int entityId);

}
