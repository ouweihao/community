package com.ouweihao.community.service;

import com.ouweihao.community.entity.Comment;

import java.util.List;

public interface CommentService {

    /**
     * 根据评论Id查找评论
     *
     * @param id 评论Id
     * @return 评论
     */
    Comment findCommentById(int id);

    /**
     * 根据用户的实体类型和对应类型下的id实现分页查询
     *
     * @param entityType 评论对应的类型，是对评论的回复还是对帖子的评论
     * @param entityId   评论或回复对应的id
     * @param offset     页数
     * @param limit      每页评论数
     * @return 对应类型的帖子或评论的评论或回复
     */
    List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 查询对应帖子或评论下的评论或回复的数量
     *
     * @param entityType 帖子或评论的类型
     * @param entityId   帖子或评论所对应的id
     * @return 类型为entityType且id为entityId所对应评论或回复的数量
     */
    int findCommentCount(int entityType, int entityId);

    int addComment(Comment comment);

}
