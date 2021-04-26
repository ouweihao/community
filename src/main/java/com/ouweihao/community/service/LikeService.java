package com.ouweihao.community.service;

public interface LikeService {

    /**
     * 实现点赞和取消点赞的功能
     *
     * @param userId     点赞的用户的id
     * @param entityType 实体的类型，是评论还是帖子
     * @param entityId   实体的id
     */
    void like(int userId, int entityType, int entityId);

    /**
     * 统计点赞数量
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return 类型为entityType，id为entityId的实体的点赞数
     */
    long findEntityLikeCount(int entityType, int entityId);

    /**
     * 查询用户的点赞状态
     *
     * @param userId     当前用户的id
     * @param entityType 实体的类型
     * @param entityId   实体的id
     * @return 用户的实体的状态，1表示已点过赞，0表示未点过赞，-1表示点过踩（拓展业务）
     */
    int findEntityLikeStatus(int userId, int entityType, int entityId);

}
