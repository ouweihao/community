package com.ouweihao.community.service;

import java.util.List;
import java.util.Map;

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

    /**
     * 返回一个用户的关注列表的所有用户
     *
     * @param userId 被查询用户的id
     * @param offset 用于分页
     * @param limit  用于分页
     * @return 用户和关注时间的map所组成的list
     */
    List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

    /**
     * 返回所有粉丝
     *
     * @param userId 实体Id
     * @param offset 用于分页
     * @param limit  用于分页
     * @return 粉丝和粉丝所关注的时间组合成的map所组成的list
     */
    List<Map<String, Object>> findFollowers(int userId, int offset, int limit);

}
